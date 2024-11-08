import chromadb
from dotenv import load_dotenv
import fitz
import streamlit as st
from langchain_groq import ChatGroq
from langchain_core.prompts import PromptTemplate
from langchain_core.output_parsers import JsonOutputParser
import os
import pandas as pd
import time
import uuid

# Conditional imports for audio functionality
try:
    import speech_recognition as sr
    from gtts import gTTS
    import io
    import pygame
    AUDIO_AVAILABLE = True
except ImportError:
    AUDIO_AVAILABLE = False

load_dotenv()
llm = ChatGroq(
    model="llama-3.1-70b-versatile",
    temperature=0,
    groq_api_key=os.getenv("GROQ_API_KEY"),
)


# Format JSON to a more readable format
def format_json(data):
    result = []
    for key, value in data.items():
        if isinstance(value, list):
            result.append(f"{key.capitalize()}:")
            for item in value:
                result.append(f"  - {item}")
        else:
            result.append(f"{key.capitalize()}: {value}")
    return "\n".join(result)


# Function to fetch questions based on skills
def get_questions(skills: str):
    df = pd.read_csv("interviewV3.csv")
    client = chromadb.PersistentClient("vectorstore")
    collection = client.get_or_create_collection(name="interview")

    if not collection.count():
        for _, row in df.iterrows():
            collection.add(
                documents=row["Skills"],
                metadatas={"Ques": row["Questions"]},
                ids=[str(uuid.uuid4())],
            )
    data = collection.query(query_texts=[skills], n_results=10)
    questions = [metadata["Ques"] for metadata in data["metadatas"][0]]
    return questions


# Modified text-to-speech function with fallback
def text_to_speech(text):
    if AUDIO_AVAILABLE:
        try:
            tts = gTTS(text=text, lang="en")
            fp = io.BytesIO()
            tts.write_to_fp(fp)
            fp.seek(0)
            return fp
        except Exception as e:
            print(f"Error with text-to-speech: {e}")
            print(text)
            return None
    else:
        print(text)
        return None


# Modified play_audio function with fallback
def play_audio(audio_fp):
    if AUDIO_AVAILABLE and audio_fp:
        try:
            pygame.mixer.init()
            pygame.mixer.music.load(audio_fp)
            pygame.mixer.music.play()
            while pygame.mixer.music.get_busy():
                pygame.time.Clock().tick(10)
        except Exception as e:
            print(f"Error playing audio: {e}")


# Function to convert speech to text
# Modified speech-to-text function with fallback
def speech_to_text():
    if AUDIO_AVAILABLE:
        try:
            r = sr.Recognizer()
            with sr.Microphone() as source:
                print("Listening...")
                audio = r.listen(source)
                try:
                    text = r.recognize_google(audio)
                    return text
                except sr.UnknownValueError:
                    return "Sorry, I couldn't understand that."
                except sr.RequestError:
                    return "Sorry, there was an error processing your speech."
        except Exception as e:
            print(f"Error with speech recognition: {e}")
            return input("Please type your response: ")
    else:
        return input("Please type your response: ")


# Function to generate interview questions
def generate_interview_questions(job_posting, resume, num_questions=5):
    prompt = PromptTemplate.from_template(
        """
        ### Job Posting:
        {job_posting}

        ### Candidate's Resume:
        {resume}

        ### Instruction:
        Based on the job posting and the candidate's resume, generate {num_questions} interview questions that:
        1. Assess the candidate's fit for the specific role
        2. Evaluate their relevant skills and experience
        3. Explore any potential gaps between the job requirements and the candidate's background
        4. Probe for examples of past work or projects related to the job
        5. Gauge the candidate's interest and understanding of the company/role

        Return the questions in a JSON format with keys 'question1', 'question2', etc.

        ### Questions:
        """
    )

    chain = prompt | llm | JsonOutputParser()
    questions = chain.invoke(
        input={
            "job_posting": job_posting,
            "resume": resume,
            "num_questions": num_questions,
        }
    )

    return list(questions.values())


# Function to conduct the interview
# Modified conduct_interview function
def conduct_interview(
    job_posting,
    resume,
    question_container,
    status_container,
    response_container,
    history_container,
):
    questions = generate_interview_questions(job_posting, resume)
    client = chromadb.PersistentClient("vectorstore")
    collection = client.get_or_create_collection(name="interview_responses")

    responses = []

    # Custom CSS for highlighting current question
    highlight_css = """
        <style>
            .current-question {
                background-color: #f0f7ff;
                border-left: 5px solid #0066cc;
                padding: 1rem;
                margin: 1rem 0;
                border-radius: 5px;
                animation: fadeIn 0.5s;
                color: #000000;
            }

            .past-question {
                padding: 1rem;
                margin: 1rem 0;
                border-left: 5px solid #e0e0e0;
                color: #ffffff;
            }

            @keyframes fadeIn {
                from { opacity: 0; }
                to { opacity: 1; }
            }
        </style>
    """

    st.markdown(highlight_css, unsafe_allow_html=True)

    for i, question in enumerate(questions, 1):
        # Display the current question with highlighting
        question_container.markdown(
            f"""
            <div class="current-question">
                <h3>Question {i}/{len(questions)}:</h3>
                <p>🗣️ {question}</p>
            </div>
            """,
            unsafe_allow_html=True,
        )

        # Convert question to speech and play it
        question_audio = text_to_speech(question)
        play_audio(question_audio)

        # Update status to show we're listening
        status_container.markdown("🎤 Listening for your response...")

        # Get the candidate's response
        response = speech_to_text()

        # Display the response
        response_container.markdown("### Your Response:")
        response_container.markdown(f"💬 {response}")

        # Add to history with non-highlighted style
        with history_container:
            st.markdown(
                f"""
                <div class="past-question">
                    <h4>Question {i}:</h4>
                    <p>🗣️ {question}</p>
                    <p><strong>Your Response:</strong></p>
                    <p>💬 {response}</p>
                </div>
                """,
                unsafe_allow_html=True,
            )
            st.markdown("---")

        # Store the response in the vector database
        collection.add(
            documents=[response],
            metadatas=[{"question": question}],
            ids=[str(uuid.uuid4())],
        )

        responses.append({"question": question, "response": response})

        # Clear current question and response containers for next question
        time.sleep(2)  # Give user time to read their response
        question_container.empty()
        status_container.empty()
        response_container.empty()

    return responses


# Function to evaluate the interview
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


# Core function to process job link and resume
def process_job_and_resume(job_profile: str, resume_pdf: str):

    # Create prompt for extracting job posting info
    prompt_extract = PromptTemplate.from_template(
        f"""
        ### SCRAPED TEXT FROM WEBSITE:
        {job_profile}
        ### INSTRUCTION:
        The scraped text is from the career's page of a website.
        Your job is to extract the job posting and return them in JSON format containing the following keys: `role`,`experience`,`skills`,`description`.
        Ensure that the `skills` key contains a list of skills.
        Only return the valid JSON.
        ### VALID JSON (NO PREAMBLE):
        """
    )
    chain = prompt_extract | llm
    response = chain.invoke(input={"job_profile": job_profile})

    # Parse the job posting JSON
    json_parser = JsonOutputParser()
    try:
        json_job_posting = json_parser.parse(response.content)
    except Exception as e:
        print(f"Error parsing JSON: {e}")
        print(f"Response content: {response.content}")
        json_job_posting = {
            "role": "",
            "experience": "",
            "skills": [],
            "description": "",
        }
    print(json_job_posting)
    # Handle the case where 'skills' might not be a list
    skills = json_job_posting.get("skills", [])
    if isinstance(skills, str):
        skills = [skill.strip() for skill in skills.split(",")]
    elif not isinstance(skills, list):
        skills = []

    skills_str = ",".join(skills)
    json_job_posting_formatted = format_json(json_job_posting)

    # Read the resume from PDF
    resume = fitz.open(resume_pdf)
    resume_text = ""
    for page_num in range(len(resume)):
        page = resume.load_page(page_num)
        resume_text += page.get_text()

    # Create prompt for formatting resume
    prompt_extract_resume = PromptTemplate.from_template(
        f"""
        ### SCRAPED TEXT FROM RESUME:
        {resume_text}
        ### INSTRUCTION:
        The scraped text is from the resume of a candidate.
        Your job is to convert it into a proper resume format.
        """
    )
    chain_resume = prompt_extract_resume | llm
    response_resume = chain_resume.invoke(input={"text": resume_text})
    extracted_resume = response_resume.content

    # Final prompt to match job posting with resume
    prompt_final = PromptTemplate.from_template(
        f"""
        ### JOB POSTING:
        {json_job_posting_formatted}
        ### Candidate Resume: 
        {extracted_resume}
        ### INSTRUCTION:
        Your task is to check whether the candidate's resume matches the job posting. Highlight plus points and negative points based on the compatibility.
        Return the result of the matching, focusing on how well the candidate's qualifications align with the job requirements.
        """
    )

    chain_final = prompt_final | llm
    answer = chain_final.invoke(
        input={
            "json_job_posting": json_job_posting_formatted,
            "extracted_resume": extracted_resume,
        }
    )

    return answer.content, json_job_posting_formatted, extracted_resume