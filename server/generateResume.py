import json
import os
import re
import subprocess
from pdflatex import PDFLaTeX
from datetime import datetime
from dotenv import load_dotenv
from langchain_groq import ChatGroq

# Load environment variables
load_dotenv()

# Initialize LLM
llm = ChatGroq(
    model="llama-3.1-70b-versatile",
    temperature=0,
    groq_api_key=os.getenv("GROQ_API_KEY"),
)

# Sample job description
JOB_DESCRIPTION = """
Senior Full Stack Developer position at a fast-growing tech company.
Requirements:
- 5+ years experience in full-stack development
- Strong expertise in Python, React, and SQL
- Experience with microservices architecture
- Background in e-commerce or financial technology
- Track record of leading development teams
- Excellence in system design and optimization
"""

# Candidate data
CANDIDATE_DATA = {
    "skills": [
        {"skill": "Python", "level": 4},
        {"skill": "React", "level": 3},
        {"skill": "SQL", "level": 4},
        {"skill": "Docker", "level": 2},
    ],
    "projects": [
        {
            "name": "E-commerce Platform",
            "techStack": "React, Node.js, MongoDB",
            "demoLink": "https://github.com/username/ecommerce",
            "startDate": {"month": 3, "year": 2023},
            "endDate": {"month": 8, "year": 2023},
            "description": "Developed a full-stack e-commerce platform with features including user authentication, product catalog, shopping cart, and payment integration.",
        },
        {
            "name": "Task Management App",
            "techStack": "Python, Flask, PostgreSQL",
            "demoLink": "https://github.com/username/task-manager",
            "startDate": {"month": 9, "year": 2023},
            "endDate": {"month": 11, "year": 2023},
            "description": "Built a collaborative task management application with real-time updates and team workspace features.",
        },
    ],
    "experience": [
        {
            "title": "Senior Software Engineer",
            "company": "Tech Solutions Inc.",
            "startDate": {"month": 6, "year": 2021},
            "endDate": {"month": 11, "year": 2023},
            "description": "Led development team of 5 engineers, implemented microservices architecture, reduced deployment time by 40%.",
        },
        {
            "title": "Software Developer",
            "company": "Digital Innovations Corp",
            "startDate": {"month": 1, "year": 2019},
            "endDate": {"month": 5, "year": 2021},
            "description": "Developed and maintained multiple web applications, implemented CI/CD pipeline, mentored junior developers.",
        },
    ],
    "achievements": [
        {
            "title": "Best Innovation Award 2023",
            "description": "Received company-wide recognition for developing an AI-powered customer service automation system.",
        },
        {
            "title": "Conference Speaker",
            "description": "Presented 'Modern Web Architecture Patterns' at TechCon 2022, audience of 500+ developers.",
        },
    ],
    "education": [
        {
            "institution": "University of Technology",
            "degree": "Master of Computer Science",
            "startDate": {"month": 9, "year": 2016},
            "endDate": {"month": 6, "year": 2018},
        },
        {
            "institution": "State University",
            "degree": "Bachelor of Computer Science",
            "startDate": {"month": 9, "year": 2012},
            "endDate": {"month": 6, "year": 2016},
        },
    ],
    "profileLinks": [
        {"platform": "LinkedIn", "url": "https://linkedin.com/in/username"},
        {"platform": "GitHub", "url": "https://github.com/username"},
        {"platform": "Portfolio", "url": "https://username.dev"},
    ],
    "contactDetails": [
        {"type": "email", "value": "user@example.com"},
        {"type": "phone", "value": "+1234567890"},
        {"type": "location", "value": "San Francisco, CA"},
    ],
}


def enhance_all_descriptions(candidate_data, job_description):
    """Make a single AI call to enhance all descriptions at once."""
    prompt = f"""
    Given the following job description and candidate's experiences/projects, rewrite all descriptions 
    to better match the job requirements. For each experience and project, provide exactly 3 bullet points 
    that highlight relevant skills and quantifiable achievements. Return the response in JSON format.
    
    Job Description:
    {job_description}
    
    Experiences and Projects to enhance:
    {json.dumps({'experience': candidate_data['experience'], 'projects': candidate_data['projects']}, indent=2)}
    
    Return the enhanced descriptions in the following JSON format:
    {{
        "experience": [
            {{
                "title": "Job Title",
                "points": ["point1", "point2", "point3"]
            }}
        ],
        "projects": [
            {{
                "name": "Project Name",
                "points": ["point1", "point2", "point3"]
            }}
        ]
    }}
    """

    response = llm.invoke(prompt)

    # Extract JSON data from response using regex to capture text between the first and last curly braces
    json_match = re.search(r"\{.*\}", response.content, re.DOTALL)
    if json_match:
        json_data = json_match.group(0)
        try:
            enhanced_data = json.loads(json_data)
            return enhanced_data
        except json.JSONDecodeError:
            print("Error parsing AI response. Using original descriptions.")
            return None
    else:
        print("JSON data not found in the AI response.")
        return None


def format_date(date_dict):
    """Convert date dictionary to formatted string."""
    months = [
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    ]
    return f"{months[date_dict['month']-1]} {date_dict['year']}"

def generate_latex(candidate_data, enhanced_data):
    """Generate LaTeX content from candidate data."""
    latex_content = r"""
    \documentclass[a4paper,13pt]{article}
    \usepackage[scale=0.92]{geometry}
    \usepackage[utf8]{inputenc}
    \usepackage{enumitem}
    \usepackage{hyperref}
    \usepackage{parskip}
    \usepackage{titlesec}
    \definecolor{linkcolour}{rgb}{0,0.2,0.6}
    \hypersetup{colorlinks,breaklinks,urlcolor=linkcolour,linkcolor=linkcolour}
    
    \titlespacing{\section}{0pt}{6pt}{3pt}
    \titleformat{\section}{\Large\scshape\raggedright}{}{0em}{}[\titlerule]
    
    \begin{document}
    \pagestyle{empty}

    % Header with name and contact details
    \parbox{\dimexpr\linewidth\relax}{
    \begin{tabularx}{\linewidth}{@{} C @{}}
    \Huge{{""" + candidate_data["contactDetails"][0]["value"] + r"""}} \\[7.5pt]
    \small{
    """
    
    # Adding contact details with plain text and links
    for contact in candidate_data["contactDetails"][1:]:
        value = contact["value"]
        if "email" in contact["type"].lower():
            value = f"\\href{{mailto:{value}}}{{{value}}}"
        elif "phone" in contact["type"].lower():
            value = f"Phone: {value}"
        latex_content += f"{value} \\ $ | $ \\ "

    # Adding profile links as plain text links
    for link in candidate_data["profileLinks"]:
        platform_name = link["platform"].capitalize()
        latex_content += f"\\href{{{link['url']}}}{{{platform_name}: {link['url']}}} \\ $ | $ \\ "
    latex_content = latex_content.rstrip(" $ | $ \\ ") + "} \\\\ \n\\end{tabularx}\n}\n\n"

    # Skills Section
    latex_content += "\\section*{Skills}\n" + ", ".join(
        [f"{skill['skill']} ({skill['level']}/5)" for skill in candidate_data["skills"]]
    ) + "\n\n"

    # Professional Experience Section
    latex_content += "\\section*{Professional Experience}\n"
    for i, exp in enumerate(candidate_data["experience"]):
        latex_content += f"\\subsection*{{{exp['title']} - {exp['company']}}}\n"
        latex_content += f"{format_date(exp['startDate'])} - {format_date(exp['endDate'])}\n\n"
        if enhanced_data and i < len(enhanced_data["experience"]):
            latex_content += "\\begin{itemize}[itemsep=1pt]\n"
            for point in enhanced_data["experience"][i]["points"]:
                latex_content += f"  \\item {point}\n"
            latex_content += "\\end{itemize}\n"

    # Projects Section
    latex_content += "\\section*{Projects}\n"
    for i, project in enumerate(candidate_data["projects"]):
        latex_content += f"\\subsection*{{{project['name']}}} ({project['techStack']})\n"
        latex_content += f"{format_date(project['startDate'])} - {format_date(project['endDate'])}\n\n"
        if enhanced_data and i < len(enhanced_data["projects"]):
            latex_content += "\\begin{itemize}[itemsep=1pt]\n"
            for point in enhanced_data["projects"][i]["points"]:
                latex_content += f"  \\item {point}\n"
            latex_content += "\\end{itemize}\n"
        if "demoLink" in project:
            latex_content += f"\\href{{{project['demoLink']}}}{{Demo}}\n"

    # Education Section
    latex_content += "\\section*{Education}\n"
    for edu in candidate_data["education"]:
        latex_content += f"\\subsection*{{{edu['degree']} - {edu['institution']}}}\n"
        latex_content += f"{format_date(edu['startDate'])} - {format_date(edu['endDate'])}\n\n"

    # Achievements Section
    latex_content += "\\section*{Achievements}\n"
    for achievement in candidate_data["achievements"]:
        latex_content += f"\\subsection*{{{achievement['title']}}}\n"
        latex_content += f"{achievement['description']}\n\n"

    latex_content += "\\end{document}\n"
    return latex_content


def save_latex_to_file(latex_content, filepath):
    """Save LaTeX content to a .tex file."""
    with open(filepath, "w") as f:
        f.write(latex_content)


def convert_latex_to_pdf(latex_filepath, pdf_filepath):
    """Convert a LaTeX file to PDF using pdflatex Python module."""
    try:
        # Initialize PDFLaTeX with the .tex file
        pdfl = PDFLaTeX.from_texfile(latex_filepath)
        
        # Create the PDF, keeping the log file if desired
        pdf, log, completed_process = pdfl.create_pdf(keep_pdf_file=True, keep_log_file=True)
        
        # Save the PDF to the specified filepath
        with open(pdf_filepath, 'wb') as f:
            f.write(pdf)
        
        print(f"PDF created successfully: {pdf_filepath}")
    except Exception as e:
        print(f"An error occurred: {e}")


def main():
    # Create output directory if it doesn't exist
    os.makedirs("generated_resumes", exist_ok=True)

    # # Generate enhanced descriptions with a single AI call
    # print("Enhancing descriptions...")
    # enhanced_data = enhance_all_descriptions(CANDIDATE_DATA, JOB_DESCRIPTION)

    # # Generate LaTeX content
    # print("Generating LaTeX content...")
    # latex_content = generate_latex(CANDIDATE_DATA, enhanced_data)
    latex_filepath = "generated_resumes/generated_resume.tex"
    # save_latex_to_file(latex_content, latex_filepath)

    # Convert LaTeX to PDF
    pdf_filepath = "generated_resumes/generated_resume.pdf"
    print("Generating PDF...")
    convert_latex_to_pdf(latex_filepath, pdf_filepath)
    print(f"Resume generated successfully! Saved as: {pdf_filepath}")


if __name__ == "__main__":
    main()
