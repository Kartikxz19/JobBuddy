from flask import Flask, request, jsonify, send_file
from functools import wraps
from flask_sqlalchemy import SQLAlchemy
from google.oauth2 import id_token
from dotenv import load_dotenv
from langchain_groq import ChatGroq
from langchain_core.prompts import PromptTemplate
from langchain_core.output_parsers import JsonOutputParser
from google.auth.transport import requests as google_requests
from pathlib import Path
from datetime import datetime, timedelta
import logging
import jwt
import os
import glob
import re
from job_matcher import format_json, generate_interview_questions
from generateResume import enhance_all_descriptions, generate_latex, convert_latex_to_pdf
from flashCards import FlashcardSystem

load_dotenv()

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = os.getenv("POSTGRESQL_URI")
app.config['SECRET_KEY'] = os.getenv("SECRET_KEY")

db = SQLAlchemy(app)

llm = ChatGroq(
    model="llama-3.1-70b-versatile",
    temperature=0,
    groq_api_key=os.getenv("GROQ_API_KEY"),
)


if not os.path.exists('resumes'):
    os.makedirs('resumes')

class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    google_id = db.Column(db.String(255), unique=True, nullable=False)
    email = db.Column(db.String(255), unique=True, nullable=False)
    name = db.Column(db.String(255), nullable=False)

    def __init__(self, google_id, email, name):
        self.google_id = google_id
        self.email = email
        self.name = name

class Skill(db.Model):
    __tablename__ = 'skills'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    skill = db.Column(db.String(255), nullable=False)
    level = db.Column(db.Integer, nullable=False)

class Project(db.Model):
    __tablename__ = 'projects'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    name = db.Column(db.String(255), nullable=False)
    tech_stack = db.Column(db.String(500), nullable=False)
    demo_link = db.Column(db.String(500))
    start_date = db.Column(db.String(7), nullable=False)  # Format: MM/YYYY
    end_date = db.Column(db.String(7), nullable=False)    # Format: MM/YYYY
    description = db.Column(db.Text, nullable=False)

class Experience(db.Model):
    __tablename__ = 'experiences'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    title = db.Column(db.String(255), nullable=False)
    company = db.Column(db.String(255), nullable=False)
    start_date = db.Column(db.String(7), nullable=False)  # Format: MM/YYYY
    end_date = db.Column(db.String(7), nullable=False)    # Format: MM/YYYY
    description = db.Column(db.Text, nullable=False)

class Achievement(db.Model):
    __tablename__ = 'achievements'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    title = db.Column(db.String(255), nullable=False)
    description = db.Column(db.Text, nullable=False)

class Education(db.Model):
    __tablename__ = 'education'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    institution = db.Column(db.String(255), nullable=False)
    degree = db.Column(db.String(255), nullable=False)
    start_date = db.Column(db.String(7), nullable=False)  # Format: MM/YYYY
    end_date = db.Column(db.String(7), nullable=False)    # Format: MM/YYYY

class ProfileLink(db.Model):
    __tablename__ = 'profile_links'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    platform = db.Column(db.String(255), nullable=False)
    url = db.Column(db.String(500), nullable=False)

class ContactDetail(db.Model):
    __tablename__ = 'contact_details'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    type = db.Column(db.String(255), nullable=False)
    value = db.Column(db.String(255), nullable=False)


with app.app_context():
    db.create_all()

# Helper functions
def parse_date(date_str):
    """Convert MM/YYYY to month and year dict"""
    if not re.match(r'^\d{2}/\d{4}$', date_str):
        raise ValueError("Date must be in MM/YYYY format")
    month, year = date_str.split('/')
    return {"month": int(month), "year": int(year)}

def format_date(month, year):
    """Convert month and year to MM/YYYY format"""
    return f"{int(month):02d}/{year}"

def generate_jwt_token(user_id):
    expiration_time = datetime.utcnow() + timedelta(hours=24)
    payload = {
        'user_id': user_id,
        'exp': expiration_time
    }
    token = jwt.encode(payload, app.config['SECRET_KEY'], algorithm='HS256')
    return token

def token_required(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        auth_header = request.headers.get('Authorization')
        if not auth_header:
            return jsonify({'error': 'Authorization header missing'}), 401

        if not auth_header.startswith('Bearer '):
            return jsonify({'error': 'Invalid token format. Expected Bearer token.'}), 401

        token = auth_header.split('Bearer ')[1]

        try:
            decoded_token = jwt.decode(token, app.config['SECRET_KEY'], algorithms=['HS256'])
            user_id = decoded_token['user_id']
            return func(user_id=user_id, *args, **kwargs)
        except jwt.ExpiredSignatureError:
            return jsonify({'error': 'Token has expired'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'error': 'Invalid token'}), 401

    return wrapper

def get_latest_resume_path(user_id):
    """Helper function to get the latest resume file path for a user."""
    files = glob.glob(f'resumes/{user_id}_*.pdf')
    if not files:
        return None, None
    latest_file = max(files, key=os.path.getctime)
    upload_time_str = latest_file.split('_')[-1].replace('.pdf', '')
    upload_time = datetime.strptime(upload_time_str, '%Y%m%d%H%M%S')
    return latest_file, upload_time

def get_profile_data(user_id):
    skills = Skill.query.filter_by(user_id=user_id).all()
    projects = Project.query.filter_by(user_id=user_id).all()
    experiences = Experience.query.filter_by(user_id=user_id).all()
    achievements = Achievement.query.filter_by(user_id=user_id).all()
    education = Education.query.filter_by(user_id=user_id).all()
    profile_links = ProfileLink.query.filter_by(user_id=user_id).all()
    contact_details = ContactDetail.query.filter_by(user_id=user_id).all()

    return {
        "skills": [{
            "skill": s.skill,
            "level": s.level
        } for s in skills],
        "projects": [{
            "name": p.name,
            "techStack": p.tech_stack,
            "demoLink": p.demo_link,
            "startDate": parse_date(p.start_date),
            "endDate": parse_date(p.end_date),
            "description": p.description
        } for p in projects],
        "experience": [{
            "title": e.title,
            "company": e.company,
            "startDate": parse_date(e.start_date),
            "endDate": parse_date(e.end_date),
            "description": e.description
        } for e in experiences],
        "achievements": [{
            "title": a.title,
            "description": a.description
        } for a in achievements],
        "education": [{
            "institution": e.institution,
            "degree": e.degree,
            "startDate": parse_date(e.start_date),
            "endDate": parse_date(e.end_date)
        } for e in education],
        "profileLinks": [{
            "platform": p.platform,
            "url": p.url
        } for p in profile_links],
        "contactDetails": [{
            "type": c.type,
            "value": c.value
        } for c in contact_details]
    }

def get_user_resumes(user_id):
    """Helper function to get all resume file paths and their upload times for a user."""
    files = glob.glob(f'resumes/{user_id}_*.pdf')
    if not files:
        return []
    
    resumes = []
    for file in files:
        upload_time_str = file.split('_')[-1].replace('.pdf', '')
        try:
            upload_time = datetime.strptime(upload_time_str, '%Y%m%d%H%M%S')
            resumes.append({
                'resume_path': file,
                'upload_time': upload_time.isoformat()
            })
        except ValueError:
            logging.warning(f"Invalid timestamp format in file name: {file}")
            continue
    
    # Sort resumes by upload time (newest first)
    resumes.sort(key=lambda x: x['upload_time'], reverse=True)
    return resumes


def evaluate_interview(responses, job_posting, resume):
    prompt = PromptTemplate.from_template(
        """
        ### Job Posting:
        {job_posting}

        ### Candidate's Resume:
        {resume}

        ### Interview Responses:
        {responses}

        ### Instruction:
        Based on the job posting, the candidate's resume, and their responses during the interview, 
        provide a comprehensive evaluation of the candidate's suitability for the role. 
        Consider the following:
        1. How well the candidate's skills and experience match the job requirements
        2. The quality and relevance of their interview responses
        3. Any strengths or unique qualifications that stand out
        4. Potential areas for improvement or skills that may need development
        5. Overall fit for the role and company culture

        Provide a detailed summary of your evaluation, including specific examples from the resume and interview responses.

        ### Evaluation:
        """
    )

    chain = prompt | llm
    evaluation = chain.invoke(
        input={"job_posting": job_posting, "resume": resume, "responses": responses}
    )

    return evaluation.content


"""
API Routes:

/api/login [POST->Login]
/api/resume/ [GET->List, POST->Upload]
/api/resume/:id [GET->Retreive Single, DELETE->Delete]
/api/profile [GET->(Return details), POST->(Update Details)]
/api/profile/generateFromResume [POST->(Resume Name)]
/api/job/simplify [POST->(Use AI to retreive JSON Details)]
/api/job/checkProfileScore [POST->(Send JSON Data, and analyze profile against job details)]
/api/job/generateTailoredResume [POST->(Send JSON Data, and generate tailored resume)]
/api/job/flashCards [POST->(Send JSON Data, and generate flash cards)]
/api/interview/generateQuestions [POST->(Send JSON Data, and generate questions)]
/api/interview/evaluateResponses [POST->(Send JSON Data, and Question Responses, and evaluate responses)]

"""


@app.route('/api/login', methods=['POST'])
def login():
    try:
        token = request.json.get('idToken')
        if not token:
            return jsonify({'error': 'Token missing'}), 400

        idinfo = id_token.verify_oauth2_token(token, google_requests.Request(), os.getenv("GOOGLE_CLIENT_ID"))

        if idinfo['aud'] != os.getenv("GOOGLE_CLIENT_ID"):
            raise ValueError('Invalid audience.')

        google_id = idinfo['sub']
        email = idinfo.get('email')
        name = idinfo.get('name')

        user = User.query.filter_by(google_id=google_id).first()
        if not user:
            user = User(google_id=google_id, email=email, name=name)
            db.session.add(user)
            db.session.commit()

        jwt_token = generate_jwt_token(user.id)
        return jsonify({'message': 'Login successful', 'token': jwt_token, 'user_id' : user.id}), 200

    except ValueError as e:
        return jsonify({'error': str(e)}), 400
    except Exception as e:
        logging.error(f"Login error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500


@app.route('/api/resume', methods=['GET'])
@token_required
def list_resumes(user_id):
    try:
        resumes = get_user_resumes(user_id)
        if not resumes:
            return jsonify({'error': 'No resumes found'}), 404
        
        return jsonify({'resumes': resumes})
    except Exception as e:
        logging.error(f"List resumes error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/api/resume', methods=['POST'])
@token_required
def upload_resume(user_id):
    try:
        if 'resume_file' not in request.files:
            return jsonify({'error': 'No resume file provided'}), 400

        file = request.files['resume_file']
        if file.filename == '' or not file.filename.lower().endswith('.pdf'):
            return jsonify({'error': 'Invalid file format. Only PDF files are allowed'}), 400

        timestamp = datetime.now().strftime('%Y%m%d%H%M%S')
        file_path = os.path.join('resumes', f"{user_id}_{timestamp}.pdf")
        file.save(file_path)

        return jsonify({'message': 'Resume uploaded successfully', 'file_path': file_path}), 200
    except Exception as e:
        logging.error(f"Upload resume error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500
    
@app.route('/api/resume/<int:resume_id>', methods=['GET'])
@token_required
def get_resume(user_id, resume_id):
    try:
        resume_path = f'resumes/{user_id}_{resume_id}.pdf'
        if not os.path.exists(resume_path):
            return jsonify({'error': 'Resume not found'}), 404
        
        return send_file(resume_path, mimetype='application/pdf')
    except Exception as e:
        logging.error(f"Get resume error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/api/resume/<int:resume_id>', methods=['DELETE'])
@token_required
def delete_resume(user_id, resume_id):
    try:
        resume_path = f'resumes/{user_id}_{resume_id}.pdf'
        if not os.path.exists(resume_path):
            return jsonify({'error': 'Resume not found'}), 404
        
        os.remove(resume_path)
        return jsonify({'message': 'Resume deleted successfully'}), 200
    except Exception as e:
        logging.error(f"Delete resume error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500


@app.route('/api/profile', methods=['GET'])
@token_required
def get_profile(user_id):
    try:
        profile_data = get_profile_data(user_id)
        return jsonify(profile_data)
    except Exception as e:
        logging.error(f"Get profile error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500


@app.route('/api/profile', methods=['POST'])
@token_required
def update_profile(user_id):
    
    data = request.get_json()
    if not data:
        return jsonify({"error": "No data provided"}), 400

    try:
        # Start transaction
        db.session.begin()

        # Delete existing records
        Skill.query.filter_by(user_id=user_id).delete()
        Project.query.filter_by(user_id=user_id).delete()
        Experience.query.filter_by(user_id=user_id).delete()
        Achievement.query.filter_by(user_id=user_id).delete()
        Education.query.filter_by(user_id=user_id).delete()
        ProfileLink.query.filter_by(user_id=user_id).delete()
        ContactDetail.query.filter_by(user_id=user_id).delete()

        # Insert new records
        for skill in data.get('skills', []):
            db.session.add(Skill(
                user_id=user_id,
                skill=skill['skill'],
                level=skill['level']
            ))

        for project in data.get('projects', []):
            db.session.add(Project(
                user_id=user_id,
                name=project['name'],
                tech_stack=project['techStack'],
                demo_link=project['demoLink'],
                start_date=format_date(project['startDate']['month'], project['startDate']['year']),
                end_date=format_date(project['endDate']['month'], project['endDate']['year']),
                description=project['description']
            ))

        for exp in data.get('experience', []):
            db.session.add(Experience(
                user_id=user_id,
                title=exp['title'],
                company=exp['company'],
                start_date=format_date(exp['startDate']['month'], exp['startDate']['year']),
                end_date=format_date(exp['endDate']['month'], exp['endDate']['year']),
                description=exp['description']
            ))

        for achievement in data.get('achievements', []):
            db.session.add(Achievement(
                user_id=user_id,
                title=achievement['title'],
                description=achievement['description']
            ))

        for edu in data.get('education', []):
            db.session.add(Education(
                user_id=user_id,
                institution=edu['institution'],
                degree=edu['degree'],
                start_date=format_date(edu['startDate']['month'], edu['startDate']['year']),
                end_date=format_date(edu['endDate']['month'], edu['endDate']['year'])
            ))

        for link in data.get('profileLinks', []):
            db.session.add(ProfileLink(
                user_id=user_id,
                platform=link['platform'],
                url=link['url']
            ))

        for contact in data.get('contactDetails', []):
            db.session.add(ContactDetail(
                user_id=user_id,
                type=contact['type'],
                value=contact['value']
            ))

        # Commit transaction
        db.session.commit()
        return jsonify({'message': 'Profile updated successfully'}), 200
    except Exception as e:
        db.session.rollback()
        logging.error(f"Update profile error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/api/profile/generateFromResume', methods=['POST'])
@token_required
def generate_profile_from_resume(user_id):
    try:
        resume_name = request.json.get('resume_name')
        if not resume_name:
            return jsonify({'error': 'Resume name is required'}), 400
            
        # Implementation for generating profile from resume
        #TODO
        return jsonify({'message': 'Profile generated successfully'}), 200
    except Exception as e:
        logging.error(f"Generate profile error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/api/job/simplify', methods=['POST'])
@token_required
def simplify_job(user_id):
    try:
        job_description = request.json.get('job_description')
        if not job_description:
            return jsonify({'error': 'Job description is required'}), 400
            
        prompt_extract = PromptTemplate.from_template(
        """
        ### SCRAPED TEXT FROM WEBSITE:
        {job_profile}
        ### INSTRUCTION:
        The scraped text is from the career's page of a website.
        Your job is to extract the job posting and return them in JSON format containing the following keys:`company`,`role`,`experience`,`skills`,`description`.
        Ensure that the `skills` key contains a list of skills.
        Only return the valid JSON.
        ### VALID JSON (NO PREAMBLE):
        """
    )
        chain = prompt_extract | llm
        response = chain.invoke(input={"job_profile": job_description})
        json_parser = JsonOutputParser()
        json_job_posting = json_parser.parse(response.content)
        return jsonify(json_job_posting), 200
    except Exception as e:
        logging.error(f"Simplify job error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500
    
@app.route('/api/job/checkProfileScore', methods=['POST'])
@token_required
def check_profile_score(user_id):
    try:
        job_data = request.json.get('job_data')
        if not job_data:
            return jsonify({'error': 'Job data is required'}), 400
            
        resume = get_profile_data(user_id)
        prompt_final = PromptTemplate.from_template(
        """
        ### JOB POSTING:
        {job_posting}
        ### CANDIDATE RESUME: 
        {resume}
        ### INSTRUCTION:
        Your task is to check whether the candidate's resume matches the job posting. 
        Analyze and provide:
        1. Overall match percentage
        2. Key matching skills and qualifications
        3. Notable gaps or missing requirements
        4. Recommendations for improvement
        5. Strengths that stand out

        Return a detailed analysis focusing on how well the candidate's qualifications align with the job requirements.
        """
    )

        chain_final = prompt_final | llm
        answer = chain_final.invoke(
            input={
                "job_posting": job_data,
                "resume": resume,
            }
        )
        return jsonify({'analysis' : answer.content}), 200
    except Exception as e:
        logging.error(f"Check profile score error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/api/job/generateTailoredResume', methods=['POST'])
@token_required
def generate_tailored_resume(user_id):
    try:
        job_data = request.json.get('job_data')
        if not job_data:
            return jsonify({'error': 'Job data is required'}), 400
        resume = get_profile_data(user_id)

        output_dir = Path("generated_resumes")
        output_dir.mkdir(exist_ok=True)

        user = User.query.filter_by(id=user_id).first()

        timestamp = datetime.now().strftime('%Y%m%d%H%M%S')
        file_names = f"{user_id}_{timestamp}."

        enhanced_data = enhance_all_descriptions(resume, job_data)

        latex_content = generate_latex(resume, enhanced_data, user.name)
        latex_filepath = output_dir / f"{file_names}tex"

        latex_filepath.write_text(latex_content, encoding='utf-8')
        pdf_filepath = output_dir / f"{file_names}pdf"

        if convert_latex_to_pdf(str(latex_filepath), str(pdf_filepath)):
            return send_file(
                    pdf_filepath,
                    mimetype='application/pdf',
                    as_attachment=True,
                    download_name=f"{user.name}_Resume_{timestamp}.pdf"
                )
        else:
            return jsonify({"error" : "Some error occured while generating resume pdf"}), 500 
    except Exception as e:
        logging.error(f"Generate tailored resume error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500
    
@app.route('/api/job/flashCards', methods=['POST'])
@token_required
def generate_flash_cards(user_id):
    try:
        job_data = request.json.get('job_data')
        if not job_data:
            return jsonify({'error': 'Job data is required'}), 400
        resume = format_json(get_profile_data(user_id))
        flashcard_system = FlashcardSystem()
        study_plan = flashcard_system.generate_study_plan(
            job_data,
            resume
        )
        return jsonify({'study_plan': study_plan}), 200
    except Exception as e:
        logging.error(f"Generate flash cards error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/api/interview/generateQuestions', methods=['POST'])
@token_required
def generate_questions(user_id):
    try:
        job_data = request.json.get('job_description')
        if not job_data:
            return jsonify({'error': 'Job description is required'}), 400
            
        resume = get_profile_data(user_id)
        questions = generate_interview_questions(job_data, resume, num_questions=2)
        return jsonify({"questions" : questions}),200
    except Exception as e:
        logging.error(f"Generate interview questions error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500



@app.route('/api/interview/evaluateResponses', methods=['POST'])
@token_required
def evaluate_responses(user_id):
    try:
        data = request.json
        if not data:
            return jsonify({'error': 'No data provided'}), 400
            
        job_description = data.get('job_description')
        responses = data.get('questions_responses', [])
        
        if not job_description or not responses:
            return jsonify({'error': 'Job description and responses are required'}), 400
            
        resume = get_profile_data(user_id)
        evaluation = evaluate_interview(responses, job_description, resume)
        return jsonify({'evaluation': evaluation}), 200
    except Exception as e:
        logging.error(f"Evaluate responses error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

if __name__ == '__main__':
    app.run(debug=True)