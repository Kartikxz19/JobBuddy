#questionInserter.py
#this file will help understand how Q&As are stored in the chromDB and how do you fetch those Q&As
import pandas as pd
import uuid
import chromadb

def insert_interview_questions():
    # Create a DataFrame with the React questions and answers
    data = [
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
        },
        {
            "section": "GraphQL",
            "question": "What is GraphQL, and how does it differ from REST?",
            "answer": "GraphQL is a query language for APIs that allows clients to request specific data, reducing over-fetching and under-fetching compared to REST."
        },
        {
            "section": "GraphQL",
            "question": "What are the main components of a GraphQL schema?",
            "answer": "A GraphQL schema defines the types, queries, and mutations available in an API, shaping the structure of data clients can request."
        },
        {
            "section": "GraphQL",
            "question": "What is a resolver in GraphQL?",
            "answer": "A resolver is a function that resolves the value for a specific field in a GraphQL query, often used to fetch data from a database or other services."
        },
        {
            "section": "GraphQL",
            "question": "Explain the purpose of mutations in GraphQL.",
            "answer": "Mutations in GraphQL are used to modify server-side data, like creating, updating, or deleting resources, similar to POST, PUT, and DELETE in REST."
        },
        {
            "section": "GraphQL",
            "question": "How does error handling work in GraphQL?",
            "answer": "GraphQL sends errors in the response under an `errors` key, and individual resolvers can return partial data if only specific fields encounter issues."
        },
        {
            "section": "GraphQL",
            "question": "What is the purpose of GraphQL directives?",
            "answer": "Directives in GraphQL are used to customize query execution, such as `@include` and `@skip`, which conditionally include or exclude fields."
        },
        {
            "section": "GraphQL",
            "question": "How does GraphQL handle versioning?",
            "answer": "GraphQL doesn’t require versioning like REST, as clients request specific fields, allowing APIs to evolve without breaking existing queries."
        },
        {
            "section": "GraphQL",
            "question": "What is Apollo Client, and how is it used in GraphQL?",
            "answer": "Apollo Client is a JavaScript library used to manage local and remote data with GraphQL, providing caching and query management for client applications."
        },
        {
            "section": "GraphQL",
            "question": "Explain the use of fragments in GraphQL.",
            "answer": "Fragments are reusable parts of a query that define fields, reducing redundancy when querying the same fields across multiple parts of a query."
        },
        {
            "section": "GraphQL",
            "question": "What is introspection in GraphQL?",
            "answer": "Introspection is a feature in GraphQL that allows clients to query the schema itself, retrieving metadata about available types, queries, and mutations."
        },
        {
            "section": "Redis",
            "question": "What is Redis, and what are its primary use cases?",
            "answer": "Redis is an in-memory data store known for its speed and flexibility, commonly used for caching, session management, and real-time analytics."
        },
        {
            "section": "Redis",
            "question": "How does data persistence work in Redis?",
            "answer": "Redis supports data persistence through RDB snapshots and AOF (Append-Only File) logs, allowing data to be saved to disk and recovered."
        },
        {
            "section": "Redis",
            "question": "What are Redis keys and values, and how is data structured?",
            "answer": "Redis data is stored in key-value pairs, with values supporting multiple data types like strings, lists, hashes, sets, and sorted sets."
        },
        {
            "section": "Redis",
            "question": "Explain the purpose of Redis Pub/Sub.",
            "answer": "Redis Pub/Sub allows message broadcasting across channels, enabling publish-subscribe messaging for real-time notifications and event handling."
        },
        {
            "section": "Redis",
            "question": "What are Redis pipelines, and how do they improve performance?",
            "answer": "Pipelines allow multiple Redis commands to be executed in a single request-response cycle, reducing latency and improving throughput."
        },
        {
            "section": "Redis",
            "question": "How do you handle data expiration in Redis?",
            "answer": "Redis supports data expiration by setting a TTL (Time-to-Live) on keys, after which the key is automatically deleted from memory."
        },
        {
            "section": "Redis",
            "question": "What is Redis clustering, and why is it used?",
            "answer": "Redis clustering distributes data across multiple nodes, providing high availability, fault tolerance, and horizontal scaling for large datasets."
        },
        {
            "section": "Redis",
            "question": "How does Redis handle transactions?",
            "answer": "Redis transactions use the `MULTI` and `EXEC` commands to group multiple commands into a single transaction, executing them atomically."
        },
        {
            "section": "Redis",
            "question": "What is Redis Sentinel, and what role does it play?",
            "answer": "Redis Sentinel is a high-availability solution for monitoring Redis instances, providing automatic failover and notifications in case of failures."
        },
        {
            "section": "Redis",
            "question": "What is a Redis hash, and how does it differ from a list?",
            "answer": "A Redis hash stores key-value pairs within a single Redis key, while a list is an ordered collection of strings, similar to a linked list."
        },
        {
            "section": "Kubernetes",
            "question": "What is Kubernetes, and what problems does it solve?",
            "answer": "Kubernetes is a container orchestration platform that automates deployment, scaling, and management of containerized applications, solving issues with manual container handling."
        },
        {
            "section": "Kubernetes",
            "question": "Explain the Kubernetes architecture and its main components.",
            "answer": "Kubernetes architecture consists of nodes and a master control plane, with core components like API server, etcd, scheduler, controller manager, and kubelet."
        },
        {
            "section": "Kubernetes",
            "question": "What is a Kubernetes pod?",
            "answer": "A pod is the smallest deployable unit in Kubernetes, representing a single instance of a running process, often containing one or more containers."
        },
        {
            "section": "Kubernetes",
            "question": "How does Kubernetes handle scaling?",
            "answer": "Kubernetes handles scaling through horizontal pod autoscaling, adjusting the number of pods based on resource usage or custom metrics."
        },
        {
            "section": "Kubernetes",
            "question": "What is a Kubernetes deployment, and how does it work?",
            "answer": "A Kubernetes deployment manages stateless application updates, allowing declarative updates for pods and managing rollbacks automatically."
        },
        {
            "section": "Kubernetes",
            "question": "How does Kubernetes manage storage with Persistent Volumes?",
            "answer": "Persistent Volumes provide storage resources that outlive individual pods, allowing storage management separate from the lifecycle of pods."
        },
        {
            "section": "Kubernetes",
            "question": "What is a Kubernetes service, and why is it important?",
            "answer": "A service in Kubernetes exposes a set of pods to network traffic, providing a stable endpoint for accessing a group of pods across deployments."
        },
        {
            "section": "Kubernetes",
            "question": "How do you perform a rolling update in Kubernetes?",
            "answer": "A rolling update in Kubernetes gradually replaces pods with new ones, maintaining application availability during updates without downtime."
        },
        {
            "section": "Kubernetes",
            "question": "What are ConfigMaps and Secrets in Kubernetes?",
            "answer": "ConfigMaps store configuration data as key-value pairs, while Secrets manage sensitive data, such as passwords and tokens, securely."
        },
        {
            "section": "Kubernetes",
            "question": "How does Kubernetes handle load balancing?",
            "answer": "Kubernetes load balances traffic to pods using services, such as ClusterIP and LoadBalancer, to distribute incoming requests across available pods."
        },
        {
            "section": "Elasticsearch",
            "question": "What is Elasticsearch, and what are its primary use cases?",
            "answer": "Elasticsearch is a search engine based on the Lucene library, used for full-text search, analytics, and log aggregation in real-time."
        },
        {
            "section": "Elasticsearch",
            "question": "Explain the concept of an index in Elasticsearch.",
            "answer": "An index is a collection of documents in Elasticsearch that allows for organized storage and searchability within a specific data structure."
        },
        {
            "section": "Elasticsearch",
            "question": "What is a document in Elasticsearch?",
            "answer": "A document is the basic unit of data in Elasticsearch, represented as a JSON object containing various fields and values."
        },
        {
            "section": "Elasticsearch",
            "question": "How does Elasticsearch handle scaling and sharding?",
            "answer": "Elasticsearch scales horizontally by splitting indexes into shards, distributing them across nodes to balance load and improve performance."
        },
        {
            "section": "Elasticsearch",
            "question": "What is the role of the Elasticsearch cluster and node?",
            "answer": "A cluster is a collection of nodes working together in Elasticsearch, while a node is a single server within the cluster that stores data and participates in the search and indexing."
        },
        {
            "section": "Elasticsearch",
            "question": "Explain the concept of relevance scoring in Elasticsearch.",
            "answer": "Relevance scoring is the process by which Elasticsearch ranks search results, determining the best match for a query based on factors like term frequency and field weighting."
        },
        {
            "section": "Elasticsearch",
            "question": "What is a mapping in Elasticsearch?",
            "answer": "A mapping defines the structure of documents in an index, specifying field types and configurations for indexing and searching."
        },
        {
            "section": "Elasticsearch",
            "question": "How does Elasticsearch handle data consistency?",
            "answer": "Elasticsearch uses a primary and replica shard system, providing eventual consistency by writing changes to the primary shard and replicating them to replicas."
        },
        {
            "section": "Elasticsearch",
            "question": "What are aggregations in Elasticsearch?",
            "answer": "Aggregations in Elasticsearch are used for analytics, allowing users to group and analyze data across multiple documents to get insights."
        },
        {
            "section": "Elasticsearch",
            "question": "Explain the purpose of an Elasticsearch query and filter context.",
            "answer": "Query context scores documents for relevance, while filter context selects documents based on criteria without affecting scores, enhancing performance for specific use cases."
        },
        {
            "section": "Kafka",
            "question": "What is Apache Kafka, and what are its main use cases?",
            "answer": "Apache Kafka is a distributed streaming platform used for building real-time data pipelines and streaming applications, suitable for event sourcing and log aggregation."
        },
        {
            "section": "Kafka",
            "question": "Explain the core components of Kafka: topics, producers, and consumers.",
            "answer": "Kafka topics are channels for data, producers publish messages to topics, and consumers read messages from topics, enabling distributed data communication."
        },
        {
            "section": "Kafka",
            "question": "What is a Kafka broker, and what role does it play?",
            "answer": "A Kafka broker is a server that stores and serves messages, managing topic data and handling requests from producers and consumers."
        },
        {
            "section": "Kafka",
            "question": "How does Kafka handle data replication?",
            "answer": "Kafka replicates data across multiple brokers to ensure fault tolerance, with one broker acting as the leader and others as followers for each partition."
        },
        {
            "section": "Kafka",
            "question": "What is a Kafka partition, and why is it important?",
            "answer": "A partition is a subdivision of a topic in Kafka, allowing parallel processing and balancing data across brokers for scalability and high throughput."
        },
        {
            "section": "Kafka",
            "question": "How does Kafka guarantee message ordering?",
            "answer": "Kafka guarantees message order within a partition, but messages may arrive in different orders across partitions, preserving order within each one."
        },
        {
            "section": "Kafka",
            "question": "Explain the difference between Kafka’s Consumer Group and individual consumers.",
            "answer": "A consumer group is a collection of consumers that work together to consume a topic, ensuring each message is processed once, while individual consumers may consume messages independently."
        },
        {
            "section": "Kafka",
            "question": "What is Kafka Streams, and what purpose does it serve?",
            "answer": "Kafka Streams is a library for building real-time streaming applications that process and transform data directly within Kafka, enabling event-driven architectures."
        },
        {
            "section": "Kafka",
            "question": "How does Kafka handle fault tolerance and failover?",
            "answer": "Kafka uses leader-follower replication and elects new leaders if a broker fails, ensuring continued data availability and resilience to failures."
        },
        {
            "section": "Kafka",
            "question": "What is Kafka Connect, and how is it used?",
            "answer": "Kafka Connect is a framework for integrating Kafka with external data sources, allowing data import/export with various systems, such as databases and storage solutions."
        },
        {
            "section": "Terraform",
            "question": "What is Terraform, and what are its primary use cases?",
            "answer": "Terraform is an open-source infrastructure as code tool that allows developers to define and manage cloud and on-premises resources declaratively."
        },
        {
            "section": "Terraform",
            "question": "Explain the difference between declarative and imperative approaches in Terraform.",
            "answer": "Terraform uses a declarative approach, where you define the desired state of infrastructure, and Terraform manages the steps to reach that state."
        },
        {
            "section": "Terraform",
            "question": "What is a Terraform provider?",
            "answer": "A provider in Terraform is a plugin that interacts with cloud platforms or services, enabling the management of their resources in infrastructure configurations."
        },
        {
            "section": "Terraform",
            "question": "What are modules in Terraform, and why are they used?",
            "answer": "Modules are reusable infrastructure components in Terraform, allowing consistent configurations across projects and simplifying complex setups."
        },
        {
            "section": "Terraform",
            "question": "How does Terraform handle state management?",
            "answer": "Terraform stores infrastructure state in a file, tracking resources to determine changes needed and preventing configuration drift."
        },
        {
            "section": "Terraform",
            "question": "What is a Terraform plan, and how is it used?",
            "answer": "A Terraform plan previews changes by showing the difference between the current state and desired configuration, helping to verify changes before applying."
        },
        {
            "section": "Terraform",
            "question": "Explain the purpose of Terraform workspaces.",
            "answer": "Workspaces in Terraform allow multiple states for a configuration, enabling separate environments (e.g., dev, prod) within a single configuration."
        },
        {
            "section": "Terraform",
            "question": "What is the use of `terraform apply` and `terraform destroy` commands?",
            "answer": "`terraform apply` creates or updates infrastructure to match configuration, while `terraform destroy` removes resources defined in the configuration."
        },
        {
            "section": "Terraform",
            "question": "How does remote state work in Terraform, and why is it important?",
            "answer": "Remote state stores Terraform state in a shared location, such as cloud storage, enabling collaboration and consistent state across teams."
        },
        {
            "section": "Terraform",
            "question": "What is Terraform's approach to handling secrets?",
            "answer": "Terraform relies on external secrets management solutions and environment variables, as it lacks native secure storage for sensitive data."
        },
        {
            "section": "Jenkins",
            "question": "What is Jenkins, and what is it primarily used for?",
            "answer": "Jenkins is an open-source automation server used for continuous integration and continuous delivery, enabling automation of software development workflows."
        },
        {
            "section": "Jenkins",
            "question": "What is a Jenkins pipeline, and how does it work?",
            "answer": "A Jenkins pipeline is a suite of automated steps, defined as code, that allows continuous delivery of software from source to deployment."
        },
        {
            "section": "Jenkins",
            "question": "Explain the difference between Declarative and Scripted Pipelines in Jenkins.",
            "answer": "Declarative Pipelines provide a simpler, more readable structure for CI/CD, while Scripted Pipelines allow full programmatic control but are more complex."
        },
        {
            "section": "Jenkins",
            "question": "What is a Jenkins job, and how do you configure one?",
            "answer": "A Jenkins job is a task or set of instructions executed within Jenkins, configured through a GUI or as code to define build steps, post-build actions, etc."
        },
        {
            "section": "Jenkins",
            "question": "How does Jenkins manage plugins, and why are they important?",
            "answer": "Plugins extend Jenkins' functionality, supporting integrations with tools, platforms, and customized pipelines for diverse CI/CD needs."
        },
        {
            "section": "Jenkins",
            "question": "What is the Jenkins master-slave architecture?",
            "answer": "Jenkins' master-slave architecture allows distributed builds, with the master coordinating and assigning tasks to slave nodes for parallel execution."
        },
        {
            "section": "Jenkins",
            "question": "What is the purpose of Jenkins Blue Ocean?",
            "answer": "Blue Ocean is a modernized Jenkins UI focused on simplifying pipeline visualization and improving user experience with enhanced usability."
        },
        {
            "section": "Jenkins",
            "question": "How can Jenkins be used for automated testing?",
            "answer": "Jenkins can run test scripts as part of the CI/CD pipeline, enabling early bug detection and quality assurance in the build process."
        },
        {
            "section": "Jenkins",
            "question": "What are Jenkins agents, and how do they function?",
            "answer": "Agents (previously called slaves) execute builds and tasks assigned by the Jenkins master, enabling distributed and parallelized build processes."
        },
        {
            "section": "Jenkins",
            "question": "Explain Jenkins' approach to credentials management.",
            "answer": "Jenkins stores and manages credentials securely, enabling secure access to sensitive data and external services within pipelines."
        },
        {
            "section": "Blockchain",
            "question": "What is Blockchain technology, and how does it work?",
            "answer": "Blockchain is a decentralized, distributed ledger that records transactions in blocks, linked cryptographically to maintain a secure and tamper-proof history."
        },
        {
            "section": "Blockchain",
            "question": "Explain the concept of consensus mechanisms in Blockchain.",
            "answer": "Consensus mechanisms, like Proof of Work and Proof of Stake, ensure agreement on the validity of transactions across decentralized blockchain networks."
        },
        {
            "section": "Blockchain",
            "question": "What is a smart contract?",
            "answer": "A smart contract is a self-executing contract with the terms of the agreement directly written into code, running on a blockchain to automate transactions."
        },
        {
            "section": "Blockchain",
            "question": "Describe the difference between public and private blockchains.",
            "answer": "Public blockchains are open and accessible to anyone, while private blockchains restrict access to authorized participants for privacy and control."
        },
        {
            "section": "Blockchain",
            "question": "What role does cryptography play in Blockchain?",
            "answer": "Cryptography secures transactions in a blockchain, using hashing and digital signatures to ensure data integrity and prevent unauthorized access."
        },
        {
            "section": "Blockchain",
            "question": "What is a blockchain fork, and why does it happen?",
            "answer": "A fork occurs when the blockchain splits into two paths due to protocol changes or conflicting data, leading to a potential division of the chain."
        },
        {
            "section": "Blockchain",
            "question": "How does Bitcoin's Proof of Work mechanism work?",
            "answer": "Proof of Work requires miners to solve complex computational puzzles, adding blocks to the chain and securing the network against malicious activity."
        },
        {
            "section": "Blockchain",
            "question": "What are the potential uses of blockchain beyond cryptocurrency?",
            "answer": "Blockchain has diverse applications in areas like supply chain management, healthcare, identity verification, and secure voting systems."
        },
        {
            "section": "Blockchain",
            "question": "What is the role of nodes in a blockchain network?",
            "answer": "Nodes are computers participating in a blockchain network, maintaining copies of the ledger, validating transactions, and ensuring network integrity."
        },
        {
            "section": "Blockchain",
            "question": "Explain the concept of mining in blockchain technology.",
            "answer": "Mining is the process of adding new blocks to a blockchain, requiring computational work to validate transactions and ensure security in networks like Bitcoin."
        },
        {
            "section": "Svelte",
            "question": "What is Svelte, and how does it differ from other front-end frameworks?",
            "answer": "Svelte is a front-end framework that compiles components into highly optimized vanilla JavaScript at build time, removing the need for a virtual DOM like in React or Vue."
        },
        {
            "section": "Svelte",
            "question": "Explain the reactivity model in Svelte.",
            "answer": "In Svelte, reactivity is achieved through assignments. When a variable is reassigned, any dependent code automatically updates, enabling fine-grained reactivity without a virtual DOM."
        },
        {
            "section": "Svelte",
            "question": "What are Svelte stores, and how do they manage state?",
            "answer": "Svelte stores are reactive variables that provide a centralized way to manage state. They come in writable, readable, and derived types to fit different use cases."
        },
        {
            "section": "Svelte",
            "question": "How does Svelte handle component lifecycle?",
            "answer": "Svelte offers lifecycle functions like `onMount`, `beforeUpdate`, and `afterUpdate` that allow developers to execute code at specific points in a component's lifecycle."
        },
        {
            "section": "Svelte",
            "question": "What is the purpose of the `bind:` directive in Svelte?",
            "answer": "The `bind:` directive allows two-way data binding, letting a component variable be bound to an input field, updating the variable whenever the field changes."
        },
        {
            "section": "Tailwind CSS",
            "question": "What is Tailwind CSS, and what makes it unique among CSS frameworks?",
            "answer": "Tailwind CSS is a utility-first CSS framework that provides low-level utility classes to build custom designs without writing custom CSS."
        },
        {
            "section": "Tailwind CSS",
            "question": "Explain the concept of utility classes in Tailwind CSS.",
            "answer": "Utility classes in Tailwind CSS are single-purpose classes like `text-center` or `bg-blue-500` that allow developers to build complex designs directly in HTML."
        },
        {
            "section": "Tailwind CSS",
            "question": "How can you customize colors and breakpoints in Tailwind CSS?",
            "answer": "Tailwind CSS allows customization via the `tailwind.config.js` file, where you can define custom color schemes, breakpoints, and extend or override defaults."
        },
        {
            "section": "Tailwind CSS",
            "question": "What is JIT mode in Tailwind CSS?",
            "answer": "Just-In-Time (JIT) mode generates styles on-demand as you use them, reducing build times and overall file sizes by creating only the classes needed for a project."
        },
        {
            "section": "Tailwind CSS",
            "question": "How do you use Tailwind's responsive design utilities?",
            "answer": "Responsive design in Tailwind is achieved through breakpoint prefixes like `sm:`, `md:`, etc., allowing styles to apply at specific screen widths."
        },
        {
            "section": "TypeScript",
            "question": "What is TypeScript, and how does it enhance JavaScript?",
            "answer": "TypeScript is a superset of JavaScript that adds static typing, helping developers catch type-related errors early and write more maintainable code."
        },
        {
            "section": "TypeScript",
            "question": "Explain the difference between interfaces and types in TypeScript.",
            "answer": "Both interfaces and types allow defining object structures, but interfaces support declaration merging, whereas types are more flexible, supporting unions and intersections."
        },
        {
            "section": "TypeScript",
            "question": "What are TypeScript generics, and how do they work?",
            "answer": "Generics enable defining functions or classes that work with multiple types while providing type safety, allowing developers to write reusable, type-safe code."
        },
        {
            "section": "TypeScript",
            "question": "How does TypeScript handle null and undefined values?",
            "answer": "TypeScript uses strict null checks (enabled with `--strictNullChecks`) to ensure variables are not assigned null or undefined unless explicitly allowed."
        },
        {
            "section": "TypeScript",
            "question": "What is type inference in TypeScript?",
            "answer": "Type inference allows TypeScript to automatically determine the type of a variable based on its initial value or usage, reducing the need for explicit type annotations."
        },
        {
            "section": "Django (Python)",
            "question": "What is Django, and what is it primarily used for?",
            "answer": "Django is a high-level Python web framework designed for rapid development and clean, pragmatic design, often used for building complex database-driven websites."
        },
        {
            "section": "Django (Python)",
            "question": "Explain the purpose of Django's ORM (Object-Relational Mapper).",
            "answer": "Django's ORM provides an abstraction layer to interact with databases, allowing developers to work with database objects using Python classes instead of raw SQL."
        },
        {
            "section": "Django (Python)",
            "question": "What is Django's MVT (Model-View-Template) architecture?",
            "answer": "Django's MVT architecture separates data (Model), user interface (Template), and business logic (View), promoting modular and maintainable code structure."
        },
        {
            "section": "Django (Python)",
            "question": "How does Django handle user authentication?",
            "answer": "Django provides a built-in authentication system with support for login, logout, password management, and session-based authentication for web applications."
        },
        {
            "section": "Django (Python)",
            "question": "What is the purpose of Django's middleware?",
            "answer": "Django middleware is a layer that processes requests and responses globally, enabling features like security, session handling, and cross-site scripting protection."
        },
        {
            "section": "Apache Spark",
            "question": "What is Apache Spark, and how does it differ from Hadoop MapReduce?",
            "answer": "Apache Spark is a distributed data processing engine that performs in-memory computing, making it faster than Hadoop MapReduce, which writes intermediate data to disk."
        },
        {
            "section": "Apache Spark",
            "question": "What are RDDs (Resilient Distributed Datasets) in Spark?",
            "answer": "RDDs are a fundamental data structure in Spark that represent distributed collections of objects, offering fault tolerance and parallel processing capabilities."
        },
        {
            "section": "Apache Spark",
            "question": "What is Spark SQL, and how does it integrate with Spark's core engine?",
            "answer": "Spark SQL is a module for working with structured data using SQL queries, integrating with Spark’s core engine to provide support for data frames and SQL-based transformations."
        },
        {
            "section": "Apache Spark",
            "question": "Explain Spark’s support for machine learning with MLlib.",
            "answer": "MLlib is Spark's scalable machine learning library that provides common algorithms and utilities for classification, regression, clustering, and collaborative filtering."
        },
        {
            "section": "Apache Spark",
            "question": "What is the concept of ‘lazy evaluation’ in Apache Spark?",
            "answer": "Lazy evaluation means that Spark delays the execution of transformations until an action (like `collect` or `count`) is invoked, optimizing the overall execution plan."
        },
        {
            "section": "Scikit-Learn",
            "question": "What is Scikit-Learn, and how is it used in machine learning?",
            "answer": "Scikit-Learn is a popular Python library for machine learning that provides simple and efficient tools for data analysis and modeling, including algorithms for classification, regression, and clustering."
        },
        {
            "section": "Scikit-Learn",
            "question": "What is the purpose of feature scaling in Scikit-Learn?",
            "answer": "Feature scaling standardizes or normalizes the range of features so that models are not biased towards features with larger values or scales, improving model accuracy."
        },
        {
            "section": "Scikit-Learn",
            "question": "How does cross-validation work in Scikit-Learn?",
            "answer": "Cross-validation splits the data into multiple subsets and trains the model on different training sets, evaluating it on different validation sets to ensure robust performance."
        },
        {
            "section": "Scikit-Learn",
            "question": "What are hyperparameters, and how do you tune them in Scikit-Learn?",
            "answer": "Hyperparameters are parameters set before training the model, and you can tune them using techniques like grid search or randomized search to improve model performance."
        },
        {
            "section": "Scikit-Learn",
            "question": "Explain the difference between classification and regression algorithms in Scikit-Learn.",
            "answer": "Classification algorithms predict categorical outcomes (e.g., spam or not spam), while regression algorithms predict continuous outcomes (e.g., house price predictions)."
        },
        {
            "section": "Flask (Python)",
            "question": "What is Flask, and how does it differ from Django?",
            "answer": "Flask is a micro web framework for Python that is lightweight and flexible, allowing more control over the application structure, while Django is a full-stack framework with built-in features."
        },
        {
            "section": "Flask (Python)",
            "question": "How does Flask handle routing in web applications?",
            "answer": "Flask uses decorators to map URLs to view functions, allowing easy URL routing. Routes can also include variables and query parameters."
        },
        {
            "section": "Flask (Python)",
            "question": "What is a Flask blueprint, and why is it useful?",
            "answer": "A Flask blueprint is a way to organize application code into reusable components, improving code modularity and allowing for easier maintenance of large applications."
        },
        {
            "section": "Flask (Python)",
            "question": "Explain Flask’s support for session management.",
            "answer": "Flask supports session management through secure cookies that store user session data on the client-side, with optional server-side session storage for more complex use cases."
        },
        {
            "section": "Flask (Python)",
            "question": "What are Flask extensions, and how do they enhance Flask’s functionality?",
            "answer": "Flask extensions add additional functionality to Flask, such as database integration, form validation, authentication, and more, by extending the core Flask features."
        },
        {
            "section": "TensorFlow",
            "question": "What is TensorFlow, and how does it differ from other machine learning frameworks?",
            "answer": "TensorFlow is an open-source machine learning framework developed by Google that supports deep learning, neural networks, and various machine learning algorithms, providing scalability and ease of deployment, especially in production environments."
        },
        {
            "section": "TensorFlow",
            "question": "What is TensorFlow's computational graph, and how does it work?",
            "answer": "TensorFlow uses a computational graph where nodes represent operations, and edges represent data flow. This graph is executed in a session to compute the results of the operations."
        },
        {
            "section": "TensorFlow",
            "question": "What are tensors in TensorFlow, and how are they different from arrays in other libraries?",
            "answer": "Tensors are multi-dimensional arrays used in TensorFlow to represent data. They are similar to arrays but can run on different devices (like GPUs) and are more efficient for large-scale computations."
        },
        {
            "section": "TensorFlow",
            "question": "Explain the purpose of TensorFlow's Keras API.",
            "answer": "Keras is a high-level neural networks API integrated into TensorFlow that provides easy-to-use methods to build and train deep learning models with minimal code."
        },
        {
            "section": "TensorFlow",
            "question": "What is TensorFlow Lite, and how is it used for mobile applications?",
            "answer": "TensorFlow Lite is a lightweight version of TensorFlow designed for mobile and embedded devices, allowing deployment of machine learning models on devices with limited resources."
        },
        {
            "section": "PyTorch",
            "question": "What is PyTorch, and how does it compare to TensorFlow?",
            "answer": "PyTorch is an open-source machine learning framework known for its dynamic computation graph, which allows more flexibility during model development compared to TensorFlow's static graph approach."
        },
        {
            "section": "PyTorch",
            "question": "What is the difference between a tensor and a Variable in PyTorch?",
            "answer": "Tensors are multi-dimensional arrays used in PyTorch, while Variables are wrappers around tensors that allow automatic differentiation for backpropagation in neural networks."
        },
        {
            "section": "PyTorch",
            "question": "Explain how PyTorch handles GPU acceleration.",
            "answer": "PyTorch allows seamless use of GPUs by moving tensors and models to the GPU with `.to(device)` or `.cuda()`, enabling faster computation for deep learning tasks."
        },
        {
            "section": "PyTorch",
            "question": "What is the role of autograd in PyTorch?",
            "answer": "Autograd in PyTorch is responsible for automatic differentiation, tracking operations on tensors and automatically computing gradients for optimization during training."
        },
        {
            "section": "PyTorch",
            "question": "How do you implement a custom loss function in PyTorch?",
            "answer": "To implement a custom loss function, you subclass `torch.nn.Module` and define the forward method to calculate the loss based on predictions and true labels."
        },
        {
            "section": "Ruby on Rails",
            "question": "What is Ruby on Rails, and why is it a popular framework for web development?",
            "answer": "Ruby on Rails (RoR) is an open-source web framework written in Ruby, known for its 'convention over configuration' approach and rapid development features, making it popular for startups and agile teams."
        },
        {
            "section": "Ruby on Rails",
            "question": "What are the key principles of the Model-View-Controller (MVC) architecture in Ruby on Rails?",
            "answer": "In Rails, the MVC architecture separates the application into models (data), views (UI), and controllers (business logic), promoting clean code organization and easier maintenance."
        },
        {
            "section": "Ruby on Rails",
            "question": "How does ActiveRecord work in Ruby on Rails?",
            "answer": "ActiveRecord is Rails' Object-Relational Mapping (ORM) library, which abstracts database interactions into Ruby objects, simplifying database operations like creating, reading, updating, and deleting records."
        },
        {
            "section": "Ruby on Rails",
            "question": "What is a migration in Ruby on Rails?",
            "answer": "Migrations in Rails are scripts that allow developers to modify the database schema, such as adding or removing columns, without losing data."
        },
        {
            "section": "Ruby on Rails",
            "question": "What are Rails callbacks, and when would you use them?",
            "answer": "Rails callbacks are methods that are called at specific points in an object's lifecycle, such as before or after saving, updating, or deleting records. They're useful for running custom logic during these stages."
        },
        {
            "section": "Swift (iOS)",
            "question": "What is Swift, and why is it preferred for iOS app development?",
            "answer": "Swift is a modern, open-source programming language developed by Apple, designed for building iOS, macOS, watchOS, and tvOS applications with a focus on safety, performance, and expressiveness."
        },
        {
            "section": "Swift (iOS)",
            "question": "What are optionals in Swift, and how do they help with null safety?",
            "answer": "Optionals in Swift are a type that can hold either a value or `nil`, allowing safe handling of the absence of a value and preventing runtime crashes due to null values."
        },
        {
            "section": "Swift (iOS)",
            "question": "What is the difference between `struct` and `class` in Swift?",
            "answer": "In Swift, `structs` are value types, meaning they are copied when passed around, while `classes` are reference types, meaning they are passed by reference, sharing the same instance."
        },
        {
            "section": "Swift (iOS)",
            "question": "What are closures in Swift, and how are they different from functions?",
            "answer": "Closures are self-contained blocks of code that can be passed around and used in your code, similar to functions, but they capture and store references to variables and constants from the surrounding context."
        },
        {
            "section": "Swift (iOS)",
            "question": "What is the purpose of the `guard` statement in Swift?",
            "answer": "The `guard` statement is used for early exits in functions or loops. It ensures that certain conditions are met, and if not, the function exits early, improving readability and reducing nested code."
        },
        {
            "section": "Hadoop",
            "question": "What is Hadoop, and how does its architecture work?",
            "answer": "Hadoop is an open-source framework for distributed storage and processing of large data sets. It consists of the Hadoop Distributed File System (HDFS) for storage and the MapReduce framework for processing data in parallel across clusters."
        },
        {
            "section": "Hadoop",
            "question": "What is HDFS, and what are its key components?",
            "answer": "HDFS (Hadoop Distributed File System) is designed to store large files across multiple machines. Its key components are the NameNode (which manages metadata) and DataNodes (which store data blocks)."
        },
        {
            "section": "Hadoop",
            "question": "What is MapReduce, and how does it work in Hadoop?",
            "answer": "MapReduce is a programming model for processing large data sets. The 'Map' phase processes input data and outputs key-value pairs, while the 'Reduce' phase aggregates the results from the 'Map' phase."
        },
        {
            "section": "Hadoop",
            "question": "What is YARN in Hadoop, and what is its role?",
            "answer": "YARN (Yet Another Resource Negotiator) is the resource management layer in Hadoop. It manages and schedules resources across the cluster for applications, ensuring efficient use of resources."
        },
        {
            "section": "Hadoop",
            "question": "What is Hadoop Hive, and how is it used?",
            "answer": "Hive is a data warehouse infrastructure built on top of Hadoop that provides a query language (HQL) similar to SQL for querying large datasets stored in HDFS."
        },
        {
            "section": "Kotlin (Android)",
            "question": "What is Kotlin, and how is it used in Android development?",
            "answer": "Kotlin is a modern, statically-typed programming language developed by JetBrains. It is used in Android development as a more concise and safe alternative to Java."
        },
        {
            "section": "Kotlin (Android)",
            "question": "What are the key benefits of using Kotlin for Android development over Java?",
            "answer": "Kotlin offers enhanced syntax, null safety, higher-order functions, and better integration with Android Studio compared to Java, making it more efficient for Android development."
        },
        {
            "section": "Kotlin (Android)",
            "question": "Explain Kotlin's null safety feature.",
            "answer": "Kotlin's null safety feature prevents null pointer exceptions by distinguishing between nullable and non-nullable types. It requires explicit handling of null values using `?` for nullable types."
        },
        {
            "section": "Kotlin (Android)",
            "question": "How does Kotlin handle coroutines for asynchronous programming?",
            "answer": "Kotlin coroutines simplify asynchronous programming by allowing developers to write asynchronous code in a sequential manner using `suspend` functions and `async/await` constructs."
        },
        {
            "section": "Kotlin (Android)",
            "question": "What are `data` classes in Kotlin and how are they used?",
            "answer": "`data` classes in Kotlin are special classes designed to hold data. They automatically generate functions like `toString()`, `equals()`, and `hashCode()`, reducing boilerplate code."
        },
        {
            "section": "API Management",
            "question": "What is API management, and why is it important for modern applications?",
            "answer": "API management involves the creation, deployment, monitoring, and security of APIs in order to ensure consistent, secure, and reliable communication between services and applications."
        },
        {
            "section": "API Management",
            "question": "What is an API Gateway, and what role does it play in microservices architecture?",
            "answer": "An API Gateway is a server that acts as an entry point for client requests in a microservices architecture. It handles routing, load balancing, security, and rate limiting."
        },
        {
            "section": "API Management",
            "question": "How do you implement rate limiting in an API?",
            "answer": "Rate limiting can be implemented using tools like API Gateways or by tracking API usage with tokens or counters. Limits are set to restrict the number of requests from a user in a defined time period."
        },
        {
            "section": "API Management",
            "question": "What is OAuth, and how does it work in API security?",
            "answer": "OAuth is an open-standard authorization protocol that allows third-party applications to securely access user data without exposing credentials. It uses access tokens to authorize API calls."
        },
        {
            "section": "API Management",
            "question": "What is API versioning, and why is it necessary?",
            "answer": "API versioning ensures backward compatibility while enabling developers to introduce new features or breaking changes in the API without affecting existing users."
        },
        {
            "section": "WebSockets",
            "question": "What are WebSockets, and how do they differ from traditional HTTP requests?",
            "answer": "WebSockets provide a full-duplex communication channel that allows bidirectional data flow between client and server over a single TCP connection, unlike HTTP, which is request-response based."
        },
        {
            "section": "WebSockets",
            "question": "How do you establish a WebSocket connection in a browser?",
            "answer": "A WebSocket connection is established using the JavaScript WebSocket API, where the client initiates a handshake using `new WebSocket('ws://url')` to connect to the server."
        },
        {
            "section": "WebSockets",
            "question": "What are some use cases for WebSockets in web applications?",
            "answer": "WebSockets are commonly used in real-time applications like chat applications, online gaming, stock price updates, and collaborative editing."
        },
        {
            "section": "WebSockets",
            "question": "How do you handle message broadcasting in WebSockets?",
            "answer": "Message broadcasting in WebSockets can be handled by maintaining a list of connected clients and then sending a message to each client in the list. This is typically done on the server-side."
        },
        {
            "section": "WebSockets",
            "question": "How does WebSocket manage connection lifecycle, and what are some common errors?",
            "answer": "WebSocket manages its lifecycle through events like `onopen`, `onmessage`, `onclose`, and `onerror`. Common errors include connection failures and message transmission errors, often handled with retries or error handling functions."
        },
        {
            "section": "NLP Libraries",
            "question": "What is spaCy, and how is it used in natural language processing?",
            "answer": "spaCy is an open-source library for advanced natural language processing (NLP) in Python. It provides fast and accurate tools for tokenization, part-of-speech tagging, named entity recognition (NER), and dependency parsing."
        },
        {
            "section": "NLP Libraries",
            "question": "How does Hugging Face's `Transformers` library help in NLP tasks?",
            "answer": "Hugging Face's `Transformers` library provides state-of-the-art pre-trained models for various NLP tasks, such as text classification, translation, summarization, and question answering. It simplifies the use of transformer models like BERT, GPT, and T5 for NLP applications."
        },
        {
            "section": "NLP Libraries",
            "question": "What is tokenization, and why is it important in NLP?",
            "answer": "Tokenization is the process of splitting text into individual words, subwords, or characters. It is important because it converts raw text into a format that can be understood by machine learning models for NLP tasks like sentiment analysis or machine translation."
        },
        {
            "section": "NLP Libraries",
            "question": "What is Named Entity Recognition (NER), and how does spaCy perform it?",
            "answer": "NER is a task in NLP where entities such as names, dates, locations, and organizations are identified in text. spaCy uses pre-trained models to detect and classify entities in text with high accuracy."
        }
    ]

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
fetch_questions_by_keyword("python")


def count_unique_sections():
    # Initialize ChromaDB client
    client = chromadb.PersistentClient("vectorstore")
    collection = client.get_collection(name="interview_questions")

    # Fetch all metadata from the collection
    all_documents = collection.get(include=["metadatas"])

    # Extract sections from metadata
    sections = [metadata["Section"] for metadata in all_documents["metadatas"]]

    # Count unique sections
    unique_sections = set(sections)
    print(f"Total unique sections: {len(unique_sections)}")
    print(f"Sections: {unique_sections}")

#count_unique_sections()