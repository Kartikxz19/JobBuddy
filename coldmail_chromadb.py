import uuid

import pandas as pd
import chromadb
df = pd.read_csv("interviewV3.csv")
client = chromadb.PersistentClient('vectorstore')
collection = client.get_or_create_collection(name="interview")

if not collection.count():
    for _, row in df.iterrows():
        collection.add(documents=row["Skills"],
                       metadatas={"Ques":row["Questions"]},
                       ids=[str(uuid.uuid4())]
                       )
        print(row)
links = collection.query(query_texts=["Experience in React"],n_results=5)
print(links)