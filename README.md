# ServiceDesk – IT Support & Ticket Management System

## 1. Project Overview

ServiceDesk is a beginner-friendly IT support ticket management system. Employees
can raise support tickets, and support agents can view, assign, prioritize,
comment on, resolve, and close them. The system also tracks a simple SLA
(Service Level Agreement) deadline for every ticket based on its priority.

The backend is a Spring Boot REST API backed by PostgreSQL, and the frontend
is a single-page app built with plain HTML, CSS, and JavaScript (no framework).

## 2. Features

- **User management** – create Employees and Support Agents.
- **Ticket management** – create, view, list, and filter tickets.
- **Ticket operations** – update status, assign an agent, update priority.
- **Comments** – support agents can add comments / resolution notes to a ticket.
- **Simple workflow rules**:
  - A ticket starts as `OPEN`.
  - Assigning an agent automatically moves an `OPEN` ticket to `IN_PROGRESS`.
  - Only valid forward/backward transitions are allowed
    (`OPEN → IN_PROGRESS → RESOLVED → CLOSED`, with limited reopening).
  - Only `EMPLOYEE` users can create tickets.
  - Only `SUPPORT_AGENT` users can be assigned to tickets or add comments.
- **SLA monitoring** – every ticket gets a resolution deadline based on priority:
  - `LOW` → 72 hours
  - `MEDIUM` → 48 hours
  - `HIGH` → 24 hours
  - `CRITICAL` → 8 hours

  Each ticket's SLA status (`WITHIN_SLA` or `BREACHED`) is calculated live by
  comparing the current time (or resolution time, for resolved/closed tickets)
  against the deadline.
- **Dashboard** – ticket counts by status, priority, and SLA breach count.
- **Validation & error handling** – required fields, valid email format, and a
  global exception handler for not-found, invalid-request, and validation errors.

## 3. Tech Stack

- Java 17
- Spring Boot 3 (Web, Data JPA, Validation)
- PostgreSQL
- Maven
- HTML5, CSS3, vanilla JavaScript (no frontend framework)

## 4. Project Structure

```
servicedesk/
├── pom.xml
├── schema.sql
├── README.md
├── .gitignore
└── src/main/
    ├── java/com/servicedesk/
    │   ├── ServicedeskApplication.java
    │   ├── controller/       (UserController, TicketController, CommentController)
    │   ├── service/          (UserService, TicketService, CommentService)
    │   ├── repository/       (UserRepository, TicketRepository, TicketCommentRepository)
    │   ├── entity/           (User, Ticket, TicketComment, Role, Priority, Status, SlaStatus)
    │   ├── dto/               (request/response DTOs)
    │   ├── exception/        (ResourceNotFoundException, InvalidRequestException, GlobalExceptionHandler, ErrorResponse)
    │   └── config/           (CorsConfig)
    └── resources/
        ├── application.properties
        └── static/
            ├── index.html
            ├── style.css
            └── script.js
```

## 5. Database Setup

1. Install PostgreSQL locally and make sure it is running.
2. Create the database:
   ```sql
   CREATE DATABASE servicedesk_db;
   ```
3. Update `src/main/resources/application.properties` with your local
   PostgreSQL username/password if they differ from the defaults
   (`postgres` / `postgres`).
4. Tables are created automatically on startup because
   `spring.jpa.hibernate.ddl-auto=update` is set. If you'd rather create the
   schema manually, run `schema.sql` against `servicedesk_db` instead and set
   `spring.jpa.hibernate.ddl-auto=validate` (or leave it as `update`, which is
   safe to run against an existing matching schema too).

## 6. How to Run

1. Make sure PostgreSQL is running and `servicedesk_db` exists (step 5 above).
2. From the project root, build and run with Maven:
   ```bash
   mvn spring-boot:run
   ```
   or build a jar and run it:
   ```bash
   mvn clean package
   java -jar target/servicedesk.jar
   ```
3. The backend starts on `http://localhost:8080`.
4. Open `http://localhost:8080` in your browser — Spring Boot serves the
   frontend (`index.html`, `style.css`, `script.js`) directly from
   `src/main/resources/static`, so no separate frontend server is needed.

## 7. API Endpoints

### Users
| Method | Endpoint            | Description                        |
|--------|----------------------|-------------------------------------|
| POST   | `/api/users`         | Create a user                      |
| GET    | `/api/users`         | List all users                     |
| GET    | `/api/users/{id}`    | Get a user by id                   |
| GET    | `/api/users/agents`  | List all SUPPORT_AGENT users       |

### Tickets
| Method | Endpoint                        | Description                          |
|--------|----------------------------------|----------------------------------------|
| POST   | `/api/tickets`                  | Create a ticket                       |
| GET    | `/api/tickets`                  | List all tickets                      |
| GET    | `/api/tickets/{id}`             | Get a ticket by id                    |
| GET    | `/api/tickets/status/{status}`  | List tickets by status                |
| GET    | `/api/tickets/priority/{p}`     | List tickets by priority              |
| PUT    | `/api/tickets/{id}/status`      | Update ticket status                  |
| PUT    | `/api/tickets/{id}/assign`      | Assign a support agent to a ticket    |
| PUT    | `/api/tickets/{id}/priority`    | Update ticket priority                |
| GET    | `/api/tickets/stats`            | Dashboard statistics (status/priority/SLA counts) |

### Ticket Comments
| Method | Endpoint                                | Description                     |
|--------|-------------------------------------------|-----------------------------------|
| POST   | `/api/tickets/{ticketId}/comments`       | Add a comment/resolution note    |
| GET    | `/api/tickets/{ticketId}/comments`       | List comments for a ticket       |

## 8. Testing Steps

1. **Create users** (Users tab or `POST /api/users`):
   - One with role `EMPLOYEE`.
   - One with role `SUPPORT_AGENT`.
2. **Create a ticket** (New Ticket tab) as the employee — it starts as `OPEN`.
3. **Open the ticket** from the Tickets tab to see its details.
4. **Assign a support agent** — status automatically moves to `IN_PROGRESS`.
5. **Add a comment/resolution note** as the support agent.
6. **Update the status** to `RESOLVED`, then to `CLOSED`.
7. **Check the Dashboard tab** to see ticket counts and SLA breach counts
   update live.
8. To see a `BREACHED` SLA in action, create a `CRITICAL` ticket and leave it
   `OPEN` for over 8 hours (or temporarily edit its `resolution_deadline` in
   the database to a past timestamp to test immediately).

You can also test the API directly with `curl` or Postman, e.g.:
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","role":"EMPLOYEE"}'
```

## 9. Screenshots

_(Add screenshots of the Dashboard, Ticket List, and Ticket Details modal here.)_

## 10. Future Improvements

- Proper authentication/authorization (login, sessions, or tokens).
- Email notifications for SLA breaches and status changes.
- Pagination and search on the ticket list.
- File attachments on tickets and comments.
- Automated background job to flag breached tickets instead of computing on read.
