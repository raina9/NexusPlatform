# NEXUS PLATFORM — INTERVIEW PREPARATION

```
FILE INFORMATION
Project     : Nexus Platform
Purpose     : Consolidated interview preparation document
Base Package: com.raina.nexus
Stack       : Java 25, Spring Boot 4.1.0, Spring Cloud 2025.1.2, MySQL 8, PostgreSQL, Maven
Generated   : 2026-07-16
```

This document is written to be read out loud in an interview, not studied silently. Every section
answers "what did you build, why, and what would you have done differently in production."

---

## 1. RELEASE NOTES

### What this project is

A single Spring Boot service that owns two independent JPA domains — Employee (MySQL) and
Department (PostgreSQL) — deliberately kept on separate databases to force real dual-datasource
configuration, rather than a toy single-DB CRUD app. On top of that base, the project layers every
major Spring data-access and REST-client pattern an interviewer is likely to probe: three pagination
styles, two projection styles, three HTTP client implementations, and a centralized error/response
contract.

### Build order (why this sequence, not another)

**1. Core CRUD + entities.**
Employee and Department entities were modeled first, each with its own repository, service,
controller, and request/response DTOs. This established the package-by-feature structure
(`employee/`, `department/` each with `controller/dto/repository/service`) before anything else was
layered on.

**2. Dual datasource.**
As soon as a second entity (Department) needed a different database engine, the single
`spring.datasource.*` auto-configuration stopped being viable. `EmployeeDataSourceConfig` and
`DepartmentDataSourceConfig` were introduced together — each declaring its own `DataSource`,
`LocalContainerEntityManagerFactoryBean`, and `PlatformTransactionManager`, scoped to its own
repository package via `@EnableJpaRepositories(basePackages = ...)`. This had to happen early because
it changes how every repository and transaction in the app behaves.

**3. REST clients (RestTemplate → WebClient → Feign).**
Once both domains existed, cross-domain communication became a natural exercise: Department calls
Employee (and vice versa) over HTTP instead of a JPA join, because the two entities do not share a
persistence unit. All three Spring HTTP client styles were implemented against the same self-call
target to compare blocking vs. declarative vs. reactive approaches side by side.

**4. Java 25 + Spring Boot 4.1.0 migration.**
The project was upgraded onto the latest Boot line (`spring-boot-starter-parent 4.1.0`) and Spring
Cloud `2025.1.2` BOM, on Java 25. This surfaced real breaking changes worth remembering:
`DataSourceProperties` and `EntityManagerFactoryBuilder` moved packages
(`org.springframework.boot.jdbc.autoconfigure`, `org.springframework.boot.jpa`), and `@WebMvcTest`
support was split into its own `spring-boot-webmvc-test` dependency. Lombok's version was pinned
explicitly (`1.18.40`) because the Boot-managed default did not have confirmed JDK 25 annotation
processing support at the time.

**5. Pagination — offset, then cursor, then keyset.**
Offset pagination (`Pageable` / `Page<T>`) came first because it is what Spring Data gives you for
free. Cursor pagination was added next as a JPQL seek query (`WHERE e.id > :cursor ORDER BY e.id
ASC`) to demonstrate the seek method without leaving JPQL. Keyset pagination was added last as the
same seek method expressed in native SQL with `LIMIT`, to compare the two at the query level. All
three were exposed on `/api/employees` (`/page`, `/cursor`, `/keyset`).

**6. Projections — interface, then DTO — across all three pagination styles.**
`EmployeeProjection` (interface/closed projection) and `EmployeeSummaryResponse` (DTO projection via
JPQL constructor expression) were each wired through offset, cursor, and keyset variants
(`/api/employees/projection/interface[...]`, `/api/employees/projection/dto[...]`). This is the part
of the project that best demonstrates "I understand projections aren't just a syntax choice, they
change what SQL gets generated."

**7. Address sub-resource.**
`Address` was added as a child of `Employee` (`@OneToMany`, `cascade = CascadeType.ALL,
orphanRemoval = true`) to demonstrate parent-owned-child lifecycle management, exposed as a full
nested CRUD resource: `/api/employees/{employeeId}/addresses[/{addressId}]`.

**8. Testing pass.**
JUnit 5 + Mockito service-layer tests and MockMvc controller-layer tests were added for Employee,
Department, Address, the Weather clients, and `GlobalExceptionHandler`. Test object creation uses the
Lombok `@Builder` pattern rather than telescoping constructors. A dedicated `application-test.yml`
profile runs against H2 in dual compatibility mode (`MODE=MySQL` for the employee schema,
`MODE=PostgreSQL` for the department schema) so tests never touch real MySQL/PostgreSQL instances.

**9. Open source release pass.**
Credentials were extracted out of YAML into environment variables (`${DB_EMPLOYEE_PASSWORD}`, etc.),
per-environment profiles (`local`, `dev`, `test`) were finalized, and a Postman collection plus a
curl collection were added covering every endpoint in the project, kept in the repo root for anyone
cloning the project to exercise the API immediately.

### What changed along the way (worth mentioning if asked "what would you do differently")

- The duplicate `exception.ErrorResponse` class was removed once `common.response.ErrorResponse` was
  established as the canonical response-envelope location — a reminder that "canonical location"
  decisions need to be made *before* the second implementation gets written, not after.
- The shared `WebClient` bean originally had no `baseUrl` — every call had to hardcode the full URL.
  Adding a base URL (`http://localhost:9191`) to the bean was a small config change with an outsized
  readability win.

---

## 2. ARCHITECTURE NOTES

Each decision below is stated as **Decision → Problem → Solution → Why this approach → Tradeoffs**,
the way you'd walk an interviewer through a design doc.

### 2.1 Dual datasource (MySQL + PostgreSQL)

- **Decision:** Employee data lives in MySQL (`employeesdb`), Department data lives in PostgreSQL
  (`departmentsdb`), in the same Spring Boot application.
- **Problem:** Spring Boot's default auto-configuration wires exactly one `DataSource`, one
  `EntityManagerFactory`, one `TransactionManager`. A second, differently-engined database can't just
  be added to `application.yml` and expected to work.
- **Solution:** Two `@Configuration` classes — `EmployeeDataSourceConfig` (marked `@Primary` on every
  bean) and `DepartmentDataSourceConfig` — each defining its own `HikariDataSource`,
  `LocalContainerEntityManagerFactoryBean` (with its own `persistenceUnit` name and its own
  `packages(...)` scan target), and `JpaTransactionManager`. Each is paired with
  `@EnableJpaRepositories(basePackages = "...", entityManagerFactoryRef = "...",
  transactionManagerRef = "...")` so Spring Data knows which repositories belong to which persistence
  unit.
- **Why this approach:** It's the standard, supported way to run multiple JPA persistence units in one
  Spring context. Anything less explicit (e.g., relying on default bean names) breaks silently the
  moment a second `DataSource` bean exists, because Spring can no longer guess which one is primary.
- **Tradeoffs:** No cross-database transactions — a write to Employee and a write to Department in the
  "same" business operation are two separate local transactions, not one distributed transaction.
  There is no 2PC/XA coordinator here, so a partial failure (Employee write commits, Department write
  fails) is possible and is not compensated for automatically. In production this would need either an
  outbox pattern, a saga, or accepting eventual consistency with explicit compensation logic.

### 2.2 Cross-database JPA relationship removal (`departmentId Long`, not `@ManyToOne Department`)

- **Decision:** `Employee.departmentId` is a plain `Long` column, not a `@ManyToOne` object
  reference to `Department`.
- **Problem:** JPA entity associations (`@ManyToOne`, `@OneToMany`, lazy proxies, cascades) all assume
  both sides of the relationship are managed by the *same* `EntityManager` / persistence unit. Employee
  and Department are not — they're on physically different databases with different transaction
  managers.
- **Solution:** Store the foreign key as a plain scalar (`Long departmentId`) with no JPA-level
  navigability. Loading the associated Department, when needed, is an explicit service-layer call
  (`DepartmentWebClient.getDepartment(id)`), not a Hibernate-managed proxy fetch.
- **Why this approach:** It's honest about the constraint instead of fighting it. Trying to fake a
  cross-database `@ManyToOne` (e.g., with `@NotFound(action = IGNORE)` tricks or a custom
  `UserType`) produces fragile, surprising behavior — lazy-loading exceptions, N+1 queries that can
  never be joined away, and cache-coherency bugs. A soft reference makes the boundary explicit in the
  type system.
- **Tradeoffs:** No referential integrity at the database level — nothing stops `departmentId` from
  pointing at a Department that was deleted. No JPA join, so "give me every employee with their
  department name" is not one query; it's an employee query plus N HTTP calls (or a batched HTTP call)
  unless you build a read-side cache or a reporting DB that denormalizes both. This is the single
  biggest interview talking point in this project: **you cannot `@ManyToOne` across two different
  persistence units — full stop — and any "solution" that appears to do so is hiding a correctness
  bug.**

### 2.3 `ApiResponse<T>` generic wrapper

- **Decision:** Every successful controller response is wrapped in `ApiResponse<T>(boolean success,
  String message, T data)`.
- **Problem:** Without a consistent envelope, every endpoint returns a different raw shape, and
  clients can't write generic response-handling code (e.g., a shared Axios/RestTemplate interceptor
  that checks `success` before touching `data`).
- **Solution:** A single generic record, reused across every controller, constructed inline
  (`new ApiResponse<>(true, "Employee created successfully", response)`).
- **Why this approach:** It's the cheapest way to get a uniform contract without a framework-level
  response-body advice (`ResponseBodyAdvice`) rewriting every response, which would add indirection for
  a project this size.
- **Tradeoffs:** The `message` field is a free-text string set by hand at every call site — there's no
  compile-time guarantee it's meaningful or that two similar endpoints phrase it consistently. It also
  means HTTP status code and `success: true/false` are somewhat redundant (a 404 already tells the
  client the request failed) — some teams intentionally drop `success` for this reason and rely on
  status codes alone. Kept here for explicitness and because it makes body-only client checks trivial.

### 2.4 `CursorPageResponse<T>` for cursor/keyset pagination

- **Decision:** Cursor and keyset endpoints return `CursorPageResponse<T>(List<T> content, Long
  nextCursor, boolean hasNext)` instead of Spring Data's `Page<T>`.
- **Problem:** `Page<T>` carries `totalElements`/`totalPages`, both of which require a `COUNT(*)`
  query — expensive on large tables, and semantically meaningless for keyset pagination anyway (there
  is no stable "page 47 of N" once you're paging by seek key instead of offset).
  Correction: Spring Data's Page-based repository methods used here (`findEmployeeProjectionPage`) do
  still use `Page<T>` for the offset-pagination endpoints — `CursorPageResponse` is specifically for
  the two seek-based endpoints where a total count doesn't make sense.
- **Solution:** A minimal record carrying only what a seek-based client actually needs: the page of
  results, the cursor to pass on the next call, and whether a next page exists.
- **Why this approach:** `hasNext` is computed cheaply by over-fetching by one row
  (`PageRequest.of(0, size + 1)` / `LIMIT :size+1` in `findEmployeesKeyset`) and checking whether more
  than `size` rows came back — no second `COUNT` query, no window function.
- **Tradeoffs:** No `previousCursor` / backward paging support — this implementation is forward-only.
  No total count means UIs that want "showing 40 of 1,204" can't be built directly off this endpoint.

### 2.5 Three pagination forms — when to use which

- **Decision:** Offset (`/page`), cursor (`/cursor`), and keyset (`/keyset`) are all exposed side by
  side rather than picking one.
- **Problem:** Each pagination style has a real production failure mode the others don't, and
  "just use `Pageable`" is not always the right answer.
- **Solution / why this approach, per style:**
  - **Offset** (`Page<EmployeeResponse> findAll(Pageable)`): simple, supports jumping to an arbitrary
    page number, but degrades as `OFFSET` grows (the database still has to scan and discard the
    skipped rows) and is unstable under concurrent writes — a row inserted before the current offset
    shifts every subsequent page by one, causing skipped or duplicated rows.
  - **Cursor** (`WHERE e.id > :cursor ORDER BY e.id ASC`, JPQL): stable under concurrent writes because
    it seeks from a known key rather than counting rows to skip. Implemented here in JPQL so it's
    portable across the two datasources.
  - **Keyset** (`WHERE id > :afterId ORDER BY id ASC LIMIT :size`, native SQL): the same seek
    algorithm as "cursor" above, expressed as a native query instead of JPQL. **Interview nuance
    worth stating precisely:** in this codebase, "cursor" and "keyset" are the *same* pagination
    algorithm (seek-by-last-id) — the actual difference between the two endpoints is JPQL-with-
    `Pageable`-for-the-limit vs. hand-written native SQL with `LIMIT`, not a difference in pagination
    strategy. True keyset pagination and cursor pagination are often used as synonyms in industry;
    this project deliberately implements the same idea twice at two query layers so both are visible
    side by side.
- **Tradeoffs:** Offset is the only one of the three that supports random page access. Both seek-based
  approaches require an indexed, monotonically ordered column (here, the primary key) and lose the
  ability to jump to "page 12" without walking the sequence.

### 2.6 REST client selection (RestTemplate vs. WebClient vs. Feign)

- **Decision:** All three are implemented against the same target (Employee/Department self-calls)
  rather than choosing one and standardizing.
- **Problem:** Real production codebases have to choose one (usually), and interviewers routinely test
  whether you understand *why*, not just what the syntax looks like.
- **Solution:** `EmployeeClient` (RestTemplate, blocking), `DepartmentWebClient` (WebClient, reactive
  type but called with `.block()`), `EmployeeFeignClient` (Feign, declarative interface).
- **Why all three exist here:** deliberately educational — to compare imperative vs. declarative vs.
  reactive client ergonomics in one codebase.
- **Tradeoffs:** See section 4 for the full comparison. Short version for an interview: RestTemplate is
  in maintenance mode upstream — new code should default to `WebClient` (or `RestClient`, Spring's
  newer synchronous alternative) unless the whole call chain is genuinely reactive.

### 2.7 Error code strategy (specific vs. generic)

- **Decision:** error codes are specific (`EMPLOYEE_NOT_FOUND`, `ADDRESS_NOT_FOUND`,
  `DEPARTMENT_NOT_FOUND`, `DEPARTMENT_FETCH_FAILED`) rather than generic (`ERROR_001`), and every
  service that throws `ResourceNotFoundException` now applies one consistently.
- **Problem:** `ErrorResponse` (`timestamp, status, error, message, path`) has no dedicated
  `errorCode` field — there is nowhere structured to put a machine-readable code.
- **Solution as actually implemented:** every `ResourceNotFoundException` thrown anywhere in the
  codebase now carries a `CODE: human message` prefix baked into the exception message —
  `EmployeeService` (`EMPLOYEE_NOT_FOUND`), `DepartmentService`'s basic CRUD paths
  (`DEPARTMENT_NOT_FOUND`), `AddressService` (`EMPLOYEE_NOT_FOUND` for the parent lookup,
  `ADDRESS_NOT_FOUND` for the child lookup), and `DepartmentService.getDepartmentViaWebClient`
  (`DEPARTMENT_FETCH_FAILED` on transport failure, `DEPARTMENT_NOT_FOUND` on an empty body). This
  makes the convention uniform, but it's still a *string* convention, not a structured one — see below.
- **Why this matters / what production would still need:** a client still can't reliably branch on a
  code that's concatenated into a free-text message without parsing the string. The more correct fix
  is a dedicated `errorCode` field on `ErrorResponse`, populated from an enum, with
  `ResourceNotFoundException` carrying the code as a constructor argument instead of baking it into the
  message — worth naming as the next step if asked "how would you productionize this further," even
  though the prefix convention is now applied consistently everywhere it's used.

### 2.8 Per-environment YAML profiles

- **Decision:** `application.yaml` (base) + `application-local.yml` + `application-dev.yml` +
  `application-test.yml`, YAML only, no `.properties` files, no hardcoded credentials.
- **Problem:** Local development needs a live MySQL/PostgreSQL with schema auto-sync; CI/tests need a
  disposable, fast, isolated database; a shared dev environment needs schema drift *prevented*, not
  silently patched.
- **Solution:** `local` runs `ddl-auto: update` against real MySQL/PostgreSQL with `show-sql: true`;
  `test` runs `ddl-auto: create-drop` against H2 in dual compatibility mode (`MODE=MySQL` /
  `MODE=PostgreSQL`) so schema-specific SQL dialect differences are caught even on an in-memory DB;
  `dev` runs `ddl-auto: validate` (schema must already match the entities — Hibernate refuses to start
  otherwise) with `show-sql: false`. Credentials are always `${ENV_VAR}` placeholders, never literals.
- **Why this approach:** `validate` in shared environments is the guardrail that stops an entity change
  from silently altering a shared schema — it forces a real migration step (Flyway/Liquibase in a
  production version of this) instead of letting Hibernate improvise DDL.
- **Tradeoffs:** No profile here uses `ddl-auto: none` + a migration tool, which is what an actual
  production profile should do — `validate` still means Hibernate needs the exact schema shape at
  startup, but there's no versioned migration history in this project yet.

---

## 3. JPA NOTES

Each topic: **What it is → How Spring executes it internally → Code pattern used in this project →
Interview questions.**

### 3.1 Spring Data JPA vs. Hibernate vs. EntityManager

- **What it is:** Three layers, often confused as one thing. JPA is the *specification*
  (`jakarta.persistence.*` — `EntityManager`, `@Entity`, `@Id`). Hibernate is the *implementation* of
  that spec that Spring Boot pulls in by default. Spring Data JPA is a layer *on top of* JPA that
  generates repository implementations from interfaces, so you never write `EntityManager` calls
  directly for basic CRUD.
- **How Spring executes it internally:** At startup, Spring Data JPA scans for interfaces extending
  `JpaRepository`/`JpaSpecificationExecutor` inside the packages named in `@EnableJpaRepositories`, and
  generates a dynamic proxy implementation for each one. That proxy delegates to a Hibernate
  `EntityManager` obtained from the `EntityManagerFactory` bound to that repository's transaction
  manager.
- **Code pattern used here:** `EmployeeRepository extends JpaRepository<Employee, Long>,
  JpaSpecificationExecutor<Employee>` — no manual `EntityManager` usage anywhere in the codebase; every
  query is either a derived query method, `@Query` (JPQL or native), or the Specification API.
- **Interview questions:**
  - *"What's the actual difference between JPA and Hibernate?"* — spec vs. implementation; you could
    swap Hibernate for EclipseLink without changing `@Entity`/`@Id` annotations, in theory.
  - *"Does Spring Data JPA replace Hibernate?"* — no, it sits on top of it; Hibernate still does the
    SQL generation and entity state management underneath.
  - *"Where does the `EntityManager` come from for a `@Repository` you never explicitly wired?"* — it's
    injected by the Spring Data proxy from the `EntityManagerFactory` bean matching that repository's
    configured `entityManagerFactoryRef`.

### 3.2 Dual datasource — separate `EntityManagerFactory`, `TransactionManager`, persistence unit

- **What it is:** Two fully independent JPA runtimes in one Spring context, each with its own
  connection pool, entity scan path, and transaction boundary.
- **How Spring executes it internally:** `LocalContainerEntityManagerFactoryBean.packages(...)` tells
  Hibernate which entity classes belong to which persistence unit at startup (`packages(
  "com.raina.nexus.entity.employee")` vs. `packages("com.raina.nexus.entity.department")`); mixing an
  entity from the wrong package into the wrong `EntityManagerFactory` produces a runtime
  `IllegalArgumentException: Not an entity` the first time it's used.
- **Code pattern used here:** `EmployeeDataSourceConfig` / `DepartmentDataSourceConfig`, each with
  `@Primary` only on the Employee side (Spring needs exactly one unambiguous default when a
  non-qualified `DataSource`/`EntityManagerFactory` is requested elsewhere in the context).
- **Interview questions:**
  - *"Can `@Transactional` span both datasources atomically?"* — no, not without XA/JTA; here, each
    `PlatformTransactionManager` only knows about its own `EntityManagerFactory`, so a single
    `@Transactional` method touching both repositories would actually be running two separate local
    transactions under the hood — not atomic together.
  - *"Why does one config need `@Primary` and the other doesn't?"* — because some other bean in the
    context requests a `DataSource`/`EntityManagerFactory` without a `@Qualifier` (e.g., Boot's own
    health/metrics auto-configuration); without a primary, that lookup is ambiguous and startup fails.
  - *"What happens if you scan the wrong `packages(...)` for a persistence unit?"* — Hibernate either
    silently ignores entities outside the configured package, or throws when a repository tries to use
    an entity type its `EntityManagerFactory` was never told about.

### 3.3 `@OneToMany` with cascade and `orphanRemoval` (Employee → Address)

- **What it is:** `Employee.addresses` is `@OneToMany(mappedBy = "employee", cascade =
  CascadeType.ALL, orphanRemoval = true)`; `Address.employee` is the owning `@ManyToOne(fetch =
  FetchType.LAZY)` side with the actual `employee_id` foreign key column.
- **How Spring executes it internally:** `cascade = ALL` means persisting/removing an `Employee`
  cascades to every `Address` in its collection automatically — you never call
  `addressRepository.save()` directly from a cascade-driven flow. `orphanRemoval = true` goes further:
  if an `Address` is simply *removed from the collection* (not explicitly deleted), Hibernate issues a
  `DELETE` for it on flush, because a child with `nullable = false` on its FK can't legally exist
  without a parent. `@JsonManagedReference` / `@JsonBackReference` exist purely to stop Jackson from
  infinitely recursing when serializing `Employee → addresses → employee → addresses → ...`.
- **Code pattern used here:** `AddressService` still manages addresses through its own
  `AddressRepository` directly (`addressRepository.save(address)`, `addressRepository.delete(address)`)
  for the sub-resource CRUD endpoints — cascade here is what makes deleting an `Employee` also clean up
  its `Address` rows, not what drives the day-to-day Address CRUD API.
- **Interview questions:**
  - *"What's the difference between `cascade = REMOVE` and `orphanRemoval = true`?"* — `REMOVE`
    deletes children when the *parent* is deleted; `orphanRemoval` deletes a child the moment it's
    disconnected from the parent's collection, even if the parent itself is untouched.
  - *"Why is `Address.employee` `LAZY` but `Employee.addresses` has no explicit fetch type?"* —
    `@ManyToOne` defaults to `EAGER` in the JPA spec, so it's set to `LAZY` explicitly to avoid loading
    the parent every time an address is touched; `@OneToMany` already defaults to `LAZY`, so no override
    is needed there.
  - *"Why can `Address.employee` not be `nullable = true`?"* — because `orphanRemoval` and a nullable
    owning FK don't compose logically — if the FK could be null, "removed from the collection" and
    "still exists with no parent" become ambiguous states.

### 3.4 Soft reference across databases (`departmentId Long`)

Covered in depth in section 2.2 — the JPA-specific angle: this field is a completely ordinary mapped
column (`@Column(name = "department_id")`), with zero JPA relationship annotations. Hibernate treats
it exactly like `salary` or `email` — no fetch strategy, no cascade, no proxy. That absence is the
entire design.

- **Interview questions:**
  - *"Would `@ManyToOne` even compile here?"* — it would compile, but Hibernate would fail at runtime
    trying to resolve `Department` through an `EntityManagerFactory` that was never told about the
    `com.raina.nexus.entity.department` package (the Employee persistence unit only scans
    `com.raina.nexus.entity.employee`).
  - *"How would you fetch a Department for a list of 50 Employees without N+1 HTTP calls?"* — batch the
    distinct `departmentId`s from the 50 employees into one call (a `/api/departments?ids=1,2,3`-style
    batch endpoint, not built here) rather than calling `DepartmentWebClient.getDepartment(id)` per row.

### 3.5 Pagination with `Pageable`

- **What it is:** Spring Data's abstraction for offset-based paging and sorting, bound automatically
  from request parameters (`page`, `size`, `sort`) when a controller method takes a `Pageable`
  parameter with no extra configuration.
- **How Spring executes it internally:** `PageableHandlerMethodArgumentResolver` parses the query
  string into a `PageRequest`; `JpaRepository.findAll(Pageable)` translates that into `LIMIT`/`OFFSET`
  SQL plus a separate `COUNT(*)` query to populate `Page.getTotalElements()`.
- **Code pattern used here:** `EmployeeController.getEmployees(Pageable pageable)` →
  `employeeRepository.findAll(pageable).map(this::mapToResponse)` — `/api/employees/page`.
- **Interview questions:**
  - *"Why does `Page<T>` always run two queries?"* — one for the actual rows, one `COUNT(*)` for the
    total — unavoidable if you want `totalPages`/`totalElements`, which is exactly why the seek-based
    endpoints in this project use `CursorPageResponse` instead.

### 3.6 Specification API (dynamic search with Criteria API)

- **What it is:** `EmployeeSpecification.hasFirstName(String)` returns a `Specification<Employee>` —
  a functional interface wrapping `(Root<Employee> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
  Predicate`, used through `JpaSpecificationExecutor<Employee>.findAll(Specification)`.
- **How Spring executes it internally:** the `Specification` is invoked at query-build time with a
  live `CriteriaBuilder`, producing a `Predicate` that Spring Data JPA composes into the final Criteria
  API query — this is a thin, type-safe wrapper directly over JPA's `CriteriaBuilder`/`CriteriaQuery`,
  not a separate query language.
- **Code pattern used here:**
  ```java
  cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%")
  ```
  called via `employeeRepository.findAll(hasFirstName(firstName))` in
  `EmployeeService.searchWithSpecification`, exposed at `/api/employees/specification`.
- **Interview questions:**
  - *"Why use Specification instead of a derived query method here?"* — this particular predicate
    (`findByFirstNameContainingIgnoreCase`) genuinely could be a derived method (and is, at
    `/api/employees/search`) — Specification's real value shows up when predicates need to be combined
    conditionally at runtime (e.g., "filter by name AND optionally by salary range AND optionally by
    department"), which a fixed derived-method signature can't express.
  - *"What's the downside of Specification vs. a derived query method?"* — less readable/discoverable
    than a named method, and it's easy to accidentally build an unindexed, unbounded query when
    predicates are composed dynamically without care.

### 3.7 JPQL query (`@Query`)

- **What it is:** Hibernate's object-oriented query language — queries entities and their fields
  (`Employee e`, `e.salary`), not raw table/column names, so results are portable across the two
  database engines used in this project.
- **How Spring executes it internally:** JPQL is parsed and translated into dialect-specific SQL at
  query time, based on which `Dialect` the target `EntityManagerFactory` was configured with (MySQL
  dialect for Employee, PostgreSQL dialect for Department) — the same JPQL string produces different
  SQL depending on which persistence unit runs it.
- **Code pattern used here:** `findEmployeesWithSalaryGreaterThan` —
  `SELECT e FROM Employee e WHERE e.salary > :salary`, exposed at `/api/employees/salary`.
- **Interview questions:**
  - *"Why JPQL instead of a derived method name for this one?"* — readability past a certain
    complexity; `findBySalaryGreaterThan` would have worked here too — JPQL is chosen when the
    equivalent method name would get unwieldy or when the query needs constructs derived-method naming
    can't express (joins, subqueries, constructor expressions).

### 3.8 Native Query (`@Query(nativeQuery = true)`)

- **What it is:** Raw SQL, bypassing JPQL translation entirely — runs exactly the SQL string written,
  against whichever physical database engine that repository's `DataSource` points at.
- **How Spring executes it internally:** passed straight to the JDBC driver via the `EntityManager`'s
  native query API; results are still mapped back into entities/projections by Hibernate afterward, but
  there's no dialect translation step — the SQL has to already be valid for that specific engine.
- **Code pattern used here:** `findEmployeesNative` (`SELECT * FROM employees WHERE salary > :salary`,
  exposed at `/api/employees/native`) and `findEmployeesKeyset`
  (`SELECT * FROM employees WHERE id > :afterId ORDER BY id ASC LIMIT :size`, exposed at
  `/api/employees/keyset`) — both written against MySQL syntax since they only ever run on the Employee
  persistence unit.
- **Interview questions:**
  - *"Why does the keyset endpoint use native SQL instead of JPQL?"* — `LIMIT` inside a JPQL string
    isn't portable JPQL syntax the same way it's plain SQL — using `Pageable` to express the limit in
    JPQL (as the cursor endpoint does) versus writing `LIMIT` directly in native SQL are just two
    different ways to express the same seek query; native SQL was chosen for the keyset variant
    specifically to show the raw-SQL form.
  - *"What breaks if this project added a native query against `employees` that also needs
    department data?"* — nothing joins across the two databases at the SQL level, ever — a native query
    is still scoped to one `DataSource`.

### 3.9 Interface Projection

- **What it is:** `EmployeeProjection` — an interface with only getter methods (`getId()`,
  `getFirstName()`, `getSalary()`), no implementation. Spring Data generates a runtime proxy for it.
- **How Spring executes it internally:** the backing `@Query` methods select individual aliased
  columns (`SELECT e.id AS id, e.firstName AS firstName, e.salary AS salary FROM Employee e`) rather
  than the whole entity (`SELECT e`). Hibernate translates that into a SQL `SELECT` naming only those
  three columns, and Spring Data maps the resulting tuple onto the `EmployeeProjection` proxy by
  matching each alias to the corresponding getter name (`id → getId()`, `firstName → getFirstName()`,
  `salary → getSalary()`) — a genuine column-pruning projection, not a full-entity load exposed through
  a narrower interface. (Without explicit column aliases matching the getters — e.g. a bare
  `SELECT e FROM Employee e` — Spring Data instead loads the *full* entity and only narrows what's
  exposed through the interface at the Java level, which is a real trap worth naming: the interface
  alone guarantees nothing about the generated SQL, the query shape does.)
- **Code pattern used here:** `findEmployeeProjectionPage`, `findEmployeeProjectionCursor` (JPQL with
  aliased column selection) and `findEmployeeProjectionKeyset` (native SQL with aliased columns,
  `SELECT id, first_name AS firstName, salary FROM employees ...`) — the offset/cursor/keyset trio at
  `/api/employees/projection/interface[...]`, all three now selecting only the three required columns
  at the SQL level.
- **Interview questions:**
  - *"Does an interface projection reduce the columns fetched from the database?"* — only when the
    query itself selects individual aliased fields (either Spring Data deriving the query from a method
    name, or a hand-written `@Query` with `SELECT e.id AS id, ...` aliases matching the getters) — a
    hand-written `@Query` that selects the whole entity (`SELECT e FROM Employee e`) loads every column
    regardless of the projection interface's shape, because the interface only controls what's exposed
    in Java, not what SQL gets generated.

### 3.10 DTO Projection

- **What it is:** `EmployeeSummaryResponse` — a Java `record`, populated via a JPQL **constructor
  expression**: `SELECT new com.raina.nexus.employee.dto.EmployeeSummaryResponse(e.id, e.firstName,
  e.salary) FROM Employee e`.
- **How Spring executes it internally:** unlike the interface projection above, this genuinely changes
  the generated SQL — Hibernate selects only `id, first_name, salary` columns, then constructs the DTO
  directly via reflection, never materializing a full `Employee` entity or attaching it to the
  persistence context (so no dirty-checking overhead either).
- **Code pattern used here:** `findEmployeeSummaryPage`, `findEmployeeSummaryCursor` (JPQL constructor
  expression), and `findEmployeeSummaryKeyset`-equivalent — except the keyset variant can't use a JPQL
  constructor expression in *native* SQL (that's a JPQL-only construct), so
  `getEmployeeSummariesKeyset` in `EmployeeService` calls the native `findEmployeesKeyset` (full
  entity) and maps to `EmployeeSummaryResponse` manually in the service layer afterward — documented
  inline in the code as a deliberate, known limitation of mixing constructor expressions with native
  SQL.
- **Interview questions:**
  - *"DTO projection vs. interface projection — which one actually reduces the SQL columns
    selected?"* — DTO projection via constructor expression, reliably, because the `SELECT new ...`
    clause tells Hibernate exactly which columns to fetch; interface projection only does this when
    Spring Data authors the query itself from a method name.
  - *"Why can't the keyset endpoint use a constructor expression?"* — `SELECT new package.Type(...)` is
    JPQL syntax, translated by Hibernate; native SQL is passed straight to the JDBC driver with no JPQL
    translation step, so there's no constructor-expression concept available there at all.

### 3.11 Cursor pagination (id as cursor)

Covered fully in 2.5 / 3.8. JPQL form: `WHERE e.id > :cursor ORDER BY e.id ASC` combined with
`PageRequest.of(0, size + 1)` purely to express the row limit — `pageNumber` is always `0` here
because there's no "offset," only a seek predicate.

### 3.12 Keyset pagination (`WHERE id > :afterId` native SQL)

Covered fully in 2.5 / 3.8. Native form: `WHERE id > :afterId ORDER BY id ASC LIMIT :size`. Both 3.11
and 3.12 share the same `buildCursorPage(rows, size, idExtractor)` helper in `EmployeeService`, which
over-fetches by one row to compute `hasNext` without a second query, then trims back to `size`.

---

## 4. REST CLIENT NOTES

### 4.1 RestTemplate

- **What it is:** Spring's original synchronous HTTP client, present since Spring 3, now in
  maintenance mode (Spring's own docs recommend `WebClient` or the newer `RestClient` for new code).
- **Blocking/Non-blocking:** Blocking — the calling thread is parked until the HTTP response completes.
- **Internal working:** Delegates to a pluggable `ClientHttpRequestFactory` (default: the JDK's
  `HttpURLConnection`-based factory unless something like Apache HttpClient is on the classpath),
  synchronously executes the request, and converts the response body via `HttpMessageConverter`s
  (Jackson for JSON here).
- **Configuration in this project:** `RestTemplateConfig` registers a single bare `RestTemplate` bean
  — `new RestTemplate()`, no timeout configuration, no connection pool tuning, no interceptors.
- **Code pattern:** `EmployeeClient.getEmployee(id)` — `restTemplate.getForObject(url,
  EmployeeResponse.class)`, URL built by string concatenation (`"http://localhost:9191/api/employees/"
  + id`).
- **Error handling:** None at this call site — a non-2xx response throws `RestClientException`
  (specifically `HttpClientErrorException`/`HttpServerErrorException`), which is only ever caught by
  `GlobalExceptionHandler`'s generic `Exception` fallback (500), not translated into a domain-specific
  error the way `DepartmentService.getDepartmentViaWebClient` explicitly does for the WebClient path.
- **When to use:** Legacy codebases already standardized on it, or simple synchronous internal calls
  where pulling in WebFlux is not justified.
- **When NOT to use:** New code in a project that already has `spring-boot-starter-webflux` on the
  classpath (as this one does) — there's no reason to maintain two HTTP client stacks.
- **Tradeoffs:** Simple, synchronous, easy to reason about and debug — but no built-in timeout defaults
  (a hung downstream call can hang the calling thread indefinitely unless a request factory with
  timeouts is explicitly configured), no reactive backpressure, and it's a client Spring itself is
  steering people away from.

### 4.2 WebClient

- **What it is:** Spring WebFlux's reactive, non-blocking HTTP client, built on `Mono`/`Flux`.
- **Blocking/Non-blocking:** Non-blocking by design — but this project calls `.block()` at the end of
  every chain (`DepartmentWebClient.getDepartment`, `WeatherWebClientService.getWeather`), which
  converts it back into a synchronous call. This is a deliberate simplification for a servlet-stack
  (Spring MVC) application that isn't reactive end-to-end — worth naming as a known tradeoff rather
  than presenting it as "using WebClient reactively," because it isn't, here.
- **Internal working:** Builds a request via a fluent DSL (`.get().uri(...).retrieve()`), executes it
  on a Reactor Netty (or configured) HTTP connector, and returns a `Mono<T>`/`Flux<T>` that only
  executes the actual network call when subscribed to — `.block()` is what triggers the subscription
  and waits synchronously for the result.
- **Configuration in this project:** `WebClientConfig` registers one shared `WebClient` bean with
  `baseUrl("http://localhost:9191")`. Note: `WeatherWebClientService` passes an *absolute* URL into
  `.uri(...)` for the weather API — passing an absolute URI overrides a configured `baseUrl` entirely
  for that call, which is exactly why it works despite the bean's base URL pointing somewhere else.
- **Code pattern:** `DepartmentWebClient.getDepartment(id)` —
  `webClient.get().uri("/api/departments/{id}", id).retrieve()
  .bodyToMono(new ParameterizedTypeReference<ApiResponse<DepartmentResponse>>() {}).block()`.
- **Error handling:** `DepartmentService.getDepartmentViaWebClient` wraps the call in a try/catch,
  translating any exception into a domain `ResourceNotFoundException` with a `DEPARTMENT_FETCH_FAILED`
  prefix, and separately checks for a null body/data to raise `DEPARTMENT_NOT_FOUND` — this is the one
  client in the project with an explicit error-translation boundary at the service layer.
- **When to use:** Any new HTTP client code in this project, and especially anything that could
  eventually be reactive end-to-end (a WebFlux controller calling a WebFlux-based downstream service).
- **When NOT to use:** Don't call `.block()` inside a reactive (WebFlux) request-handling thread — that
  reintroduces blocking on a thread pool sized for non-blocking work and defeats the entire point.
  Calling `.block()` from a Spring MVC controller thread (as done here) is comparatively safe because
  MVC threads are already dedicated per-request and blocking is the model MVC assumes anyway.
- **Tradeoffs:** More setup/learning curve than `RestTemplate` for the synchronous use case shown here,
  with none of the reactive payoff since results are blocked on immediately — the honest reason to use
  it in an MVC app today is that it's still actively maintained and has a nicer, timeout-friendly
  builder API, not that it's "faster."
- **Known limitation:** WebClient used synchronously in MVC stack — reactive benefit not realized.
  Production fix: migrate to Spring WebFlux or use RestClient. Calling `.block()` at the end of every
  chain (`DepartmentWebClient.getDepartment`, `WeatherWebClientService.getWeather`) means the calling
  MVC thread is parked waiting on the network call exactly as it would be with `RestTemplate` — the
  non-blocking, backpressure-aware behavior WebClient is built for never actually engages, because
  nothing downstream of the `.block()` call is reactive. Two real fixes exist: (1) migrate the
  controller chain to Spring WebFlux end-to-end so the `Mono`/`Flux` is returned and subscribed to by
  the framework instead of blocked on manually, or (2) if the application is staying on the Spring MVC
  (servlet) stack, replace this synchronous WebClient usage with `RestClient` — Spring's newer
  synchronous HTTP client, purpose-built for exactly this call pattern, with the same modern
  timeout/builder ergonomics as WebClient but without the misleading reactive type signature.

### 4.3 OpenFeign

- **What it is:** A declarative HTTP client — you declare an interface with Spring MVC-style mapping
  annotations, and Spring Cloud OpenFeign generates the implementation at startup.
- **Blocking/Non-blocking:** Blocking by default (Feign's default client is synchronous; it can be
  backed by different underlying HTTP clients, but the calling contract here is synchronous).
- **Internal working:** `@EnableFeignClients` (declared once on `NexusPlatformApplication`) triggers a
  classpath scan for `@FeignClient`-annotated interfaces; Spring generates a dynamic proxy per
  interface that translates each method call into an HTTP request based on the method's annotations,
  using the `url` (or service-discovery `name`) configured on `@FeignClient`.
- **Configuration in this project:** `EmployeeFeignClient` — `@FeignClient(name = "employee-client",
  url = "http://localhost:9191")`, a single `@GetMapping("/api/employees/{id}")` method returning
  `ApiResponse<EmployeeResponse>` — matching the actual `{success, message, data}` envelope
  `/api/employees/{id}` returns. `FeignTestController` calls
  `employeeFeignClient.getEmployee(id).data()` to unwrap the payload before returning it. (This return
  type previously declared `EmployeeResponse` directly, which would have mismatched the real response
  envelope and either failed deserialization or produced a mostly-null object — a good example of why a
  declarative client's return type has to be checked against the real endpoint response shape, not just
  assumed from the method name.)
- **Error handling:** None implemented — a non-2xx response throws `FeignException`, uncaught at any
  call site currently using this client, falling to the generic 500 handler.
- **When to use:** Calling a known set of downstream REST endpoints where you'd rather declare a
  typed interface than hand-write request-building boilerplate — especially strong when combined with
  Spring Cloud service discovery (`name` resolved via a registry instead of a hardcoded `url`).
- **When NOT to use:** One-off calls, or calls needing fine-grained control over request building,
  retries, or streaming — the declarative model hides those details, which is the tradeoff.
- **Tradeoffs:** Least code per call site of the three, best fit for microservice-to-microservice
  calls, but the least visible about what's actually happening on the wire, and (as seen in this
  project) it's easy to let response typing drift from the response envelope actually returned by the
  server, since there's no runtime enforcement tying the Feign method's return type to reality.

### 4.4 Comparison table

| | RestTemplate | WebClient | Feign |
|---|---|---|---|
| Blocking | Yes | No (but called with `.block()` here) | Yes (default client) |
| Reactive | No | Yes (unused reactively in this project) | No |
| Declarative | No — imperative calls | No — fluent builder | Yes — interface + annotations |
| Error handling | Uncaught here → generic 500 | Explicit try/catch + domain exception (Department path only) | Uncaught here → generic 500 |
| Timeout config | None set (defaults from `HttpURLConnection`) | Configurable on the underlying `HttpClient`/connector (not set here) | Configurable via `feign.client.config.*` (not set here) |
| Production recommendation | Avoid for new code — maintenance mode | Preferred default going forward | Preferred for declarative service-to-service calls, ideally with service discovery |

---

## 5. INTERVIEW Q&A BANK

### 5.1 Spring Boot internals (auto-configuration, bean lifecycle)

**Basic**
- *Q: What triggers Spring Boot auto-configuration?* A: `@SpringBootApplication` bundles
  `@EnableAutoConfiguration`, which loads configuration classes listed in
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` on the classpath,
  each guarded by `@ConditionalOnClass`/`@ConditionalOnMissingBean` so they only activate when relevant
  dependencies are present and the developer hasn't already defined an equivalent bean.
- *Q: What's the Spring bean lifecycle in one sentence?* A: Instantiate → populate properties →
  `Aware` callbacks → `BeanPostProcessor` pre-init → `@PostConstruct`/`InitializingBean` → ready for
  use → `@PreDestroy`/`DisposableBean` on shutdown.

**Intermediate**
- *Q: Why does `EmployeeDataSourceConfig` need `@Primary` on every bean but
  `DepartmentDataSourceConfig` doesn't?* A: Any other bean in the context that requests a
  `DataSource`/`EntityManagerFactory` without a `@Qualifier` needs exactly one unambiguous candidate;
  marking the Employee side `@Primary` resolves that ambiguity in its favor.
- *Q: `@Configuration` vs. `@Component` for a class defining `@Bean` methods — does it matter?* A: Yes
  — `@Configuration` (unless `proxyBeanMethods = false`) is CGLIB-proxied so that calling one `@Bean`
  method from another within the same class returns the *same* singleton instance rather than a new
  object; `@Component` classes with `@Bean` methods don't get that guarantee.
- *Q: What does `@PostConstruct` on `NexusPlatformApplication.checkEnv()` actually run, and when?* A:
  It runs once, after the application context has fully instantiated and injected that bean's
  dependencies but before the application is considered "ready" — used here to log whether
  `WEATHER_API_KEY` resolved from the environment at all.

**Advanced**
- *Q: If two `@Configuration` classes both define a bean of the same type with no qualifiers and no
  `@Primary`, what happens?* A: Startup fails with a `NoUniqueBeanDefinitionException` at the first
  point something tries to autowire that type unqualified — which is exactly the failure this project's
  `@Primary` placement is designed to avoid.
- *Q: How does `@EnableJpaRepositories(basePackages = ...)` avoid picking up the *other* datasource's
  repositories?* A: It restricts Spring Data's classpath scan to the given package, so
  `EmployeeDataSourceConfig`'s `@EnableJpaRepositories("com.raina.nexus.employee.repository")` never
  even considers `com.raina.nexus.department.repository` — this is what makes two separate
  `@EnableJpaRepositories` declarations coexist safely in one context.

### 5.2 Dual datasource configuration

**Basic**
- *Q: Can one Spring Boot app talk to two different database engines at once?* A: Yes — by defining
  separate `DataSource`, `EntityManagerFactory`, and `TransactionManager` beans per database, as this
  project does for MySQL (Employee) and PostgreSQL (Department).

**Intermediate**
- *Q: Why does each datasource config define its own `TransactionManager` instead of sharing one?* A:
  A `PlatformTransactionManager` is bound to exactly one `EntityManagerFactory`/`DataSource` — a shared
  transaction manager couldn't coordinate commits/rollbacks across two unrelated connection pools
  correctly.
- *Q: What does `.packages("com.raina.nexus.entity.employee")` actually control?* A: Which entity
  classes Hibernate scans and registers against that specific persistence unit — an entity outside that
  package is invisible to that `EntityManagerFactory`.

**Advanced**
- *Q: Is a `@Transactional` method that calls both an Employee repository and a Department repository
  atomic?* A: No — without JTA/XA, Spring picks whichever `PlatformTransactionManager` is default (or
  the one implied by `@Transactional(transactionManager = "...")`) for that method; the *other*
  repository's writes happen outside that transaction's boundary entirely, so a rollback on one side
  does not undo a commit already made on the other.
- *Q: How would you make a write to both databases atomic in production?* A: Either accept eventual
  consistency with a compensating-action/saga pattern, use a transactional outbox for the second write,
  or introduce a JTA transaction manager (e.g., Atomikos/Narayana) coordinating both resources via 2PC
  — the last option adds real operational complexity and latency, so most teams choose the saga/outbox
  route instead.

### 5.3 JPA EntityManager and PersistenceContext

**Basic**
- *Q: What is a `PersistenceContext`?* A: The first-level cache / set of managed entities an
  `EntityManager` is currently tracking within a transaction — changes to a managed entity are detected
  automatically (dirty checking) and flushed to the database without an explicit `save()` call.

**Intermediate**
- *Q: Does calling `employeeRepository.findById(id)` inside a `@Transactional` method return a managed
  entity?* A: Yes, and mutating it (e.g., `employee.setSalary(x)`) without calling `save()` still
  persists the change on transaction commit, because Hibernate's dirty-checking flushes pending changes
  on managed entities automatically.
- *Q: Why does `EmployeeService` call `employeeRepository.save(employee)` explicitly even though it's a
  managed entity?* A: For a *new* entity (no `@GeneratedValue` id assigned yet), `save()` is what
  actually issues the `INSERT` and puts the entity into the persistence context in the first place —
  dirty checking only applies to entities already managed.

**Advanced**
- *Q: Why is `Address.employee` `FetchType.LAZY`, and what would break if it weren't?* A: `LAZY` means
  Hibernate returns a proxy for `employee` instead of loading it immediately, avoiding an unnecessary
  join every time an `Address` is fetched on its own; if it were `EAGER` (the JPA default for
  `@ManyToOne`), every `Address` query would silently join back to `Employee`, and accessing a lazy
  proxy's fields outside an active `PersistenceContext` (e.g., after the transaction/session closed) is
  exactly what causes `LazyInitializationException` if the fetch were needed later without the eager
  join.

### 5.4 Transaction management (`@Transactional` internals)

**Basic**
- *Q: How does `@Transactional` actually work?* A: It's AOP — Spring wraps the annotated method (or
  class) in a proxy that begins a transaction before the method runs and commits/rolls back after,
  based on whether the method threw an exception.

**Intermediate**
- *Q: This project's services don't have explicit `@Transactional` annotations on most write methods —
  why doesn't `employeeRepository.save()` blow up outside a transaction?* A: Spring Data JPA repository
  methods are transactional by default (Spring Data wraps them internally), so a bare `.save()` call
  still runs inside a short-lived transaction even without an explicit annotation on the calling
  service method — but multi-step operations spanning several repository calls in one service method
  would *not* be atomic together without an explicit `@Transactional` on that service method.
- *Q: Why would `AddressService.createAddress` (which does a `findById` then a `save`) benefit from
  `@Transactional` even though each individual call is already transactional on its own?* A: Without
  it, the "employee exists" check and the address insert are two separate transactions — a delete of
  the employee racing between those two calls (unlikely but possible) could leave an orphaned address
  attempt; wrapping both in one `@Transactional` method makes the whole operation atomic.

**Advanced**
- *Q: What's the default rollback behavior of `@Transactional`, and where would it bite in this
  project?* A: By default, Spring only rolls back on unchecked exceptions (`RuntimeException` and
  `Error`), not checked exceptions — `ResourceNotFoundException` here extends `RuntimeException`, so
  it *does* trigger rollback correctly, but this is a common gotcha if a checked exception were ever
  introduced into a transactional method's throws clause.
  - *Q: Does calling one `@Transactional` public method from another `@Transactional` method within the
  same class actually start a new transaction?* A: No — Spring's proxy-based AOP only intercepts calls
  that go through the proxy (i.e., external calls); a self-invocation bypasses the proxy entirely, so
  the "inner" `@Transactional` is silently ignored, and everything runs in whatever transaction (if
  any) the outer call started.

### 5.5 Pagination — all 3 forms

**Basic**
- *Q: What's the simplest way to paginate a Spring Data JPA query?* A: Accept a `Pageable` parameter
  and return `Page<T>` — Spring Data handles the `LIMIT`/`OFFSET` and total count automatically.

**Intermediate**
- *Q: Why would you ever avoid `Page<T>`/offset pagination?* A: Two reasons — performance (`OFFSET`
  gets more expensive as it grows, since the DB still scans past the skipped rows) and correctness
  under concurrent writes (rows inserted/deleted while paging shift the offset, causing skipped or
  duplicated results on later pages).
- *Q: How does this project compute `hasNext` for cursor/keyset pages without a `COUNT(*)` query?* A:
  It over-fetches by one row (`size + 1`) and checks whether more than `size` rows came back —
  `buildCursorPage` in `EmployeeService` trims the extra row before returning.

**Advanced**
- *Q: Cursor and keyset pagination in this project both seek by `id` — what happens if the sort order
  needs to be something other than the primary key, e.g., by `salary`?* A: The seek predicate has to
  become a compound comparison on the sort column *plus* the id as a tiebreaker
  (`WHERE (salary, id) > (:lastSalary, :lastId)`), because a single non-unique column alone can't
  guarantee a stable, gap-free seek position when duplicate values exist.
- *Q: Why is cursor/keyset pagination not vulnerable to the same "shifted results" problem as offset
  pagination under concurrent inserts?* A: Because it seeks from a concrete, already-seen key value
  rather than a row count — a new row inserted anywhere in the table doesn't change what "greater than
  the last id I saw" means for the next page.

### 5.6 Specification API

**Basic**
- *Q: What does `JpaSpecificationExecutor<T>` add to a repository?* A: `findAll(Specification<T>)`
  overloads that accept a composable predicate built via the JPA Criteria API instead of a fixed query.

**Intermediate**
- *Q: How would you combine two `Specification`s conditionally (e.g., name filter is optional, salary
  filter is optional)?* A: `Specification.where(spec1).and(spec2)` — chainable, and each piece can be
  `null`-guarded before being added to the chain (Spring Data's `Specification.where(null)` is a safe
  no-op starting point).

**Advanced**
- *Q: What's the main risk of building Specifications dynamically from unvalidated user input?* A: An
  unbounded predicate combination can produce a query with no usable index (e.g., a `LIKE '%x%'` on an
  unindexed column combined with several `OR`s), silently degrading into a full table scan — dynamic
  query builders need the same query-plan scrutiny as hand-written SQL.

### 5.7 Projections (interface vs. DTO)

**Basic**
- *Q: What's an interface projection?* A: An interface with only getters, matching a subset of an
  entity's fields; Spring Data proxies it at runtime to expose just those fields from the query result.

**Intermediate**
- *Q: Does an interface projection always reduce the SQL columns fetched?* A: Only when the query
  itself selects individual aliased columns matching the projection's getters — either Spring Data
  generating the query from a derived method name, or (as this project does) a hand-written `@Query`
  with explicit aliases (`SELECT e.id AS id, e.firstName AS firstName, e.salary AS salary FROM
  Employee e`). A hand-written `@Query SELECT e FROM Employee e` would fetch the full entity regardless
  of the projection interface's shape — the interface only narrows what's exposed in Java, not what SQL
  gets generated, which is why the query shape has to be checked, not assumed from the projection type
  alone.
- *Q: What does the DTO projection's `SELECT new package.Type(...)` constructor expression actually do
  to the SQL?* A: It genuinely limits the selected columns to exactly the constructor arguments listed
  (`id, first_name, salary` here), and the result is never attached to the persistence context.

**Advanced**
- *Q: Why does the DTO projection's keyset endpoint fall back to fetching the full entity instead of
  using the constructor expression?* A: `SELECT new ...` is JPQL-only syntax; the keyset query is
  native SQL (needed for the raw `LIMIT` clause), and native SQL has no JPQL translation step to
  interpret a constructor expression — so `getEmployeeSummariesKeyset` fetches full `Employee` rows via
  the native query and maps them to the DTO manually in the service layer instead.
- *Q: When would you pick DTO projection over interface projection in a real system?* A: Whenever you
  actually need guaranteed column-level SQL reduction (e.g., a wide entity with a few hot read-only
  fields on a high-traffic endpoint) — interface projection is more convenient syntactically but, as
  seen here, its column-pruning behavior depends entirely on *how* the backing query was written, which
  makes it a less reliable performance lever than a DTO constructor expression.

### 5.8 RestTemplate vs. WebClient vs. Feign

**Basic**
- *Q: Which of the three is Spring currently steering people away from?* A: RestTemplate — it's in
  maintenance mode; new code should prefer `WebClient` or `RestClient`.

**Intermediate**
- *Q: Is `WebClient` automatically non-blocking just because you use it?* A: No — this project proves
  the point: `DepartmentWebClient` and `WeatherWebClientService` both call `.block()` at the end of the
  chain, which forces synchronous behavior; the non-blocking benefit only materializes if you compose
  `Mono`/`Flux` chains and never block, ideally inside a WebFlux stack end-to-end.
- *Q: What's the one-line pitch for Feign over the other two?* A: Least boilerplate per call — you
  declare an interface, Spring generates the HTTP plumbing — at the cost of visibility into what's
  actually happening on the wire.

**Advanced**
- *Q: `EmployeeFeignClient.getEmployee` declares its return type as `ApiResponse<EmployeeResponse>` —
  why does that matter, given the real `/api/employees/{id}` endpoint returns `{success, message,
  data}`?* A: Because the declared return type has to match the actual response envelope, not just the
  "interesting" payload inside it — if the method declared a bare `EmployeeResponse` instead, Feign
  would try to bind the full `{success, message, data}` JSON directly onto `EmployeeResponse`'s fields,
  which don't match, and Jackson would either fail deserialization or silently produce an object with
  null/default fields. Declaring `ApiResponse<EmployeeResponse>` and unwrapping with `.data()` at the
  call site (as `FeignTestController` does) keeps the client's contract honest with what the server
  actually sends — a good example of why declarative clients need their return type checked against the
  real endpoint response, not assumed from the method name.
- *Q: If you had to pick exactly one client for all internal service-to-service calls in a production
  version of this platform, which would you pick and why?* A: `WebClient` used synchronously (via
  `RestClient`, Spring's newer purpose-built synchronous client, would be an even better fit here) for
  an MVC-stack app — it has first-class timeout/retry configuration and is actively maintained, without
  requiring the whole application to become reactive.

### 5.9 Global exception handling

**Basic**
- *Q: What does `@RestControllerAdvice` do?* A: Combines `@ControllerAdvice` (global exception
  interception across all controllers) with `@ResponseBody` (return values are serialized directly to
  the response body, no view resolution) — `GlobalExceptionHandler` uses it to centralize error
  responses instead of try/catch in every controller method.

**Intermediate**
- *Q: What are the three exception handlers in this project, and what does each map to?* A:
  `ResourceNotFoundException → 404`, `MethodArgumentNotValidException → 400` (aggregating every failed
  `@Valid` field into one joined message string), and a catch-all `Exception → 500` fallback.
- *Q: What happens to a `RestClientException` thrown by `EmployeeClient` (RestTemplate) when it's
  never explicitly caught?* A: It propagates up to the generic `Exception` handler and comes back to
  the client as a 500, with whatever `ex.getMessage()` RestTemplate produced — not a purpose-built error
  message, which is a real gap versus explicitly translating it the way the WebClient/Department path
  does.

**Advanced**
- *Q: `ErrorResponse` has no `errorCode` field — how would you add structured error codes without
  breaking existing response shape for clients already parsing `message`?* A: Add a new optional
  `errorCode` field to the record (additive, backward-compatible for JSON consumers), introduce an
  enum of codes, and have `ResourceNotFoundException` (and siblings) carry the code as a constructor
  argument instead of concatenating it into the message string — then update `GlobalExceptionHandler`
  to read `ex.getErrorCode()` instead of parsing/duplicating it from the message.
- *Q: Is exposing `ex.getMessage()` directly in the 500 handler's response body safe?* A: Not
  generally — a raw internal exception message can leak implementation details (SQL fragments, internal
  class names, stack info depending on the exception type); production hardening would replace the
  500 handler's message with a generic "internal error, reference id: X" and log the real exception
  server-side only.

### 5.10 Bean Validation

**Basic**
- *Q: What triggers Bean Validation on `EmployeeRequest`?* A: `@Valid` on the controller method
  parameter (`@Valid @RequestBody EmployeeRequest request`) tells Spring MVC to run the JSR 380
  validator against the object's constraint annotations before the method body executes.

**Intermediate**
- *Q: What validation constraints are declared on `EmployeeRequest`, and what do they guard against?*
  A: `@NotBlank` on `firstName`/`lastName`/`email` (rejects null/empty/whitespace-only), `@Email` on
  `email` (format check), `@NotNull` + `@Positive` on `salary` (rejects null and non-positive values).
- *Q: What happens when validation fails?* A: Spring throws `MethodArgumentNotValidException` before
  the controller method body ever runs, caught by `GlobalExceptionHandler`, which joins every failed
  field's message (`"firstName : First name is required, salary : Salary must be greater than zero"`)
  into one 400 response.

**Advanced**
- *Q: Bean Validation constraints live on the request DTO here — what validation, if any, happens at
  the entity/database level?* A: None beyond what the column definitions imply (e.g.,
  `nullable = false, unique = true` on `Department.departmentName`) — there's no `@NotNull`/`@Size` on
  the JPA entities themselves in this project, meaning a code path that builds an `Employee` entity
  without going through the validated DTO (e.g., a future batch-import job) would bypass validation
  entirely and rely only on the database's own constraints (or lack thereof) to catch bad data.

### 5.11 Spring profiles

**Basic**
- *Q: How does this project select which YAML profile to run?* A: `spring.profiles.active` (set via
  environment variable or launch argument) selects `local`, `dev`, or `test`; Spring Boot layers
  `application-{profile}.yml` on top of the base `application.yaml`.

**Intermediate**
- *Q: What's different about `ddl-auto` across the three profiles, and why?* A: `local: update` (schema
  kept in sync automatically against a real dev database), `test: create-drop` (fresh disposable schema
  every test run against H2), `dev: validate` (Hibernate refuses to start if the schema doesn't already
  match the entities — forces real migrations in a shared environment instead of silent auto-DDL).

**Advanced**
- *Q: Why does `application-test.yml` configure H2 with `MODE=MySQL` for one datasource and
  `MODE=PostgreSQL` for the other, instead of just using H2's default dialect for both?* A: The two
  production databases have different SQL dialects and quirks (e.g., quoting, native functions); running
  H2 in each engine's compatibility mode catches dialect-specific mistakes in native queries during
  tests that a single generic H2 mode would miss — directly relevant here since this project has native
  SQL queries (`findEmployeesNative`, `findEmployeesKeyset`) that must be valid MySQL syntax.

---

## 6. ONE-LINE REVISION SHEETS

Read these out loud once before an interview. Every line should be sayable in under ten seconds.

### JPA
- **Persistence unit** → a named scope of entities + config bound to one `EntityManagerFactory`.
- **`@OneToMany` mappedBy** → the inverse side; the FK column lives on the `@ManyToOne` (owning) side.
- **`cascade = ALL`** → parent operations (persist/remove/merge) propagate to child entities automatically.
- **`orphanRemoval = true`** → removing a child from the parent's collection deletes it, even without cascading a parent delete.
- **Soft reference (`departmentId Long`)** → plain FK column, no JPA relationship, used when entities span persistence units.
- **`FetchType.LAZY`** → loads a proxy instead of the real object; avoids unnecessary joins on every access.
- **Specification API** → composable, type-safe Criteria API predicates for dynamic queries.
- **JPQL** → entity-oriented query language, dialect-translated per persistence unit at query time.
- **Native query** → raw engine-specific SQL, no JPQL translation, tied to one `DataSource`.
- **Interface projection** → getter-only interface; prunes SQL columns only when the query selects aliased fields matching the getters.
- **DTO projection (constructor expression)** → `SELECT new Type(...)` reliably selects only the listed columns.
- **Dirty checking** → Hibernate auto-detects field changes on managed entities and flushes them on commit.

### Pagination
- **Offset pagination** → `Pageable`/`Page<T>`; simple, supports random page access, degrades and destabilizes at scale.
- **Cursor pagination (this project)** → JPQL seek query, `WHERE id > :cursor`, `Pageable` used only to express the limit.
- **Keyset pagination (this project)** → same seek algorithm as cursor, written as native SQL with `LIMIT`.
- **`hasNext` trick** → fetch `size + 1` rows, trim to `size`, no second `COUNT` query needed.
- **`CursorPageResponse<T>`** → minimal envelope (`content`, `nextCursor`, `hasNext`) for seek-based pages, no total count.

### REST Clients
- **RestTemplate** → blocking, synchronous, maintenance mode — avoid for new code.
- **WebClient** → reactive-capable client; only actually non-blocking if you never call `.block()`.
- **`.block()`** → converts a reactive `Mono`/`Flux` chain back into synchronous, blocking behavior.
- **Known limitation** → WebClient used synchronously in MVC stack — reactive benefit not realized. Production fix: migrate to Spring WebFlux or use RestClient.
- **Feign** → declarative interface-based client; least boilerplate, least visibility into the wire call.
- **Absolute URI overrides baseUrl** → passing a full URL into `WebClient.uri()` ignores the bean's configured base URL.
- **`@EnableFeignClients`** → triggers classpath scan for `@FeignClient` interfaces at startup.

### Spring Boot
- **`@Primary`** → resolves ambiguity when multiple beans of the same type exist and no qualifier is given.
- **`@EnableJpaRepositories(basePackages, ...Ref)`** → scopes a repository package to a specific `EntityManagerFactory`/`TransactionManager`.
- **`@RestControllerAdvice`** → global, centralized exception-to-HTTP-response mapping across all controllers.
- **`@Valid` + Bean Validation** → runs JSR 380 constraints before the controller method body executes.
- **`@Transactional` self-invocation gotcha** → calling a transactional method from within the same class bypasses the proxy, annotation is ignored.
- **Auto-configuration** → conditional `@Configuration` classes activated by classpath contents and bean absence checks.

### Testing
- **Mockito `@Mock`/`@InjectMocks`** → mocks dependencies, injects them into the class under test, no Spring context needed.
- **MockMvc** → simulates HTTP requests against controllers without starting a real server.
- **Builder pattern for test data** → `Employee.builder()...build()` instead of long constructors, readable and order-independent.
- **`application-test.yml` H2 dual mode** → in-memory DB running MySQL-compat and PostgreSQL-compat modes simultaneously, mirroring both real datasources.
- **`ddl-auto: create-drop`** → fresh schema every test run, no leftover state between runs.

### Architecture
- **Dual datasource** → two full JPA stacks (DataSource, EntityManagerFactory, TransactionManager) in one Spring context.
- **No cross-database transactions** → writes to both DBs in one flow are two separate local transactions, not atomic together.
- **`ApiResponse<T>`** → uniform success envelope (`success`, `message`, `data`) across every endpoint.
- **`ErrorResponse`** → uniform error envelope; currently has no dedicated error-code field, a known gap.
- **Error code convention** → every `ResourceNotFoundException` carries a `CODE: message` prefix (e.g. `EMPLOYEE_NOT_FOUND`); still string-based, not a dedicated `ErrorResponse` field.
- **Self-call REST clients** → Employee and Department call each other over HTTP on the same running instance, not separate services — acceptable for a learning demo, called out explicitly rather than hidden.
