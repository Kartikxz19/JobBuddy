from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from google.oauth2 import id_token
from dotenv import load_dotenv
from google.auth.transport import requests as google_requests
import jwt
import os
import datetime

load_dotenv()

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = os.getenv("POSTGRESQL_URI")
app.config['SECRET_KEY'] = os.getenv("SECRET_KEY")

db = SQLAlchemy(app)

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
    expiration_time = datetime.datetime.utcnow() + datetime.timedelta(hours=1)
    payload = {
        'user_id': user_id,
        'exp': expiration_time
    }
    token = jwt.encode(payload, app.config['SECRET_KEY'], algorithm='HS256')
    return token

@app.route('/login', methods=['POST'])
def login():
    try:
        token = request.json.get('credential')
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

        return jsonify({'message': 'User logged in successfully', 'user_id': user.id, 'token': jwt_token}), 200

    except ValueError as e:
        return jsonify({'error': str(e)}), 400
    except Exception as e:
        return jsonify({'error': 'An error occurred'}), 500

def token_required(func):
    def wrapper(*args, **kwargs):
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'error': 'Token missing'}), 401

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

@app.route('/getResumeScore', methods=['GET'])
@token_required
def get_resume_score(user_id):
    return jsonify({'message': f'User {user_id}, your resume score is 85'}), 200

if __name__ == '__main__':
    app.run(debug=True)
