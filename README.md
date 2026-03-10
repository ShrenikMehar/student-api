# Student REST API

## Overview

This project implements a simple REST API to manage student records.

The API allows clients to create, retrieve, update, and delete student information.
The service is implemented using Kotlin and the Micronaut framework and follows common REST API best practices along with principles inspired by the Twelve-Factor App methodology.

The project also demonstrates good backend engineering practices such as:

* automated formatting and linting
* unit testing
* CI validation
* structured logging
* Git hooks for local quality checks

---

## Features

The API currently supports the following operations:

* Add a new student
* Get all students
* Get a student by ID
* Update student information
* Delete a student record
* Health check endpoint

Students are currently stored using an **in-memory data store**.
A database layer will be introduced in a later milestone.

---

## API Base Path

```
/api/v1/students
```

---

## API Endpoints

| Method | Endpoint              | Description                 |
| ------ | --------------------- | --------------------------- |
| POST   | /api/v1/students      | Create a new student        |
| GET    | /api/v1/students      | Retrieve all students       |
| GET    | /api/v1/students/{id} | Retrieve a specific student |
| PUT    | /api/v1/students/{id} | Update a student            |
| DELETE | /api/v1/students/{id} | Delete a student            |
| GET    | /healthcheck          | Service health status       |

---

## Tech Stack

* **Language:** Kotlin
* **Framework:** Micronaut
* **Build Tool:** Gradle
* **Testing:** JUnit 5
* **Logging:** Logback
* **Linting:** Detekt
* **Formatting:** ktlint
* **CI:** GitHub Actions

---

## Code Quality

### Static Analysis

Detekt is used for Kotlin static analysis.

```
make lint
```

---

### Code Formatting

ktlint automatically enforces Kotlin code formatting.

Check formatting:

```
make lint
```

Automatically fix formatting:

```
make format
```

---

## Git Hooks

Local Git hooks ensure code quality before commits and pushes.

### Pre-commit

* Run ktlint formatter
* Run detekt lint checks

### Pre-push

* Run unit tests

Install hooks after cloning the repository:

```
./scripts/setup-git-hooks.sh
```

---

## Continuous Integration

A CI pipeline is configured using GitHub Actions.

Every push and pull request automatically runs:

* project build
* static analysis (Detekt)
* formatting checks (ktlint)
* unit tests

---

## Prerequisites

The following tools must be installed before running the project locally.

### Java

This project requires **Java 21**.

Check your installed version:

```
java -version
```

Expected output example:

```
openjdk version "21.x"
```

The Gradle build is configured to use the **Java 21 toolchain**.

---

### Git

Git is required to clone the repository and enable the provided Git hooks.

```
git --version
```

---

### Gradle

No manual installation is required.

The project uses the **Gradle Wrapper**, so all build commands are executed through Gradle automatically.

---

## Running the Application

Start the API locally:

```
make run
```

The service will start on the default Micronaut port (`http://localhost:8080`).

---

## Running Tests

Run the test suite:

```
make test
```

---

## Build

Build the project:

```
make build
```

---

## Postman Collection

A Postman collection is included to test the API.

Location:

```
postman/student-api.postman_collection.json
```

### Import

1. Open Postman
2. Click **Import**
3. Select the file above

Ensure the application is running (`http://localhost:8080`) before sending requests.

---

## Project Structure

```
src/main/kotlin    → application source code
src/test/kotlin    → unit tests
.githooks          → git hooks (pre-commit, pre-push)
scripts            → developer setup scripts
postman            → API testing collection
```

---

## Planned Improvements

The following features will be added in future milestones:

* PostgreSQL database integration
* Flyway database migrations
* Repository layer
* environment based configuration
* OpenAPI / Swagger documentation
* API request validation
* pagination support
* Docker support
