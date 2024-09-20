# app.py

import streamlit as st
from job_matcher import process_job_and_resume
import os

st.title("Stage 1: Are you suitable for the job?")

# Input for the job link
job_link = st.text_input("Enter the Job Link:")

# Input for the resume file
resume_file = st.file_uploader("Upload your Resume (PDF only):", type=["pdf"])

# Create the 'temp' directory if it doesn't exist
if not os.path.exists("temp"):
    os.makedirs("temp")

# Process the data when both inputs are provided
if st.button("Submit") and job_link and resume_file:
    # Save the uploaded resume temporarily
    resume_path = os.path.join("temp", resume_file.name)
    with open(resume_path, "wb") as f:
        f.write(resume_file.getbuffer())

    # Run the process_job_and_resume function and capture the result
    with st.spinner("Please wait, analyzing your resume and job posting..."):
        output = process_job_and_resume(job_link, resume_path)

    # Display the result as markdown
    st.markdown("### Job Matching Results:")
    st.markdown(output)

    # Clean up the temporary file
    os.remove(resume_path)

#https://www.glassdoor.co.in/job-listing/reactjs-developer-reizend-JV_KO0,17_KE18,25.htm?jl=1009375703917&src=GD_JOB_AD&uido=AA2BF2256E104CE3C40DBC76A13D44CE&ao=1136043&jrtk=5-yul1-0-1i7u73b92i95b801-c7b574cece3c1e89&cs=1_edcbb4c5&s=58&t=SR&pos=101&guid=00000191fc71accc83cd0a3d91596572&jobListingId=1009375703917&ea=1&vt=w&cb=1726517194184&ctt=1726517414091