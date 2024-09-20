#TO TEST OUT WEB CRAWLING AND EXPERIMENT WITH GROQ
from langchain_community.document_loaders import WebBaseLoader
from langchain_groq import ChatGroq
from langchain_core.prompts import PromptTemplate
from googlesearch import search
llm = ChatGroq(
    model="llama-3.1-70b-versatile",
    temperature=0,
    groq_api_key="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"
)
query = "Amazon interview questions"
top_links=[]
for url in search(query,num_results=3):
    top_links.append(url)

documents = []
for url in top_links:
    try:
        loader = WebBaseLoader([url])
        documents.extend(loader.load())
    except:
        print(f"Error Loading {url}")
print(documents)