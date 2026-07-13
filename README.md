# Nexus Platform

## Project Overview

Nexus Platform is a Spring Boot 3.5 and Java 17 based backend application designed to demonstrate modern enterprise application development practices.

The project started as a simple CRUD application and gradually evolved into a production-style backend system covering advanced JPA, multi-datasource configuration, testing, validation, exception handling, inter-service communication, and logging.

The objective of the project is to provide a practical reference implementation for Spring Boot interviews and enterprise backend development.

---

## Features

### Core Features

* Employee CRUD Operations
* Department CRUD Operations
* DTO-Based API Design
* Request Validation
* Global Exception Handling
* Generic API Response Wrapper

### Advanced JPA Features

* Pagination
* Sorting
* Search APIs
* Specification API
* JPQL Queries
* Native Queries
* Interface Projection
* DTO Projection

### Database Features

* Multi-Datasource Configuration
* MySQL Integration
* PostgreSQL Integration
* Entity Relationships
* Lazy Loading
* Cascade Operations

### Testing

* JUnit 5
* Mockito
* MockMvc

### Inter-Service Communication

* RestTemplate
* WebClient

### Observability

* SLF4J Logging
* Logback

---

## Technology Stack

* Java 17
* Spring Boot 3.5
* Spring Data JPA
* Hibernate
* MySQL
* PostgreSQL
* Lombok
* Maven
* JUnit 5
* Mockito
* MockMvc
* RestTemplate
* WebClient

---

## Architecture

The application follows a layered architecture.

```text
Controller Layer
       ↓
Service Layer
       ↓
Repository Layer
       ↓
Database Layer
```

Supporting Layers:

* DTO Layer
* Validation Layer
* Exception Handling Layer
* Logging Layer

---

## Database Design

### Employee Database (MySQL)

```text
employees
---------
id
first_name
last_name
email
salary
department_id
```

### Department Database (PostgreSQL)

```text
departments
-----------
id
department_name
```

### Address Table

```text
addresses
---------
id
city
state
country
employee_id
```

### Relationships

```text
Department
    1
    ↓
Many
Employee

Employee
    1
    ↓
Many
Address
```

Covered Concepts:

* OneToMany
* ManyToOne
* FetchType.LAZY
* CascadeType.ALL
* orphanRemoval

---

## API Endpoints

### Employee APIs

| Method | Endpoint                     |
| ------ | ---------------------------- |
| POST   | /api/employees               |
| GET    | /api/employees/{id}          |
| GET    | /api/employees               |
| DELETE | /api/employees/{id}          |
| GET    | /api/employees/page          |
| GET    | /api/employees/search        |
| GET    | /api/employees/specification |
| GET    | /api/employees/salary        |

### Department APIs

| Method | Endpoint              |
| ------ | --------------------- |
| POST   | /api/departments      |
| GET    | /api/departments/{id} |
| GET    | /api/departments      |
| DELETE | /api/departments/{id} |

---

## Advanced JPA Features

Implemented Features:

1. Pagination
2. Sorting
3. Search APIs
4. Specification API
5. JPQL Queries
6. Native Queries
7. Interface Projection
8. DTO Projection

Benefits:

* Scalable Data Retrieval
* Dynamic Filtering
* Better Query Performance
* Reduced Data Transfer

---

## Inter-Service Communication

### RestTemplate

Used for synchronous HTTP communication.

Characteristics:

* Blocking
* Traditional Approach

### WebClient

Used for modern HTTP communication.

Characteristics:

* Reactive
* Non-Blocking
* Recommended for New Applications

---

## Testing

Implemented Using:

* JUnit 5
* Mockito
* MockMvc

Coverage:

* Service Layer
* Controller Layer

Benefits:

* Improved Reliability
* Safer Refactoring
* Better Maintainability

---

## Logging

Implemented Using:

* SLF4J
* Logback

Log Levels:

* TRACE
* DEBUG
* INFO
* WARN
* ERROR

Benefits:

* Production Monitoring
* Easier Debugging
* Better Observability

---

## Key Learning Outcomes

This project demonstrates:

* Enterprise Layered Architecture
* Multi-Datasource Configuration
* Advanced JPA Features
* Validation and Exception Handling
* Generic API Design
* Inter-Service Communication
* Unit Testing
* Logging Best Practices

---

## Future Enhancements

The following topics are intentionally kept outside the current scope:

* Spring Security
* JWT Authentication
* OAuth2
* Kafka
* Docker
* AWS
* Flyway
* Actuator
* MapStruct

These can be implemented as separate advanced modules in future iterations.

# External API Integration

The application demonstrates integration with third-party REST APIs using three different Spring communication mechanisms.

## Provider

Weather API

Documentation:

https://www.weatherapi.com/docs/

---

## Authentication

The Weather API uses API Key based authentication.

Configuration is externalized using Spring Environment Variables.

Example:

```yaml
external:

  weather:

    base-url: https://api.weatherapi.com/v1

    api-key: ${WEATHER_API_KEY}
```

Benefits:

* No hardcoded credentials
* Environment specific configuration
* Production ready approach

---

## RestTemplate Integration

Endpoint:

```
GET /api/weather/rest/{city}
```

Example:

```
GET /api/weather/rest/London
```

Purpose:

* Traditional synchronous HTTP communication
* Blocking I/O model
* Easy to understand and implement

---

## WebClient Integration

Endpoint:

```
GET /api/weather/webclient/{city}
```

Example:

```
GET /api/weather/webclient/London
```

Purpose:

* Modern HTTP client
* Reactive programming support
* Non-blocking communication
* Recommended for new Spring applications

---

## OpenFeign Integration

Endpoint:

```
GET /api/weather/feign/{city}
```

Example:

```
GET /api/weather/feign/London
```

Purpose:

* Declarative HTTP client
* Reduced boilerplate code
* Clean interface-based integrations
* Commonly used in microservice architectures

---

## Learning Outcomes

This module demonstrates:

* External API Integration
* API Key Authentication
* Environment Variables
* DTO Mapping
* RestTemplate
* WebClient
* OpenFeign
* Service Layer Design
* Controller Layer Design
* Logging Best Practices

---

## Future Enhancements

The following enterprise integrations can be implemented using the same architecture:

* DigiLocker
* Aadhaar Verification
* PAN Verification
* GST Verification
* CKYC Verification
* Payment Gateway Integration
* Shipping Provider Integration
* Notification Services
