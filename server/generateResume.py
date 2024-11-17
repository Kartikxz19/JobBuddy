import json
import os
import re
import subprocess
import logging
import sys
from pdflatex import PDFLaTeX
from datetime import datetime
from dotenv import load_dotenv
from langchain_groq import ChatGroq
from typing import Dict, Optional
from pathlib import Path

# Load environment variables
load_dotenv()

# Initialize LLM
llm = ChatGroq(
    model="llama-3.1-70b-versatile",
    temperature=0,
    groq_api_key=os.getenv("GROQ_API_KEY"),
)
# Configure logging
logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)
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

ENHANCED_DATA = {
    "experience": [
        {
            "title": "Senior Software Engineer",
            "points": [
                "Led a team of 5 engineers, driving full-stack development projects and fostering a culture of collaboration and innovation.",
                "Designed and implemented microservices architecture, resulting in a 40% reduction in deployment time and improved system scalability.",
                "Mentored junior engineers, providing guidance on best practices and code reviews to ensure high-quality code delivery."
            ]
        },
        {
            "title": "Software Developer",
            "points": [
                "Developed and maintained multiple web applications using React, Node.js, and MongoDB, ensuring high performance and reliability.",
                "Implemented CI/CD pipeline, automating testing and deployment processes to reduce manual errors and improve delivery speed.",
                "Collaborated with cross-functional teams to identify and prioritize project requirements, ensuring alignment with business objectives."
            ]
        }
    ],
    "projects": [
        {
            "name": "E-commerce Platform",
            "points": [
                "Designed and developed a full-stack e-commerce platform using React, Node.js, and MongoDB, featuring user authentication, product catalog, shopping cart, and payment integration.",
                "Implemented a scalable and secure payment gateway, ensuring seamless transactions and protecting sensitive user data.",
                "Optimized platform performance, achieving a 30% reduction in page load times and improving overall user experience."
            ]
        },
        {
            "name": "Task Management App",
            "points": [
                "Built a collaborative task management application using Python, Flask, and PostgreSQL, featuring real-time updates and team workspace functionality.",
                "Designed and implemented a robust database schema, ensuring data consistency and supporting high-volume data storage.",
                "Developed a RESTful API, providing a flexible and scalable interface for integrating with third-party services and applications."
            ]
        }
    ]
}

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
    json_match = re.search(r"\{.*\}", response.content, re.DOTALL)
    if json_match:
        json_data = json_match.group(0)
        print(json_data)
        try:
            enhanced_data = json.loads(json_data)
            return enhanced_data
        except json.JSONDecodeError as e:
            print("Error parsing AI response. Using original descriptions.")
            print(str(e))
            return None
    else:
        print("JSON data not found in the AI response.")
        return None
    
def generate_latex(candidate_data, enhanced_data, candidate_name):
    """Generate LaTeX content from candidate data matching the given resume format."""
    latex_content = r"""
\documentclass[a4paper,13pt]{article}
\usepackage{url}
\usepackage{parskip}   
\RequirePackage{color}
\RequirePackage{graphicx}
\usepackage[usenames,dvipsnames]{xcolor}
\usepackage[scale=0.92]{geometry}
\usepackage{tabularx}
\usepackage{enumitem}
\newcolumntype{C}{>{\centering\arraybackslash}X} 
\usepackage{supertabular}
\usepackage{tabularx}
\newlength{\fullcollw}
\setlength{\fullcollw}{0.47\textwidth}
\usepackage{titlesec}                
\usepackage{multicol}
\usepackage{multirow}
\titlespacing{\section}{0pt}{6pt}{6pt}
\titleformat{\section}{\Large\scshape\raggedright}{}{0em}{}[\titlerule]
\usepackage[style=authoryear,sorting=ynt,maxbibnames=2]{biblatex}
\usepackage[unicode,draft=false]{hyperref}
\definecolor{linkcolour}{rgb}{0,0.2,0.6}
\hypersetup{colorlinks,breaklinks,urlcolor=linkcolour,linkcolor=linkcolour}
\addbibresource{citations.bib}
\setlength\bibitemsep{0.8em}
\usepackage{fontawesome5}

\begin{document}

\pagestyle{empty} 

\parbox{\dimexpr\linewidth\relax}{
\begin{tabularx}{\linewidth}{@{} C @{}}
\Huge{""" + candidate_name + r"""} \\[7.5pt]
\small{"""
# Contact mapping with default icon for unknown types
    contact_mapping = {
        "email": (r"\faEnvelope", "mailto:"),
        "phone": (r"\faMobile", "tel:"),
        "github": (r"\faGithub", "https://github.com/"),
        "linkedin": (r"\faLinkedin", "https://linkedin.com/in/"),
        "website": (r"\faGlobe", ""),
        "portfolio": (r"\faGlobe", ""),
        "location": (r"\faMapMarker", ""),  # Added location type
    }
    
    # Default icon for unknown types
    default_icon = r"\faLink"
    
    contact_items = []
    
    # Process contact details
    for contact in candidate_data["contactDetails"]:
        contact_type = contact["type"].lower()
        value = contact["value"]
        username = value.split("/")[-1] if "/" in value else value
        
        if contact_type in contact_mapping:
            icon, prefix = contact_mapping[contact_type]
            contact_items.append(
                f"\\href{{{prefix}{username}}}{{\\raisebox{{-0.05\\height}}{{{icon}}}\\ {username}}}"
            )
        else:
            # For unknown contact types, use default icon without prefix
            contact_items.append(
                f"\\raisebox{{-0.05\\height}}{{{default_icon}}}\\ {username}"
            )
    
    # Process profile links
    platform_to_type = {
        "LinkedIn": "linkedin",
        "GitHub": "github",
        "Portfolio": "portfolio"
    }
    
    for profile in candidate_data["profileLinks"]:
        platform = profile["platform"]
        url = profile["url"]
        username = url.split("/")[-1]
        
        contact_type = platform_to_type.get(platform, "").lower()
        if contact_type in contact_mapping:
            icon, _ = contact_mapping[contact_type]
        else:
            # For unknown platforms, use default icon
            icon = default_icon
            
        contact_items.append(
            f"\\href{{{url}}}{{\\raisebox{{-0.05\\height}}{{{icon}}}\\ {username}}}"
        )
    
    latex_content += " \\ $|$ \\ ".join(contact_items)
    latex_content += r"""} \\
\end{tabularx}
}"""

    # Experience Section
    latex_content += r"\section{Experiences}" + "\n\n"
    for exp in candidate_data["experience"]:
        latex_content += r"\begin{tabularx}{\linewidth}{ @{}l r@{} }" + "\n"
        end_date = "Present" if exp["endDate"]["month"] is None else f"{exp['endDate']['month']}/{exp['endDate']['year']}"
        latex_content += f"\\textbf{{{exp['company']}}} \\small{{$|$ {exp['title']}}} & \\hfill {{{exp['startDate']['month']}/{exp['startDate']['year']} - {end_date}}} \\\\[3.75pt]\n"
        latex_content += r"\multicolumn{2}{@{}X@{}}{" + "\n"
        latex_content += r"\begin{itemize}[itemsep=1pt, parsep=0pt]" + "\n"
        
        # Find matching enhanced experience points
        enhanced_points = []
        for enhanced_exp in enhanced_data["experience"]:
            if enhanced_exp["title"] == exp["title"]:
                enhanced_points = enhanced_exp["points"]
                break
        
        # Use enhanced points if available, otherwise use description
        points = enhanced_points if enhanced_points else [exp["description"]]
        for point in points:
            latex_content += f"    \\item {point}\n"
        
        latex_content += r"\end{itemize}" + "\n"
        latex_content += r"}  \\" + "\n"
        latex_content += r"\end{tabularx}" + "\n\n"

    # Projects Section
    latex_content += r"\section{Projects}" + "\n\n"
    for project in candidate_data["projects"]:
        latex_content += r"\begin{tabularx}{\linewidth}{ @{}l r@{} }" + "\n"
        latex_content += f"\\textbf{{{project['name']}}} \\small{{$|$ {project['techStack']}}} & \\hfill \\href{{{project['demoLink']}}}{{GitHub Repo}} \\\\[3.75pt]\n"
        latex_content += r"\multicolumn{2}{@{}X@{}}{" + "\n"
        latex_content += r"\begin{itemize}[itemsep=1pt, parsep=0pt]" + "\n"
        
        # Find matching enhanced project points
        enhanced_points = []
        for enhanced_proj in enhanced_data["projects"]:
            if enhanced_proj["name"] == project["name"]:
                enhanced_points = enhanced_proj["points"]
                break
            
        # Use enhanced points if available, otherwise use description
        points = enhanced_points if enhanced_points else [project["description"]]
        for point in points:
            latex_content += f"    \\item {point}\n"
            
        latex_content += r"\end{itemize}" + "\n"
        latex_content += r"}  \\" + "\n"
        latex_content += r"\end{tabularx}" + "\n\n"

    # Education Section
    latex_content += r"\section{Education}" + "\n"
    latex_content += r"\begin{itemize}[leftmargin=0.0in, itemsep=1pt, parsep=0pt, label={}]" + "\n"
    for edu in candidate_data["education"]:
        latex_content += r"\vspace{-2pt}\item" + "\n"
        latex_content += r"    \begin{tabular*}{1.0\textwidth}[t]{l@{\extracolsep{\fill}}r}" + "\n"
        latex_content += f"      \\textbf{{{edu['institution']}}} & \\textbf{{\\small {edu['startDate']['year']}-{edu['endDate']['year']}}} \\\\\n"
        latex_content += f"      \\textit{{\\small {edu['degree']}}} \\\\\n"
        latex_content += r"    \end{tabular*}\vspace{-5pt}" + "\n"
    latex_content += r"\end{itemize}" + "\n\n"

    # Technical Skills Section
    latex_content += r"\section{Technical Skills}" + "\n"
    skills_text = ", ".join(skill["skill"] for skill in candidate_data["skills"])
    latex_content += f"\\normalsize{{{skills_text}}}\n\n"

    # Achievements Section
    latex_content += r"\section{Achievements}" + "\n"
    for achievement in candidate_data["achievements"]:
        latex_content += f"\\textbf{{{achievement['title']}}} \\\\\n"
        if "description" in achievement:
            latex_content += f"\\textit{{\\small{{{achievement['description']}}}}}  \\\\[5pt]\n"

    latex_content += r"\end{document}"
    return latex_content


def save_latex_to_file(latex_content, filepath):
    """Save LaTeX content to a .tex file."""
    with open(filepath, "w") as f:
        f.write(latex_content)


class PDFConversionError(Exception):
    """Custom exception for PDF conversion errors"""

    pass


def convert_latex_to_pdf(latex_filepath: str, pdf_filepath: str) -> bool:
    """
    Convert LaTeX to PDF using direct pdflatex command with proper error handling.
    Returns True if the PDF was generated successfully, False otherwise.
    """
    try:
        # Convert to absolute paths
        latex_filepath = os.path.abspath(latex_filepath)
        pdf_filepath = os.path.abspath(pdf_filepath)

        # Get the directory containing the .tex file
        tex_dir = os.path.dirname(latex_filepath)
        tex_filename = os.path.basename(latex_filepath)

        # Ensure the directory exists
        os.makedirs(tex_dir, exist_ok=True)

        # Change to the directory containing the .tex file
        original_dir = os.getcwd()
        os.chdir(tex_dir)

        try:
            # Run pdflatex twice to ensure proper rendering of all elements
            for _ in range(2):
                result = subprocess.run(
                    ["pdflatex", "-interaction=nonstopmode", tex_filename],
                    stdout=subprocess.PIPE,
                    stderr=subprocess.PIPE,
                    check=True,
                )

            # Move the generated PDF to the desired location if they're different
            generated_pdf = os.path.splitext(tex_filename)[0] + ".pdf"
            if os.path.abspath(generated_pdf) != pdf_filepath and os.path.exists(
                generated_pdf
            ):
                os.replace(generated_pdf, pdf_filepath)

            # Clean up auxiliary files
            for ext in [".aux", ".log", ".out"]:
                aux_file = os.path.splitext(tex_filename)[0] + ext
                if os.path.exists(aux_file):
                    os.remove(aux_file)

            return True

        finally:
            # Always change back to the original directory
            os.chdir(original_dir)

    except subprocess.CalledProcessError as e:
        error_msg = f"LaTeX compilation failed with return code {e.returncode}"
        logger.error(error_msg)
        logger.error(f"stdout: {e.stdout.decode('utf-8', errors='ignore')}")
        logger.error(f"stderr: {e.stderr.decode('utf-8', errors='ignore')}")
        return False
    except FileNotFoundError:
        error_msg = (
            "pdflatex command not found. Please install TeX Live or MiKTeX.\n"
            "Windows: Install MiKTeX from https://miktex.org/download\n"
            "Linux: Run 'sudo apt-get install texlive-full'\n"
            "macOS: Install MacTeX from https://tug.org/mactex/"
        )
        logger.error(error_msg)
        return False
    except Exception as e:
        error_msg = f"PDF conversion failed: {str(e)}"
        logger.error(error_msg)
        return False


def main():
    try:
        # Create output directory if it doesn't exist
        output_dir = Path("generated_resumes")
        output_dir.mkdir(exist_ok=True)

        # Generate enhanced descriptions with a single AI call
        # logger.info("Enhancing descriptions...")
        enhanced_data = enhance_all_descriptions(CANDIDATE_DATA, JOB_DESCRIPTION)

        # Generate LaTeX content
        logger.info("Generating LaTeX content...")
        latex_content = generate_latex(CANDIDATE_DATA, ENHANCED_DATA, "Hardik Jain")
        latex_filepath = output_dir / "generated_resume.tex"

        # # Save LaTeX content
        latex_filepath.write_text(latex_content, encoding='utf-8')
        logger.info(f"LaTeX file saved: {latex_filepath}")

        # Convert to PDF
        pdf_filepath = output_dir / "generated_resume.pdf"
        logger.info("Converting to PDF...")
        if convert_latex_to_pdf(str(latex_filepath), str(pdf_filepath)):
            logger.info(f"Resume generated successfully! Saved as: {pdf_filepath}")
        else:
            logger.error(
                "PDF conversion failed. Please check the LaTeX content and your LaTeX installation."
            )
            # Return a non-zero exit code to indicate failure
            sys.exit(1)

    except Exception as e:
        logger.error(f"An unexpected error occurred: {str(e)}")
        sys.exit(1)


if __name__ == "__main__":
    main()
