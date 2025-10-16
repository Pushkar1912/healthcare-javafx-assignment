# How to Run This Healthcare Project

## What You Need First

- **Java 17 or higher** (the programming language our app uses)
- **Maven** (helps manage our project dependencies)
- **MySQL** (our database)


## Getting Started

### Step 1: Extract the Project


### Step 2: Run Docker Compose file (it will initialise its db)

```
docker compose up -d
````

### Step 3: Run the Application

#### On Mac:
```bash
# Make sure you're in the project folder
cd healthcare-javafx

# Install dependencies and run
mvn clean compile
mvn javafx:run
```

#### On Windows:
```cmd
# Open Command Prompt in the project folder
cd healthcare-javafx

# Install dependencies and run
mvn clean compile
mvn javafx:run
```

## Running Tests

To make sure everything works properly:

```bash
# Run all tests
mvn test

# Run specific tests
mvn test -Dtest=SimpleBusinessLogicTest
mvn test -Dtest=HealthcareSystemIntegrationTest
```



