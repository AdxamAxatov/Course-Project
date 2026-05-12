# Warehouse Multi-Client System

Multi-client extension of the Semester 1 Warehouse Management System. A client–server application for managing an inventory of household appliances (laptops and ovens) with role-based access, persistent relational storage, and socket-based communication.

This is the Semester 2 deliverable. The original single-process console version is preserved in `../Semester 1 Project/`.

---

## Architecture

Layered, no frameworks (plain JDBC + sockets + java.util.logging).

```
Client (ClientMain)
   │  TCP socket  (line-oriented text protocol)
   ▼
SocketServer ──► spawns one thread per accepted connection
   │
   ▼
ClientHandler  (per-connection Session)
   │
   ▼
Controller (auth gate + role gate)
   │
   ▼
Command Pattern  (login, find, add, remove, users, …)
   │
   ▼
Services  (AuthService, UserService, ApplianceService)
   │
   ▼
DAO  (UserDao, ApplianceDao + MutableApplianceDao subinterface)
   │
   ▼
H2 (embedded, file mode)  ◄── ConnectionPool (in-house, transactions supported)
```

Each client connection runs on its own server thread (`thread-N` in logs) and owns a private `Session` with the currently logged-in user.

---

## Roles

| Role     | Read items | Add / remove items | Manage users |
|----------|:----------:|:------------------:|:------------:|
| ADMIN    | ✓          | ✓                  | ✓            |
| USER     | ✓          | ✓                  |              |
| VISITOR  | ✓          |                    |              |

VISITOR is entered with the `visit` command (no password). ADMIN and USER are entered with `login <username> <password>`.

---

## Tech stack

- **Java 17** (compiles on JDK 17+; tested on JDK 26)
- **Maven** for build, test, and run
- **H2** embedded database (file mode by default)
- **JUnit 5** for unit + parameterized tests
- **Mockito** for mocking the DAO layer in service tests
- **Failsafe** for the integration test phase (`*IT.java`)

No Spring, no Hibernate, no other side frameworks.

---

## Build & test

The Oracle Java VS Code extension bundles a compatible Maven; that's what these examples use. Any Maven ≥ 3.6 will work.

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.1"
$mvn = "$env:USERPROFILE\.vscode\extensions\oracle.oracle-java-25.1.0\nbcode\java\maven\bin\mvn.cmd"

& $mvn compile           # compile only
& $mvn test              # unit tests (Surefire)
& $mvn verify            # unit + integration tests (Surefire + Failsafe)
& $mvn package           # produces target/warehouse-multiclient-2.0.0.jar
```

---

## Run

Server and client are two separate JVM entry points. Run them in two terminals.

```powershell
& $mvn exec:java@server   # terminal 1
& $mvn exec:java@client   # terminal 2 (and any additional terminals for more clients)
```

Or directly:

```powershell
java -cp "target/warehouse-multiclient-2.0.0.jar;$env:USERPROFILE/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar" com.adxam.warehouse.app.ServerMain

java -cp "target/warehouse-multiclient-2.0.0.jar" com.adxam.warehouse.app.ClientMain [host] [port]
```

**Default login:** `admin / admin` (seeded on first run, then a no-op).

The server listens on `localhost:7070` by default. Override via `server.port` in `src/main/resources/app.properties`.

---

## Configuration

Server behaviour is driven by `src/main/resources/app.properties`:

| Property         | Default                                         | Description                          |
|------------------|-------------------------------------------------|--------------------------------------|
| `storage`        | `jdbc`                                          | `jdbc` (read+write) or `csv` (read-only fallback) |
| `server.port`    | `7070`                                          | TCP port the server listens on       |
| `db.url`         | `jdbc:h2:./data/warehouse;AUTO_SERVER=TRUE`     | JDBC URL for H2                      |
| `db.user`        | `sa`                                            | DB user                              |
| `db.password`    | *(empty)*                                       | DB password                          |
| `db.poolSize`    | `5`                                             | Connection pool size                 |
| `admin.username` | `admin`                                         | Seeded admin username (first run)    |
| `admin.password` | `admin`                                         | Seeded admin password (first run)    |
| `log.level`      | `INFO`                                          | `SEVERE`, `WARNING`, `INFO`, `FINE`  |

Customizable main: `ServerMain` accepts an alternate properties name as its first argument: `java ... ServerMain prod` loads `prod.properties`.

---

## Command reference

```
login <username> <password>         authenticate as user/admin
visit                                enter as visitor (read-only)
logout                               clear the current session
whoami                               show current user
help                                 print this list

find <laptops|ovens|all> [price=min;max]      list items (all roles)
cost <laptops|ovens|all>                       total inventory value
cheapest                                       lowest-priced item in stock

add laptop <name> <weight> <price> <quantity> <os> <cpu> <battery>   (USER, ADMIN)
add oven   <name> <weight> <price> <quantity> <power> <capacity>     (USER, ADMIN)
remove laptop <id>                                                    (USER, ADMIN)
remove oven   <id>                                                    (USER, ADMIN)

users list                                              (ADMIN)
users add <username> <password> <role>                  (ADMIN)
users delete <id>                                       (ADMIN)

exit                                 close the session
```

`price=min;max` is an optional filter on `find all`, e.g. `find all price=500;1500`.

---

## Wire protocol

Line-oriented UTF-8 text over TCP.

Client → Server: one command per line.
Server → Client: response body (may span multiple lines), terminated by a sentinel line `<<END>>`.

Example exchange:

```
S: Welcome to the Warehouse Server.
S: Type 'help' for commands, or 'login <user> <pass>' to begin.
S: <<END>>
C: login admin admin
S: Logged in as admin (ADMIN)
S: <<END>>
C: find laptops
S: Laptop{id=1, name='X1Carbon', weight=1.1, price=2200, quantity=3, os='Linux', cpu='Intel-i7', battery=450}
S: <<END>>
```

---

## Testing techniques

| Type                       | Tool                    | Where                                     |
|----------------------------|-------------------------|-------------------------------------------|
| Unit                       | JUnit 5                 | `*Test.java` (Surefire)                   |
| Parameterized              | JUnit 5 params          | `RoleTest`, `CommandArgsTest`, `PasswordHasherTest` |
| Mock-based                 | Mockito                 | `AuthServiceImplTest`, `UserServiceImplTest` |
| Integration (real H2)      | JUnit 5 + Failsafe      | `*IT.java` — `JdbcUserDaoIT`, `JdbcLaptopDaoIT`, `ControllerEndToEndIT` |
| Connection pool concurrency| JUnit + threads         | `ConnectionPoolTest`                      |

Test data is isolated from main data: each integration test class uses its own randomly-named in-memory H2 (`jdbc:h2:mem:…-<UUID>`) and the test CSVs live under `src/test/resources/`.

---

## Project structure

```
Semester 2 Project/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/adxam/warehouse/
    │   │   ├── app/         ServerMain, ClientMain
    │   │   ├── config/      Config, PropertiesConfigImpl
    │   │   ├── controller/  Session, Controller(Impl/Factory), Request, Response
    │   │   │   └── command/ Login/Logout/Visit/Whoami/Help/Find/Cost/Cheapest/Add/Remove/Users/Exit/Wrong
    │   │   ├── criteria/    SearchCriteria, AbstractCriteria, Parameter, Laptop/OvenSearchCriteria
    │   │   ├── dal/         ApplianceDao, MutableApplianceDao, UserDao, factories, DaoException
    │   │   │   └── db/      ConnectionPool, Database, SchemaInitializer, Jdbc*Dao
    │   │   ├── entity/      Appliance (base), Laptop, Oven, Range, User, Role
    │   │   ├── interfacepkg/ PowerConsumable
    │   │   ├── net/         SocketServer, ClientHandler, Protocol
    │   │   ├── service/     ApplianceService, AuthService, UserService (+ Impls + Factories)
    │   │   ├── source/      AbstractCsvSource, LaptopCsvSourceImpl, OvenCsvSourceImpl (read-only fallback)
    │   │   └── util/        PasswordHasher, CommandArgs, ValidationException, Logging
    │   └── resources/       app.properties, Laptops.csv, Ovens.csv
    └── test/
        ├── java/com/adxam/warehouse/
        │   ├── controller/  SessionTest, ControllerEndToEndIT
        │   ├── dal/db/      JdbcUserDaoIT, JdbcLaptopDaoIT, ConnectionPoolTest
        │   ├── entity/      RoleTest
        │   ├── service/     AuthServiceImplTest, UserServiceImplTest
        │   └── util/        PasswordHasherTest, CommandArgsTest
        └── resources/       laptops1-test.csv, ovens1-test.csv
```

---

## Design notes

- **Subinterface for write operations.** `ApplianceDao<A>` stays read-only (CSV impl still works). `MutableApplianceDao<A>` extends it with `insert` and `deleteById`, implemented only by the JDBC DAOs. The service checks at runtime and gracefully reports "storage is read-only" when CSV mode is selected.
- **Two ApplianceDao implementations** coexist: `ApplianceDaoImpl` (CSV, read-only) and `JdbcApplianceDao` (H2). Wired via `storage=jdbc|csv` in properties.
- **Session per connection.** Each socket has its own `Session`; threads never share session state. The DB and pool are the only shared resources, accessed thread-safely.
- **Transactions** are used on every write path via `Database.inTransaction(...)`, with rollback on any exception.
- **Connection pool** is hand-rolled (~80 lines, no third-party): proxied connections return to the pool on `close()`.
- **OOP-style input validation:** `CommandArgs` exposes typed `requireString` / `requireLong` / `requireDouble` / `requireInt` accessors that throw `ValidationException`. The controller catches once at the dispatch layer; commands never do try/catch boilerplate for parsing.
- **Authentication.** Passwords are stored as SHA-256 digests over a 16-byte random per-user salt. `PasswordHasher.matches` uses `MessageDigest.isEqual` for constant-time comparison.
