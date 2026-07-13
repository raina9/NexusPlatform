# NEXUS PLATFORM — CLAUDE.md
# Governed by: Root Workspace CLAUDE.md (Global Brain v1.0)
# Location: Workspace/Projects/NexusPlatform/
# Last Updated: 2026-07-13

---

## INHERITANCE

This file inherits ALL rules from the Root Workspace CLAUDE.md.
Golden Rules, 100 Engineering Parameters, Error Handling Standard,
Open Source Release Standard, Session Output Rule, Mentor Behaviour —
all apply unless explicitly overridden below.

---

## PROJECT IDENTITY

```
Project Name  : Nexus Platform
Purpose       : Enterprise backend reference implementation + Interview preparation
Group Id      : com.raina.nexus
Artifact Id   : nexus-platform
Base Package  : com.raina.nexus
Local Path    : C:\Users\shive\Shivendra_Workspace\nexus-platform
Status        : Active — Stabilisation + Migration Phase
```

---

## JAVA VERSION POLICY (LOCAL OVERRIDE)

Root workspace mandates Java 25 LTS.
Nexus Platform runs Java 17 — this is intentional and permanent for this project.

**Reason:** Nexus exists as an interview reference for Spring Boot 3.x + Java 17
enterprise patterns. Most production codebases in 2026 interviews still reference
Java 17 LTS. Migrating to Java 25 + Spring Boot 4.x would create a separate
learning track — that belongs in a new project, not here.

**Rule:** Never upgrade Nexus to Java 25 unless explicitly instructed.

---

## CURRENT TECH STACK

```
Java            : 17
Spring Boot     : 3.5.4
Spring Cloud    : 2025.0.0 (BOM)
Build           : Maven (with wrapper)
Primary DB      : MySQL 8 (Employee domain)
Secondary DB    : PostgreSQL (Department domain)
REST Clients    : RestTemplate + WebClient + OpenFeign
Validation      : Spring Bean Validation
Lombok          : Yes
Testing         : JUnit 5 + Mockito + MockMvc
Logging         : SLF4J + Logback
```

**Not yet implemented (Future Roadmap):**
Swagger/OpenAPI, Spring Security, JWT, OAuth2, Kafka, Redis,
Docker, Docker Compose, Flyway, Actuator, AWS, CI/CD, TestContainers

---

## BUSINESS DOMAIN

### MySQL — Employee DB
```
employeesdb
Entity: Employee
Fields: id, name, email, salary, departmentId (Long — soft reference only)
Address: @OneToMany relationship exists at entity level
```

### PostgreSQL — Department DB
```
departmentsdb
Entity: Department
Fields: id, departmentName, location
```

**Architectural Decision (locked):**
Employee stores `departmentId (Long)` — NOT `@ManyToOne Department`.
Reason: Cross-database JPA relationships are impossible — entities
must belong to the same persistence unit.
Never revert this decision.

---

## PACKAGE STRUCTURE

```
com.raina.nexus
├── client
│   ├── resttemplate
│   ├── feign
│   └── webclient
├── common
│   └── response          ← ApiResponse, ErrorResponse (CANONICAL location)
├── config                ← EmployeeDataSourceConfig, DepartmentDataSourceConfig, RestTemplateConfig
├── department
│   ├── controller
│   ├── dto
│   ├── repository
│   └── service
├── employee
│   ├── controller
│   ├── dto
│   ├── projection
│   ├── repository
│   ├── service
│   └── specification
├── entity
│   ├── department
│   └── employee
├── exception             ← GlobalExceptionHandler, ResourceNotFoundException
│                           ErrorResponse HERE is DEAD CODE — must be removed
└── external
    └── weather
        ├── client
        ├── controller
        ├── dto
        └── service
```

---

## CURRENT STATUS (Scanned: 2026-07-13)

### Completed and Working
- Employee CRUD + Pagination + Sorting + FirstN Search + JPQL salary filter
- Department CRUD (full)
- Dual datasource — MySQL (Primary) + PostgreSQL — separate EntityManagers + TransactionManagers
- Global Exception Handling via @RestControllerAdvice
- Generic ApiResponse<T> wrapper
- Bean Validation
- Weather API — 3 client implementations (RestTemplate / WebClient / Feign)
- SLF4J + Logback logging
- JUnit 5 + Mockito service tests + MockMvc controller tests (Employee + Department only)

### Implemented but NOT Exposed (Dead from API surface)
- Native Query in EmployeeRepository — no controller endpoint
- Interface Projection in EmployeeRepository — no controller endpoint
- DTO Projection in EmployeeRepository — no controller endpoint
- DepartmentWebClient — wired but no controller endpoint
- Address — @OneToMany on Employee exists, AddressRepository exists, NO service, NO controller

### Known Issues (Must Fix Before Next Phase)
1. **Duplicate ErrorResponse** — exists in `common.response` AND `exception` package
   Fix: Delete `exception.ErrorResponse` — keep only `common.response.ErrorResponse`

2. **Hardcoded DB credentials** in application.yaml
   Fix: Move to environment profiles (application-local.yml, application-dev.yml)
   Use `${DB_PASSWORD}` env var pattern

3. **No per-environment profiles** — single application.yaml does everything
   Fix: Split into application.yml (base) + application-local.yml

4. **Address has no API surface** — entity + repository exists, nothing above
   Fix: Implement AddressService + AddressController as sub-resource of Employee

5. **Test coverage gaps** — Address, weather, pagination/search/spec endpoints untested

6. **REST clients call self** (localhost) — not a separate service
   Note: Acceptable for learning demo, but document this clearly

---

## CONFIGURATION STRATEGY

Use ONLY YAML. Never properties files.

```
application.yml          ← base config, no secrets
application-local.yml    ← local DB credentials, dev settings
application-dev.yml      ← dev environment overrides
application-test.yml     ← test DB config
```

Credentials must come from environment variables:
```yaml
spring:
  datasource:
    password: ${DB_EMPLOYEE_PASSWORD}
```

Never hardcode passwords. Not even for learning projects.

---

## MENTOR BEHAVIOUR (Nexus-specific)

When I say `Continue Nexus Platform`, always respond with:
1. Current Project Status
2. Completed Components
3. Known Issues count
4. Current Task
5. Why this task is next
6. Expected Learning Outcome

Only then generate code.

When implementing any feature, follow the 12-step structure:
Objective → Current Status → Folder Location → Complete Code →
Internal Working → Tradeoffs → Interview Questions → Production Notes →
Common Mistakes → Testing → Troubleshooting → Next Step

---

## STABILISATION PLAN (Do in order)

### Phase 0 — Cleanup (Before anything new)
- [ ] Delete duplicate `exception.ErrorResponse`
- [ ] Extract DB credentials to env vars + profiles
- [ ] Expose Native Query endpoint
- [ ] Expose Interface Projection endpoint
- [ ] Expose DTO Projection endpoint
- [ ] Expose DepartmentWebClient endpoint
- [ ] Implement AddressService + AddressController

### Phase 1 — Testing
- [ ] Address tests
- [ ] Weather client tests
- [ ] Pagination + Search + Specification tests
- [ ] Exception handler tests

### Phase 2 — Documentation
- [ ] Release Notes
- [ ] Architecture Notes
- [ ] JPA Notes
- [ ] REST Client Notes
- [ ] Interview Q&A Bank
- [ ] One-Line Revision Sheets

### Phase 3 — Future Features
Swagger/OpenAPI → Spring Security → JWT → Kafka → Redis →
Docker → Flyway → Actuator → TestContainers

---

## NON-NEGOTIABLES (Inherited + Nexus-specific)

- No feature without tests
- No hardcoded credentials ever
- No duplicate classes
- No System.out.println — SLF4J only
- No swallowed exceptions
- Error codes must be specific: EMPLOYEE_NOT_FOUND, not ERROR_001
- Every new endpoint must be in ApiResponse<T> wrapper
- Builder pattern for test object creation (not constructors)
- ddl-auto: update only in dev — never in prod profiles

---

## SESSION OUTPUT RULE

Every session producing code or notes must write to:
```
nexus-platform/session-notes/nexus-session-notes-vN.txt
```
Newest content prepended. FILE INFORMATION header required.
