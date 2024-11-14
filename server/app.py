from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from google.oauth2 import id_token
from dotenv import load_dotenv
from google.auth.transport import requests as google_requests
import jwt
import os
from datetime import datetime
from job_matcher import *
import glob


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

with app.app_context():
    db.create_all()

def generate_jwt_token(user_id):
    expiration_time = datetime.datetime.utcnow() + datetime.timedelta(hours=24)
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

if __name__ == '__main__':
    app.run(debug=True)
