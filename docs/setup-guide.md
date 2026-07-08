# How to Run This Healthcare Project

## What You Need First

- **Java 17 or higher** (the programming language the app uses)
- **Maven** (helps manage project dependencies)
- **MySQL** (the database)
- **Docker & Docker Compose** (for running MySQL + phpMyAdmin locally)

## Getting Started

### Step 1: Clone the Repository

```bash
git clone https://github.com/Pushkar1912/healthcare-javafx-assignment.git
cd healthcare-javafx-assignment
```

### Step 2: Start the Database

```bash
docker compose up -d
```

This starts MySQL on port 3306 and phpMyAdmin on port 8080. Give it about 45 seconds to finish initializing before moving on.

### Step 3: Run the Application

#### On Mac / Linux:
```bash
mvn clean compile
mvn javafx:run
```

#### On Windows:
```cmd
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
