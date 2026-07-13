# Nexus Platform — cURL Collection

Base URL for all commands: `http://localhost:9191`

Generated from the actual controller code in:
`employee/controller/`, `department/controller/`, `external/weather/controller/`,
`client/controller/`, `client/webclient/`, `client/feign/`, `employee/controller/TestController.java`,
`employee/controller/FeignTestController.java`.

---

## EMPLOYEE

```bash
# Create an employee
curl -X POST http://localhost:9191/api/employees \
  -H "Content-Type: application/json" \
  -d '{
        "firstName": "Shivendra",
        "lastName": "Raina",
        "email": "shivendra@gmail.com",
        "salary": 100000.0
      }'

# Get an employee by id
curl -X GET http://localhost:9191/api/employees/1

# Get all employees (no pagination)
curl -X GET http://localhost:9191/api/employees

# Delete an employee by id
curl -X DELETE http://localhost:9191/api/employees/1

# Offset pagination (page/size/sort)
curl -X GET "http://localhost:9191/api/employees/page?page=0&size=10&sort=firstName,asc"

# FirstN / substring search by first name
curl -X GET "http://localhost:9191/api/employees/search?firstName=Shiv"

# Specification-based search by first name
curl -X GET "http://localhost:9191/api/employees/specification?firstName=Shiv"

# JPQL salary filter — employees with salary greater than the given amount
curl -X GET "http://localhost:9191/api/employees/salary?salary=50000"

# Native SQL query — employees with salary greater than the given amount
curl -X GET "http://localhost:9191/api/employees/native?salary=50000"

# Cursor pagination — first page (cursor=0 means start from beginning)
curl -X GET "http://localhost:9191/api/employees/cursor?cursor=0&size=10"

# Cursor pagination — next page using the previous response's nextCursor
curl -X GET "http://localhost:9191/api/employees/cursor?cursor=10&size=10"

# Keyset pagination (native query) — first page
curl -X GET "http://localhost:9191/api/employees/keyset?afterId=0&size=10"

# Keyset pagination (native query) — next page using the previous response's nextCursor
curl -X GET "http://localhost:9191/api/employees/keyset?afterId=10&size=10"

# Interface projection — offset pagination
curl -X GET "http://localhost:9191/api/employees/projection/interface?page=0&size=10&sort=firstName,asc"

# Interface projection — cursor pagination
curl -X GET "http://localhost:9191/api/employees/projection/interface/cursor?cursor=0&size=10"

# Interface projection — keyset pagination (native query)
curl -X GET "http://localhost:9191/api/employees/projection/interface/keyset?afterId=0&size=10"

# DTO projection — offset pagination
curl -X GET "http://localhost:9191/api/employees/projection/dto?page=0&size=10&sort=firstName,asc"

# DTO projection — cursor pagination
curl -X GET "http://localhost:9191/api/employees/projection/dto/cursor?cursor=0&size=10"

# DTO projection — keyset pagination (native query)
curl -X GET "http://localhost:9191/api/employees/projection/dto/keyset?afterId=0&size=10"
```

---

## ADDRESS

All routes are nested under an employee: `/api/employees/{employeeId}/addresses`.

```bash
# Create an address for employee 1
curl -X POST http://localhost:9191/api/employees/1/addresses \
  -H "Content-Type: application/json" \
  -d '{
        "city": "Pune",
        "state": "Maharashtra",
        "country": "India"
      }'

# Get all addresses for employee 1
curl -X GET http://localhost:9191/api/employees/1/addresses

# Get a single address by id for employee 1
curl -X GET http://localhost:9191/api/employees/1/addresses/1

# Delete an address by id for employee 1
curl -X DELETE http://localhost:9191/api/employees/1/addresses/1
```

---

## DEPARTMENT

```bash
# Create a department
curl -X POST http://localhost:9191/api/departments \
  -H "Content-Type: application/json" \
  -d '{
        "departmentName": "Engineering"
      }'

# Get a department by id
curl -X GET http://localhost:9191/api/departments/1

# Get all departments
curl -X GET http://localhost:9191/api/departments

# Delete a department by id
curl -X DELETE http://localhost:9191/api/departments/1

# Get a department by id via WebClient (self-call through DepartmentWebClient)
curl -X GET http://localhost:9191/api/departments/webclient/1
```

---

## WEATHER

```bash
# Current weather for a city via RestTemplate
curl -X GET http://localhost:9191/api/weather/rest/London

# Current weather for a city via WebClient
curl -X GET http://localhost:9191/api/weather/webclient/London

# Current weather for a city via OpenFeign
curl -X GET http://localhost:9191/api/weather/feign/London
```

---

## REST CLIENTS (self-call demo endpoints)

These endpoints demonstrate calling the Employee API back through each of the
three HTTP client styles (RestTemplate, WebClient, Feign), all targeting this
same running instance on `localhost:9191`.

```bash
# Fetch employee 1 via RestTemplate (EmployeeClient -> GET /api/employees/{id})
curl -X GET http://localhost:9191/test/employee/1

# Fetch employee 1 via WebClient (EmployeeWebClient -> GET /api/employees/{id})
curl -X GET http://localhost:9191/webclient/employees/1

# Fetch employee 1 via OpenFeign (EmployeeFeignClient -> GET /api/employees/{id})
curl -X GET http://localhost:9191/feign/employee/1
```
