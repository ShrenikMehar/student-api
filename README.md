# Student REST API

## Overview

This project implements a REST API for managing student records.

The service is built using **Kotlin** and the **Micronaut** framework and follows good backend engineering practices such as:

* layered architecture (controller → service → repository)
* database migrations using Flyway
* automated linting and formatting
* unit testing
* CI pipeline with GitHub Actions
* containerized deployment using Docker
* infrastructure provisioning using Vagrant

Student data is stored in **PostgreSQL**.

---

## Features

The API supports:

* Create a student
* Retrieve all students
* Retrieve a student by ID
* Update student information
* Delete a student record
* Health check endpoint

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
* **Database:** PostgreSQL
* **Database Migration:** Flyway
* **Build Tool:** Gradle
* **Testing:** JUnit 5
* **Logging:** Logback
* **Linting:** Detekt
* **Formatting:** ktlint
* **CI/CD:** GitHub Actions
* **Containerization:** Docker
* **Infrastructure:** Vagrant

---

# Running the Application

## Prerequisites

Install the following tools:

* Docker
* Make
* Java 21 (only required for local development)

Verify installation:

```
docker --version
make --version
java -version
```

---

## Run Using Docker (Recommended)

The easiest way to run the project is using Docker.

### Build the image

```
make build
```

### Start the application

```
make run
```

This starts:

* PostgreSQL database
* Student REST API

The API will be available at:

```
http://localhost:8080
```

Health check endpoint:

```
http://localhost:8080/healthcheck
```

### Stop the application

```
make stop
```

### View logs

```
make logs
```

---

## Local Development (Without Docker)

Start PostgreSQL:

```
make db-up
```

Run the application:

```
make local-run
```

Stop PostgreSQL:

```
make db-down
```

---

# Deployment Using Vagrant

The project can also run inside a **virtual machine** using Vagrant.
This simulates a production-like deployment environment where services run inside containers.

## Architecture

```
Client
   │
   ▼
Nginx
   │
   ├── Student API (instance 1)
   └── Student API (instance 2)
          │
          ▼
        PostgreSQL
```

---

## Infrastructure Layout

```
infra/
 ├── provision.sh
 ├── docker-compose.yml
 └── nginx/
     └── nginx.conf

Vagrantfile
```

---

# Installing Vagrant

## macOS (Using Homebrew)

Install Vagrant:

```
brew install vagrant
```

Verify installation:

```
vagrant --version
```

---

## Apple Silicon (M1/M2/M3/M4) Setup

VirtualBox does **not support Apple Silicon processors**.

This project uses **UTM as the Vagrant provider** instead.

### Install UTM

Download from:

```
https://mac.getutm.app/
```

### Install the UTM provider plugin

```
vagrant plugin install vagrant_utm
```

Verify installation:

```
vagrant plugin list
```

Expected output:

```
vagrant_utm
```

---

## Vagrant Box Used

The project uses an ARM-compatible box:

```
utm/bookworm
```

This provides a **Debian 12 ARM64 environment** compatible with Apple Silicon.

---

# Running the Application in the VM

Start the VM:

```
vagrant up
```

SSH into the VM:

```
vagrant ssh
```

Start the services:

```
cd /vagrant/infra
docker-compose up -d
```

This will start:

* PostgreSQL container
* Student API containers
* Nginx reverse proxy

---

## Verify the API

```
curl http://localhost:8080/healthcheck
```

Expected response:

```
OK
```

---

## Stop Services

Inside the VM:

```
docker-compose down
```

Stop the VM:

```
vagrant halt
```

Destroy the VM:

```
vagrant destroy
```

---

# Tested Environment

The infrastructure setup has been tested on:

```
MacBook (Apple Silicon M4)
macOS
UTM
Vagrant
Docker
```

---

# Running Tests

```
make local-test
```

---

# Code Quality

Run lint checks:

```
make lint
```

Format code automatically:

```
make format
```

---

# Continuous Integration

GitHub Actions runs the following on every push:

* build
* lint checks
* formatting validation
* unit tests
* Docker image build and push

---

# Postman Collection

API requests can be tested using the provided Postman collection.

Location:

```
postman/student-api.postman_collection.json
```

Import the collection into Postman and run requests against:

```
http://localhost:8080
```

---

# Project Structure

```
src/main/kotlin
  controller     → REST controllers
  service        → business logic
  repository     → database access
  entity         → database entities
  dto            → request/response models

src/main/resources
  db/migration   → Flyway migrations

src/test/kotlin  → unit tests

infra            → infrastructure configuration (Vagrant deployment)

scripts          → developer setup scripts
postman          → API testing collection
```

---

# Future Improvements

Possible enhancements:

* request validation improvements
* pagination support
* API documentation (OpenAPI / Swagger)
* container orchestration using Kubernetes
* monitoring and observability stack
