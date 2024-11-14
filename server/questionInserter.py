#questionInserter.py
#this file will help understand how Q&As are stored in the chromDB and how do you fetch those Q&As
import pandas as pd
import uuid
import chromadb

def insert_interview_questions():
    # Create a DataFrame with the React questions and answers
    data = [
        # JavaScript
        {
            "section": "JavaScript",
            "question": "What is JavaScript, and how is it used?",
            "answer": "JavaScript is a programming language primarily used to add interactive behavior to web pages, enabling features like form validation, dynamic content updates, and animations."
        },
        {
            "section": "JavaScript",
            "question": "What is the difference between '==' and '===' in JavaScript?",
            "answer": "'==' checks for equality with type conversion, whereas '===' checks for both value and type, providing a stricter comparison."
        },
        {
            "section": "JavaScript",
            "question": "What is a closure in JavaScript?",
            "answer": "A closure is a feature where an inner function has access to the outer (enclosing) function's variables even after the outer function has finished executing."
        },
        {
            "section": "JavaScript",
            "question": "What is the purpose of 'this' in JavaScript?",
            "answer": "'this' refers to the context in which the function is invoked. Its value can change depending on where it’s called and how."
        },
        {
            "section": "JavaScript",
            "question": "What is an IIFE (Immediately Invoked Function Expression) in JavaScript?",
            "answer": "An IIFE is a function that runs as soon as it is defined. It’s used to create a private scope to avoid polluting the global namespace."
        },

        # Python
        {
            "section": "Python",
            "question": "What is Python, and why is it popular?",
            "answer": "Python is a versatile, high-level programming language known for its readability, extensive libraries, and wide range of applications, including web development, data science, and automation."
        },
        {
            "section": "Python",
            "question": "What are lists and tuples in Python?",
            "answer": "Lists are mutable, ordered collections of items, while tuples are immutable and used for fixed data sets."
        },
        {
            "section": "Python",
            "question": "What is a Python dictionary?",
            "answer": "A dictionary is a collection of key-value pairs, where each key must be unique. Dictionaries are used for fast lookups."
        },
        {
            "section": "Python",
            "question": "What is a lambda function in Python?",
            "answer": "A lambda function is an anonymous, inline function defined with the keyword 'lambda'. It can take any number of arguments but has only one expression."
        },
        {
            "section": "Python",
            "question": "What is Python's 'self' keyword?",
            "answer": "'self' is used within a class to reference the current instance, allowing access to the instance's attributes and methods."
        },

        # HTML
        {
            "section": "HTML",
            "question": "What is HTML?",
            "answer": "HTML (HyperText Markup Language) is the standard language for creating and structuring content on the web."
        },
        {
            "section": "HTML",
            "question": "What are HTML tags?",
            "answer": "HTML tags are the building blocks of HTML; they define elements such as headings, paragraphs, links, and images in a web document."
        },
        {
            "section": "HTML",
            "question": "What is the purpose of the <head> and <body> tags?",
            "answer": "The <head> contains metadata about the HTML document, while the <body> contains the actual content displayed to the user."
        },
        {
            "section": "HTML",
            "question": "What is the <div> tag used for?",
            "answer": "<div> is a container element used to group together other elements, often for styling and layout purposes."
        },
        {
            "section": "HTML",
            "question": "What is the difference between <strong> and <b> tags?",
            "answer": "<strong> represents important text, often shown in bold, whereas <b> is for stylistically bold text without emphasis."
        },

        # CSS
        {
            "section": "CSS",
            "question": "What is CSS?",
            "answer": "CSS (Cascading Style Sheets) is a language used to style and layout HTML elements on a web page, controlling attributes like color, size, and positioning."
        },
        {
            "section": "CSS",
            "question": "What is the purpose of a CSS class selector?",
            "answer": "A CSS class selector is used to apply styles to elements with a specific class attribute, allowing for reusable styling across multiple elements."
        },
        {
            "section": "CSS",
            "question": "What is a CSS Flexbox?",
            "answer": "Flexbox is a CSS layout module that provides a flexible way to arrange elements within a container, offering control over alignment, direction, and spacing."
        },
        {
            "section": "CSS",
            "question": "What is the box model in CSS?",
            "answer": "The box model is the structure of an HTML element, consisting of margins, borders, padding, and the content itself, affecting the layout and spacing."
        },
        {
            "section": "CSS",
            "question": "How do you center an element in CSS?",
            "answer": "Elements can be centered using techniques like 'margin: auto', Flexbox's 'justify-content: center' and 'align-items: center', or CSS Grid's 'place-items: center'."
        },

        # Node.js
        {
            "section": "Node.js",
            "question": "What is Node.js?",
            "answer": "Node.js is a JavaScript runtime built on Chrome's V8 engine, enabling JavaScript execution on the server-side for building scalable network applications."
        },
        {
            "section": "Node.js",
            "question": "What is npm?",
            "answer": "npm (Node Package Manager) is a package manager for Node.js, used to install, manage, and share reusable code packages for JavaScript projects."
        },
        {
            "section": "Node.js",
            "question": "What is the event loop in Node.js?",
            "answer": "The event loop allows Node.js to perform non-blocking operations, handling asynchronous callbacks, making it ideal for I/O-heavy tasks."
        },
        {
            "section": "Node.js",
            "question": "What is a middleware in Express.js?",
            "answer": "Middleware functions are used in Express.js to handle requests before reaching the final route handler, enabling tasks like authentication and logging."
        },
        {
            "section": "Node.js",
            "question": "What is the difference between readFile and createReadStream in Node.js?",
            "answer": "readFile loads the entire file into memory, while createReadStream reads files in chunks, making it more efficient for large files."
        }
    ]
    data.extend([
        # SQL
        {
            "section": "SQL",
            "question": "What is SQL?",
            "answer": "SQL (Structured Query Language) is a standardized language used to interact with relational databases, including querying, updating, and managing data."
        },
        {
            "section": "SQL",
            "question": "What is the difference between INNER JOIN and OUTER JOIN?",
            "answer": "INNER JOIN returns records that have matching values in both tables, while OUTER JOIN returns all records when there is a match in either left or right table."
        },
        {
            "section": "SQL",
            "question": "What is normalization in SQL?",
            "answer": "Normalization is the process of structuring a database to reduce redundancy and dependency by dividing data into related tables."
        },
        {
            "section": "SQL",
            "question": "What are primary and foreign keys?",
            "answer": "A primary key uniquely identifies each record in a table, while a foreign key is a field in one table that references the primary key of another table."
        },
        {
            "section": "SQL",
            "question": "What is a stored procedure?",
            "answer": "A stored procedure is a precompiled collection of SQL statements stored in the database, which can be reused and executed to perform complex operations."
        },

        # Git
        {
            "section": "Git",
            "question": "What is Git?",
            "answer": "Git is a version control system that tracks changes in source code, allowing developers to collaborate, revert, and manage versions of their work."
        },
        {
            "section": "Git",
            "question": "What is a commit in Git?",
            "answer": "A commit in Git is a snapshot of changes made to the codebase, representing a point in history that can be revisited or reverted if needed."
        },
        {
            "section": "Git",
            "question": "What is the difference between git pull and git fetch?",
            "answer": "'git fetch' retrieves changes from a remote repository without merging, while 'git pull' fetches and automatically merges changes into the local branch."
        },
        {
            "section": "Git",
            "question": "What is branching in Git?",
            "answer": "Branching allows developers to create separate lines of development within a repository, enabling work on features independently before merging."
        },
        {
            "section": "Git",
            "question": "What is Git rebase?",
            "answer": "Git rebase integrates changes from one branch into another, creating a cleaner, linear commit history by moving commits to the top of the branch."
        },

        # Docker
        {
            "section": "Docker",
            "question": "What is Docker?",
            "answer": "Docker is a platform for creating, deploying, and running applications in isolated environments called containers, ensuring consistent environments across development and production."
        },
        {
            "section": "Docker",
            "question": "What is a Docker container?",
            "answer": "A Docker container is a lightweight, standalone executable package that includes everything needed to run a piece of software, including code, libraries, and dependencies."
        },
        {
            "section": "Docker",
            "question": "What is the difference between Docker image and Docker container?",
            "answer": "A Docker image is a blueprint used to create containers. A Docker container is a running instance of an image."
        },
        {
            "section": "Docker",
            "question": "What is Docker Compose?",
            "answer": "Docker Compose is a tool for defining and running multi-container Docker applications, using a YAML file to configure application services."
        },
        {
            "section": "Docker",
            "question": "How do you optimize a Docker image?",
            "answer": "Optimization techniques include using smaller base images, minimizing layers, caching dependencies, and cleaning up unused files and packages."
        },

        # Machine Learning
        {
            "section": "Machine Learning",
            "question": "What is Machine Learning?",
            "answer": "Machine Learning is a field of AI that enables systems to learn and improve from experience without being explicitly programmed, by analyzing patterns in data."
        },
        {
            "section": "Machine Learning",
            "question": "What is supervised learning?",
            "answer": "Supervised learning is a type of machine learning where models are trained on labeled data, with input-output pairs provided to predict future outcomes."
        },
        {
            "section": "Machine Learning",
            "question": "What is overfitting in Machine Learning?",
            "answer": "Overfitting occurs when a model learns the training data too well, capturing noise and details that reduce its performance on new data."
        },
        {
            "section": "Machine Learning",
            "question": "What is the difference between classification and regression?",
            "answer": "Classification is predicting categorical labels, while regression predicts continuous numerical values."
        },
        {
            "section": "Machine Learning",
            "question": "What is a neural network?",
            "answer": "A neural network is a series of algorithms that attempts to recognize underlying relationships in a set of data through a process that mimics the way the human brain operates."
        },

        # Data Science
        {
            "section": "Data Science",
            "question": "What is Data Science?",
            "answer": "Data Science is an interdisciplinary field focused on extracting knowledge and insights from structured and unstructured data using techniques from statistics, machine learning, and computer science."
        },
        {
            "section": "Data Science",
            "question": "What is data cleaning?",
            "answer": "Data cleaning is the process of preparing data for analysis by removing or correcting incorrect, corrupted, or missing data."
        },
        {
            "section": "Data Science",
            "question": "What is the difference between data mining and data analysis?",
            "answer": "Data mining focuses on finding patterns in large data sets, while data analysis involves interpreting those patterns and extracting actionable insights."
        },
        {
            "section": "Data Science",
            "question": "What is a data pipeline?",
            "answer": "A data pipeline is a series of data processing steps that move data from a source to a destination, often used in ETL (Extract, Transform, Load) processes."
        },
        {
            "section": "Data Science",
            "question": "What is exploratory data analysis (EDA)?",
            "answer": "EDA is the process of analyzing data sets to summarize their main characteristics, often using visualizations to gain insights before applying modeling techniques."
        }
    ])
    data.extend([
        # Vue.js
        {
            "section": "Vue.js",
            "question": "What is Vue.js?",
            "answer": "Vue.js is a progressive JavaScript framework used for building user interfaces and single-page applications, known for its gentle learning curve and flexibility."
        },
        {
            "section": "Vue.js",
            "question": "What are Vue components?",
            "answer": "Components are reusable instances in Vue that contain their own data, template, and logic, making it easy to structure and reuse code across an application."
        },
        {
            "section": "Vue.js",
            "question": "What is the Vue CLI?",
            "answer": "The Vue CLI is a tool for quickly setting up a Vue project with a pre-configured build system and development tools, allowing rapid development of Vue applications."
        },
        {
            "section": "Vue.js",
            "question": "What is Vue Router?",
            "answer": "Vue Router is the official router for Vue.js, enabling navigation between pages and providing features like nested routes, route params, and route guards."
        },
        {
            "section": "Vue.js",
            "question": "What is Vuex in Vue.js?",
            "answer": "Vuex is a state management library for Vue.js applications, allowing centralized storage of the app's state and enabling easy access and management of data across components."
        },

        # Angular.js
        {
            "section": "Angular.js",
            "question": "What is Angular.js?",
            "answer": "Angular.js is a JavaScript-based open-source front-end web framework primarily maintained by Google, used to build dynamic single-page applications."
        },
        {
            "section": "Angular.js",
            "question": "What are Angular directives?",
            "answer": "Directives are special tokens in Angular that allow developers to extend HTML with custom behaviors, manipulating DOM elements and adding logic to templates."
        },
        {
            "section": "Angular.js",
            "question": "What is two-way data binding in Angular?",
            "answer": "Two-way data binding synchronizes data between the model and the view, ensuring that changes in the model update the view and vice versa."
        },
        {
            "section": "Angular.js",
            "question": "What is an Angular service?",
            "answer": "An Angular service is a reusable business logic component used to perform tasks like data fetching, processing, and sharing across components."
        },
        {
            "section": "Angular.js",
            "question": "What is dependency injection in Angular?",
            "answer": "Dependency injection in Angular is a design pattern that allows classes to request dependencies from external sources rather than creating them themselves, improving code modularity."
        },

        # React Native
        {
            "section": "React Native",
            "question": "What is React Native?",
            "answer": "React Native is an open-source framework for building mobile applications using JavaScript and React, allowing code sharing between iOS and Android platforms."
        },
        {
            "section": "React Native",
            "question": "How does React Native work?",
            "answer": "React Native compiles JavaScript code to native components, enabling the use of JavaScript to build native mobile applications with near-native performance."
        },
        {
            "section": "React Native",
            "question": "What is a React Native component?",
            "answer": "A React Native component is a building block for UI in React Native, representing a reusable piece of the app's interface, like buttons, images, or containers."
        },
        {
            "section": "React Native",
            "question": "What is Expo in React Native?",
            "answer": "Expo is a framework and platform for React Native that provides tools for rapid prototyping, development, and testing, especially useful for beginners."
        },
        {
            "section": "React Native",
            "question": "What is the purpose of the React Navigation library?",
            "answer": "React Navigation is a library for routing and navigation in React Native applications, providing navigators like stack, tab, and drawer for screen transitions."
        },

        # Cypress
        {
            "section": "Cypress",
            "question": "What is Cypress?",
            "answer": "Cypress is a front-end testing framework built for modern web applications, offering end-to-end testing, integration testing, and unit testing."
        },
        {
            "section": "Cypress",
            "question": "What makes Cypress unique for testing?",
            "answer": "Cypress offers real-time reloads, time-travel debugging, and automatic waiting, which simplify writing and debugging tests for front-end applications."
        },
        {
            "section": "Cypress",
            "question": "What is the purpose of fixtures in Cypress?",
            "answer": "Fixtures in Cypress are external files used to hold mock data that can be loaded into tests, helping simulate different test scenarios."
        },
        {
            "section": "Cypress",
            "question": "How does Cypress handle asynchronous operations?",
            "answer": "Cypress automatically waits for asynchronous operations, like API requests or DOM updates, eliminating the need for manual waits in tests."
        },
        {
            "section": "Cypress",
            "question": "What is Cypress Test Runner?",
            "answer": "The Cypress Test Runner is a visual tool that allows you to watch tests run in real-time, providing insight into each command's behavior during execution."
        },

        # Java
        {
            "section": "Java",
            "question": "What is Java?",
            "answer": "Java is a high-level, object-oriented programming language known for its portability, robustness, and wide use in enterprise and mobile applications."
        },
        {
            "section": "Java",
            "question": "What are Java classes and objects?",
            "answer": "Classes in Java are blueprints for objects, defining their properties and behaviors, while objects are instances created from classes."
        },
        {
            "section": "Java",
            "question": "What is inheritance in Java?",
            "answer": "Inheritance is a feature in Java that allows one class to inherit fields and methods from another class, promoting code reuse and organization."
        },
        {
            "section": "Java",
            "question": "What is the purpose of the 'final' keyword in Java?",
            "answer": "The 'final' keyword in Java can make variables constant, prevent method overriding, and prevent inheritance for classes, ensuring immutability."
        },
        {
            "section": "Java",
            "question": "What is a Java interface?",
            "answer": "An interface in Java is an abstract type used to specify a set of methods that a class must implement, enabling multiple inheritance and abstraction."
        },

        # Spring Boot
        {
            "section": "Spring Boot",
            "question": "What is Spring Boot?",
            "answer": "Spring Boot is a framework for building Java applications with minimal configuration, using pre-built defaults to simplify Spring-based development."
        },
        {
            "section": "Spring Boot",
            "question": "What are Spring Boot starters?",
            "answer": "Starters are dependency packages in Spring Boot that simplify adding specific functionality, such as web development, database access, and testing."
        },
        {
            "section": "Spring Boot",
            "question": "What is Spring Boot autoconfiguration?",
            "answer": "Autoconfiguration in Spring Boot automatically configures beans based on the project's dependencies, reducing the need for explicit configuration."
        },
        {
            "section": "Spring Boot",
            "question": "What is Spring Boot's Actuator?",
            "answer": "Spring Boot Actuator provides tools for monitoring and managing applications in production, including health checks and metric endpoints."
        },
        {
            "section": "Spring Boot",
            "question": "What is the purpose of Spring Boot profiles?",
            "answer": "Spring Boot profiles allow managing different configurations for different environments, such as development, testing, and production."
        },

        # MongoDB
        {
            "section": "MongoDB",
            "question": "What is MongoDB?",
            "answer": "MongoDB is a NoSQL, document-oriented database known for its scalability and flexibility, commonly used to store semi-structured data."
        },
        {
            "section": "MongoDB",
            "question": "What is a document in MongoDB?",
            "answer": "A document in MongoDB is a record stored in BSON format, containing fields and values, similar to JSON objects, and can be nested."
        },
        {
            "section": "MongoDB",
            "question": "What is a MongoDB collection?",
            "answer": "A collection in MongoDB is a group of documents, akin to a table in relational databases, but it does not enforce a schema for its documents."
        },
        {
            "section": "MongoDB",
            "question": "What is sharding in MongoDB?",
            "answer": "Sharding is a method for distributing data across multiple servers in MongoDB, allowing for horizontal scaling of large datasets."
        },
        {
            "section": "MongoDB",
            "question": "What is MongoDB aggregation?",
            "answer": "Aggregation in MongoDB processes data records and returns computed results, often used for analytics, similar to SQL's GROUP BY."
        },

        # PostgreSQL
        {
            "section": "PostgreSQL",
            "question": "What is PostgreSQL?",
            "answer": "PostgreSQL is an open-source, relational database system known for its robustness, extensibility, and support for complex queries."
        },
        {
            "section": "PostgreSQL",
            "question": "What are PostgreSQL indexes?",
            "answer": "Indexes in PostgreSQL are used to speed up data retrieval operations by creating a lookup structure for rows, reducing query time."
        },
        {
            "section": "PostgreSQL",
            "question": "What is a PostgreSQL schema?",
            "answer": "A schema in PostgreSQL is a logical container for database objects, such as tables, functions, and views, organizing them within a database."
        },
        {
            "section": "PostgreSQL",
            "question": "What is a primary key in PostgreSQL?",
            "answer": "A primary key is a unique identifier for rows in a PostgreSQL table, ensuring each row has a unique, non-null value in the key column."
        },
        {
            "section": "PostgreSQL",
            "question": "What are PostgreSQL transactions?",
            "answer": "Transactions in PostgreSQL group SQL operations into a single, atomic unit, ensuring all or none of the changes are applied to the database."
        },

        # AWS
        {
            "section": "AWS",
            "question": "What is AWS?",
            "answer": "AWS (Amazon Web Services) is a cloud platform offering computing power, storage, and various tools to build and manage applications in the cloud."
        },
        {
            "section": "AWS",
            "question": "What is Amazon S3?",
            "answer": "Amazon S3 (Simple Storage Service) is a scalable storage service for storing and retrieving large volumes of data and files in AWS."
        },
        {
            "section": "AWS",
            "question": "What is EC2 in AWS?",
            "answer": "Amazon EC2 (Elastic Compute Cloud) provides scalable virtual servers for running applications on the AWS cloud."
        },
        {
            "section": "AWS",
            "question": "What is an Amazon RDS?",
            "answer": "Amazon RDS (Relational Database Service) simplifies setting up, operating, and scaling relational databases in the cloud with automated maintenance."
        },
        {
            "section": "AWS",
            "question": "What is Amazon Lambda?",
            "answer": "AWS Lambda is a serverless computing service that automatically manages the infrastructure, allowing users to run code in response to events without managing servers."
        },

        # Azure
        {
            "section": "Azure",
            "question": "What is Microsoft Azure?",
            "answer": "Microsoft Azure is a cloud computing platform offering a wide array of services including compute, analytics, storage, and networking."
        },
        {
            "section": "Azure",
            "question": "What is Azure Virtual Machines?",
            "answer": "Azure Virtual Machines provide scalable, on-demand computing resources on the Azure cloud, similar to physical computers but fully virtualized."
        },
        {
            "section": "Azure",
            "question": "What is Azure Blob Storage?",
            "answer": "Azure Blob Storage is a service for storing large amounts of unstructured data, such as text or binary data, commonly used for backups and media storage."
        },
        {
            "section": "Azure",
            "question": "What is Azure Kubernetes Service (AKS)?",
            "answer": "Azure Kubernetes Service (AKS) is a managed Kubernetes service that simplifies deploying, managing, and scaling containerized applications in Azure."
        },
        {
            "section": "Azure",
            "question": "What is Azure Active Directory?",
            "answer": "Azure Active Directory (Azure AD) is an identity and access management service for securely managing access to Azure resources, applications, and services."
        }
    ])

    # Create DataFrame
    df = pd.DataFrame(data)

    # Initialize ChromaDB client
    client = chromadb.PersistentClient("vectorstore")
    collection = client.get_or_create_collection(name="interview_questions")

    # Check if collection is empty before inserting data

    for _, row in df.iterrows():
        collection.add(
            documents=row["question"],
            metadatas={"Answer": row["answer"], "Section": row["section"]},
            ids=[str(uuid.uuid4())]  # Generate unique ID for each entry
        )

    print("Data inserted successfully into ChromaDB under 'interview questions'.")

# Call the function to execute insertion
#insert_interview_questions()

import chromadb

def fetch_questions_by_keyword(keyword: str):
    # Initialize ChromaDB client
    client = chromadb.PersistentClient("vectorstore")
    collection = client.get_collection(name="interview_questions")

    # Query the collection for questions related to the keyword
    query_result = collection.query(query_texts=[keyword], n_results=10)

    # Extract and print the questions and answers
    if query_result['documents']:
        for doc, metadata in zip(query_result['documents'][0], query_result['metadatas'][0]):
            print(f"Section: {metadata['Section']}")
            print(f"Question: {doc}")
            print(f"Answer: {metadata['Answer']}\n")
    else:
        print("No questions found for the given keyword.")

# Example usage
fetch_questions_by_keyword("AWS")