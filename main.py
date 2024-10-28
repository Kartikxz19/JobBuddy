# app.py
import streamlit as st
from job_matcher import process_job_and_resume, conduct_interview, evaluate_interview
import os

st.title("Job Application and Interview Process")

# Stage 1: Resume Submission and Job Matching
st.header("Stage 1: Are you suitable for the job?")

# Initialize session state for job_profile if it doesn't exist
if 'job_profile' not in st.session_state:
    st.session_state.job_profile = ""

# Job Profile input
job_profile = st.text_area("Enter the Job Profile:", key="job_profile")

resume_file = st.file_uploader("Upload your Resume (PDF only):", type=["pdf"])

# Create temp directory if it doesn't exist
if not os.path.exists("temp"):
    os.makedirs("temp")

# Submit button to process job profile and resume
if st.button("Submit Resume") and job_profile and resume_file:
    resume_path = os.path.join("temp", resume_file.name)
    with open(resume_path, "wb") as f:
        f.write(resume_file.getbuffer())

    # Pass job_profile to `process_job_and_resume`
    with st.spinner("Please wait, analyzing your resume and job posting..."):
        output, job_posting, resume = process_job_and_resume(job_profile, resume_path)

    st.markdown("### Job Matching Results:")
    st.markdown(output)

    os.remove(resume_path)

    # Store the job posting and resume for later use
    st.session_state['job_posting'] = job_posting
    st.session_state['resume'] = resume

    # Show the "Start Interview" button
    st.session_state['show_interview_button'] = True

# Stage 2: Interview
# app.py - Update the interview section
if st.session_state.get('show_interview_button', False):
    st.header("Stage 2: Interview")

    # Create containers for the interview UI
    question_container = st.empty()
    status_container = st.empty()
    response_container = st.empty()
    history_container = st.container()

    if st.button("Start Interview"):
        st.write("Starting the interview. Please make sure your microphone is connected and working.")

        with st.spinner("Conducting interview..."):
            responses = conduct_interview(
                st.session_state['job_posting'],
                st.session_state['resume'],
                question_container,
                status_container,
                response_container,
                history_container
            )

        st.write("Interview completed. Evaluating responses...")
        evaluation = evaluate_interview(responses, st.session_state['job_posting'], st.session_state['resume'])

        st.markdown("### Interview Evaluation:")
        st.markdown(evaluation)
