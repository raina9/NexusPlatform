# Nexus Platform API Collection

Base URL

http://localhost:9191

---

# Employee APIs

| API Name                      | Method | URL                                                |
| ----------------------------- | ------ | -------------------------------------------------- |
| Create Employee               | POST   | /api/employees                                     |
| Get Employee By Id            | GET    | /api/employees/{id}                                |
| Get All Employees             | GET    | /api/employees                                     |
| Delete Employee               | DELETE | /api/employees/{id}                                |
| Employee Pagination           | GET    | /api/employees/page?page=0&size=5                  |
| Employee Pagination + Sorting | GET    | /api/employees/page?page=0&size=5&sort=salary,desc |
| Employee Search               | GET    | /api/employees/search?firstName=shiv               |
| Employee Specification Search | GET    | /api/employees/specification?firstName=shiv        |
| Employee Salary Search (JPQL) | GET    | /api/employees/salary?salary=50000                 |

### Create Employee Request Body

```json
{
  "firstName": "Shivendra",
  "lastName": "Raina",
  "email": "shivendra@gmail.com",
  "salary": 120000
}
```

---

# Department APIs

| API Name             | Method | URL                   |
| -------------------- | ------ | --------------------- |
| Create Department    | POST   | /api/departments      |
| Get Department By Id | GET    | /api/departments/{id} |
| Get All Departments  | GET    | /api/departments      |
| Delete Department    | DELETE | /api/departments/{id} |

### Create Department Request Body

```json
{
  "departmentName": "Engineering"
}
```

---

# WebClient APIs

| API Name                   | Method | URL                         |
| -------------------------- | ------ | --------------------------- |
| Employee WebClient Proxy   | GET    | /webclient/employees/{id}   |
| Department WebClient Proxy | GET    | /webclient/departments/{id} |

---

# JPA Features Covered

| Feature              | Endpoint                             |
| -------------------- | ------------------------------------ |
| Pagination           | /api/employees/page                  |
| Sorting              | /api/employees/page?sort=salary,desc |
| Search API           | /api/employees/search                |
| Specification API    | /api/employees/specification         |
| JPQL Query           | /api/employees/salary                |
| Native Query         | Repository Layer                     |
| Interface Projection | Repository Layer                     |
| DTO Projection       | Repository Layer                     |

---

# Testing Coverage

| Layer              | Technology        |
| ------------------ | ----------------- |
| Service Testing    | JUnit 5 + Mockito |
| Controller Testing | MockMvc           |

---

# Databases

Employee Database

MySQL

```text
employeesdb
```

Department Database

PostgreSQL

```text
departmentsdb
```

---

# Relationships

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

Concepts Covered

* OneToMany
* ManyToOne
* FetchType.LAZY
* CascadeType.ALL
* orphanRemoval

---

# Inter-Service Communication

| Technology   | Status      |
| ------------ | ----------- |
| RestTemplate | Implemented |
| WebClient    | Implemented |
| OpenFeign    | Deferred    |

---

# Logging

Implemented Using

* SLF4J
* Logback

Log Levels

* TRACE
* DEBUG
* INFO
* WARN
* ERROR

---

# Project Status

Current Scope Completed

✓ Multi Datasource

✓ CRUD APIs

✓ DTO Layer

✓ Validation

✓ Global Exception Handling

✓ ApiResponse

✓ ErrorResponse

✓ Pagination

✓ Sorting

✓ Search API

✓ Specification API

✓ Relationships

✓ JPQL

✓ Native Query

✓ Projection

✓ JUnit 5

✓ Mockito

✓ MockMvc

✓ RestTemplate

✓ WebClient

✓ Logging
