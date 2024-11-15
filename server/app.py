from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from google.oauth2 import id_token
from dotenv import load_dotenv
from google.auth.transport import requests as google_requests
import jwt
import os
from datetime import datetime, timedelta
from job_matcher import *
import glob
import re

load_dotenv()

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = os.getenv("POSTGRESQL_URI")
app.config['SECRET_KEY'] = os.getenv("SECRET_KEY")

db = SQLAlchemy(app)

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


with app.app_context():
    db.create_all()

def generate_jwt_token(user_id):
    expiration_time = datetime.utcnow() + timedelta(hours=24)
    payload = {
        'user_id': user_id,
        'exp': expiration_time
    }
    token = jwt.encode(payload, app.config['SECRET_KEY'], algorithm='HS256')
    return token

@app.route('/login', methods=['POST'])
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

        return jsonify({'message': 'User logged in successfully', 'user_id': str(user.id), 'token': jwt_token}), 200

    except ValueError as e:
        return jsonify({'error': str(e)}), 400
    except Exception as e:
        print(str(e))
        return jsonify({'error': 'An error occurred'}), 500

def token_required(func):
    def wrapper(*args, **kwargs):
        auth_header = request.headers.get('Authorization')
        if not auth_header:
            return jsonify({'error': 'Authorization header missing'}), 401

        if not auth_header.startswith('Bearer '):
            return jsonify({'error': 'Invalid token format. Expected Bearer token.'}), 401

        # Extract the token after 'Bearer '
        token = auth_header.split('Bearer ')[1]

        try:
            decoded_token = jwt.decode(token, app.config['SECRET_KEY'], algorithms=['HS256'])
            user_id = decoded_token['user_id']
            return func(user_id=user_id, *args, **kwargs)
        except jwt.ExpiredSignatureError:
            return jsonify({'error': 'Token has expired'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'error': 'Invalid token'}), 401

    wrapper.__name__ = func.__name__
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

@app.route('/uploadResume', methods=['POST'])
@token_required
def upload_resume(user_id):
    try:
        if 'resume_file' not in request.files:
            return jsonify({'error': 'No resume file provided'}), 400

        file = request.files['resume_file']

        if file.filename == '':
            return jsonify({'error': 'No selected file'}), 400

        if not file.filename.lower().endswith('.pdf'):
            return jsonify({'error': 'Only PDF files are allowed'}), 400

        # Generate a timestamped filename
        timestamp = datetime.now().strftime('%Y%m%d%H%M%S')
        file_path = os.path.join('resumes', f"{user_id}_{timestamp}.pdf")

        # Save the uploaded file
        file.save(file_path)

        return jsonify({'message': 'Resume uploaded successfully', 'file_path': file_path}), 200

    except Exception as e:
        print(e)
        return jsonify({'error': 'An internal server error occurred'}), 500

@app.route('/checkResumeScore', methods=['POST'])
@token_required
def check_resume_score(user_id):
    try:
        job_description = request.json.get('job_description')
        if not job_description:
            return jsonify({'error': 'Job description is required'}), 400

        # Check if user has an uploaded resume
        file_path, _ = get_latest_resume_path(user_id)
        if not file_path:
            return jsonify({'error': 'No resume uploaded for this user'}), 400


        # Process the extracted text and job description (mocked for demonstration)
        answer, jobPosting, extractedResume = process_job_and_resume(
            job_profile=job_description,
            resume_pdf=file_path
        )
        
        return jsonify({
            'answer': answer,
            'job_posting': jobPosting,
            'extracted_resume': extractedResume
        }), 200


    except Exception as e:
        print(e)
        return jsonify({'error': 'An internal server error occurred'}), 500


@app.route('/getResumeStatus', methods=['GET'])
@token_required
def get_resume_status(user_id):
    try:
        # Check if user has an uploaded resume
        file_path, upload_time = get_latest_resume_path(user_id)
        if not file_path:
            return jsonify({'message': 'No resume uploaded'}), 200

        return jsonify({
            'message': 'Resume uploaded',
            'file_path': file_path,
            'upload_time': upload_time.strftime('%Y-%m-%d %H:%M:%S')
        }), 200

    except Exception as e:
        print(e)
        return jsonify({'error': 'An internal server error occurred'}), 500
    
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

@app.route('/api/profile', methods=['GET'])
@token_required
def get_profile(user_id):
    response = get_profile_data(user_id)
    return jsonify(response)


@app.route('/api/profile', methods=['PUT'])
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
        return jsonify({"message": "Profile updated successfully"})

    except Exception as e:
        db.session.rollback()
        return jsonify({"error": str(e)}), 400


@app.route('/api/generateInterviewQuestions', methods=['POST'])
@token_required
def get_questions(user_id):
    try:
        job_description = request.json.get('job_description')
        if not job_description:
            return jsonify({'error': 'Job description is required'}), 400
        resume = get_profile_data(user_id)
        questions = generate_interview_questions(job_description, resume)
        return jsonify({"questions" : questions})
    except Exception as e:
        print(str(e))
        return jsonify({"error" : str(e)}), 500


@app.route('/api/evaluateInterview', methods=['POST'])
@token_required
def evaluate(user_id):
    try:
            
        data = request.get_json()
        if not data:
            return jsonify({"error": "No data provided"}), 400

        job_description = data.get('job_description')
        if not job_description:
            return jsonify({'error': 'Job description is required'}), 400
        responses = data.get('questions_responses', [])
        resume = get_profile_data(user_id)
        evaluation = evaluate_interview(responses, job_description, resume)
        return jsonify({"evaluation" : evaluation})
    except Exception as e:
        print(str(e))
        return jsonify({"error" : str(e)}), 500


if __name__ == '__main__':
    app.run(debug=False)
