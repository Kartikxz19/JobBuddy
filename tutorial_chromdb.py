import chromadb
client = chromadb.Client()
collection = client.create_collection(name="my_collection")

collection.add(
    documents=[
        "This document is about New York",
        "This document is about New Delhi",
    ],
    ids = ['id1','id2'],
    metadatas=[
        {"url":"https://en.wikipedia.org/wiki/New_York_City"},
        {"url":"https://en.wikipedia.org/wiki/New_Delhi"}
    ]
)

results =collection.query(
    query_texts=['Query is about Brooklyn Bridge'],
    n_results=2
)
print(results)
#to delete all documents
#collection.delete(ids=all_docs['ids'])
