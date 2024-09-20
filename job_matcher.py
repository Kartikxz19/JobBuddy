# job_matcher.py

from langchain_groq import ChatGroq
from langchain_community.document_loaders import WebBaseLoader
from langchain_core.prompts import PromptTemplate
from langchain_core.output_parsers import JsonOutputParser
import uuid
import pandas as pd
import chromadb
import fitz
import os
from dotenv import load_dotenv
load_dotenv()
llm = ChatGroq(
    model="llama-3.1-70b-versatile",
    temperature=0,
    groq_api_key=os.getenv("GROQ_API_KEY")
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
    client = chromadb.PersistentClient('vectorstore')
    collection = client.get_or_create_collection(name="interview")

    if not collection.count():
        for _, row in df.iterrows():
            collection.add(documents=row["Skills"],
                           metadatas={"Ques": row["Questions"]},
                           ids=[str(uuid.uuid4())]
                           )
    data = collection.query(query_texts=[skills], n_results=10)
    questions = [metadata['Ques'] for metadata in data['metadatas'][0]]
    return questions

# Core function to process job link and resume
def process_job_and_resume(job_link: str, resume_pdf: str):
    # Load job posting from the link
    loader = WebBaseLoader(job_link)
    page_data = loader.load().pop().page_content

    # Create prompt for extracting job posting info
    prompt_extract = PromptTemplate.from_template(
        f"""
        ### SCRAPED TEXT FROM WEBSITE:
        {page_data}
        ### INSTRUCTION:
        The scraped text is from the career's page of a website.
        Your job is to extract the job posting and return them in JSON format containing the following keys: `role`,`experience`,`skills`,`description`.
        Only return the valid JSON.
        ### VALID JSON (NO PREAMBLE):
        """
    )
    chain = prompt_extract | llm
    response = chain.invoke(input={"page_data": page_data})

    # Parse the job posting JSON
    json_parser = JsonOutputParser()
    json_job_posting = json_parser.parse(response.content)
    skills = ",".join(json_job_posting['skills'])
    json_job_posting_formatted = format_json(json_job_posting)

    # Get interview questions
    questions = get_questions(skills)

    # Read the resume from PDF
    resume = fitz.open(resume_pdf)
    text = ""
    for page_num in range(len(resume)):
        page = resume.load_page(page_num)
        text += page.get_text()

    # Create prompt for formatting resume
    prompt_extract_resume = PromptTemplate.from_template(
        f"""
        ### SCRAPED TEXT FROM RESUME:
        {text}
        ### INSTRUCTION:
        The scraped text is from the resume of a candidate.
        Your job is to convert it into a proper resume.
        """
    )
    chain_resume = prompt_extract_resume | llm
    response_resume = chain_resume.invoke(input={"text": text})
    extracted_resume = response_resume.content

    # Final prompt to match job posting with resume
    prompt_final = PromptTemplate.from_template(
        f"""
        ### JOB POSTING:
        {json_job_posting_formatted}
        ### Candidate Resume: 
        {extracted_resume}
        ### JOB POSTING QUESTIONS:
        {questions}
        ### INSTRUCTION:
        Your task is to check whether the candidate's resume matches the job posting. Highlight plus points and negative points based on the compatibility.
        Return the result of the matching along with the JOB POSTING QUESTIONS that can help the candidate prepare for interviews.
        """
    )

    chain_final = prompt_final | llm
    answer = chain_final.invoke(input={"json_job_posting": json_job_posting_formatted, "extracted_resume": extracted_resume, "questions": str(questions)})

    return answer.content
