import chromadb
from typing import Dict, List
import streamlit as st
import pandas as pd
import json
class FlashcardSystem:
    def __init__(self):
        self.client = chromadb.PersistentClient("vectorstore")
        self.collection = self.client.get_collection(name="interview_questions")

    def identify_skill_gaps(self, job_skills: List[str], resume_text: str) -> Dict[str, str]:
        """
        Identifies skills that need improvement based on job requirements and resume
        """
        skill_status = {}
        for skill in job_skills:
            # Simple text matching - could be enhanced with NLP
            if skill.lower() in resume_text.lower():
                skill_status[skill] = "needs_improvement"
            else:
                skill_status[skill] = "missing"
        return skill_status

    def get_flashcards_for_skill(self, skill: str, limit: int = 5) -> List[Dict]:
        """
        Fetches relevant Q&As from ChromaDB for a specific skill
        """
        results = self.collection.query(
            query_texts=[skill],
            n_results=limit
        )

        flashcards = []
        if results['documents']:
            for doc, metadata in zip(results['documents'][0], results['metadatas'][0]):
                flashcard = {
                    'skill': metadata['Section'],
                    'question': doc,
                    'answer': metadata['Answer']
                }
                flashcards.append(flashcard)
        return flashcards

    def generate_study_plan(self, job_posting: str | dict, resume_text: str) -> Dict[str, List[Dict]]:
        """
        Creates a complete study plan based on job requirements and resume
        """
        # Parse job posting JSON only if it's a string
        if isinstance(job_posting, str):
            try:
                job_posting = json.loads(job_posting)
            except json.JSONDecodeError:
                # If it fails to parse as JSON, assume it's already formatted text
                pass

        # Ensure job_posting is a dictionary
        if not isinstance(job_posting, dict):
            raise ValueError("job_posting must be either a JSON string or a dictionary")

        # Extract skills from job posting
        job_skills = job_posting.get('skills', [])

        # Identify gaps
        skill_gaps = self.identify_skill_gaps(job_skills, resume_text)

        # Generate flashcards for each skill that needs improvement
        study_plan = []
        for skill, status in skill_gaps.items():
            flashcards = self.get_flashcards_for_skill(skill)
            if flashcards:  # Only include skills with available flashcards
                study_plan.append({
                    'skill': skill,
                    'status': status,
                    'flashcards': flashcards
                })

        return study_plan


def display_flashcards(study_plan: Dict):
    """
    Displays flashcards in Streamlit interface
    """
    st.header("📚 Study Plan and Flashcards")

    # Display overall progress
    total_skills = len(study_plan)
    missing_skills = sum(1 for skill_data in study_plan.values()
                         if skill_data['status'] == 'missing')

    col1, col2 = st.columns(2)
    with col1:
        st.metric("Total Skills to Study", total_skills)
    with col2:
        st.metric("Missing Skills", missing_skills)

    # Skill selection
    skills = list(study_plan.keys())
    if not skills:
        st.warning("No flashcards available for the required skills.")
        return

    selected_skill = st.selectbox(
        "Select a skill to study:",
        skills,
        format_func=lambda x: f"{x} ({'Missing' if study_plan[x]['status'] == 'missing' else 'Needs Improvement'})"
    )

    # Initialize session state for flashcard index if not exists
    if 'flashcard_index' not in st.session_state:
        st.session_state.flashcard_index = 0
    if 'show_answer' not in st.session_state:
        st.session_state.show_answer = False

    # Get flashcards for selected skill
    flashcards = study_plan[selected_skill]['flashcards']

    # Ensure index is within bounds
    if len(flashcards) == 0:
        return

    current_card = flashcards[st.session_state.flashcard_index]

    # Display flashcard
    st.markdown("---")

    card_container = st.container()

    with card_container:
        st.markdown(f"### Question {st.session_state.flashcard_index + 1}/{len(flashcards)}")
        st.markdown(f"**Q: {current_card['question']}**")

        if st.button("Show Answer" if not st.session_state.show_answer else "Hide Answer"):
            st.session_state.show_answer = not st.session_state.show_answer

        if st.session_state.show_answer:
            st.markdown("**Answer:**")
            st.markdown(current_card['answer'])

    # Navigation buttons
    col1, col2, col3 = st.columns([1, 2, 1])

    with col1:
        if st.button("Previous") and st.session_state.flashcard_index > 0:
            st.session_state.flashcard_index -= 1
            st.experimental_rerun()

    with col3:
        if st.button("Next") and st.session_state.flashcard_index < len(flashcards) - 1:
            st.session_state.flashcard_index += 1
            st.experimental_rerun()