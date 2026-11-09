# Marketing Command Centre - Spring Boot API

## Overview
This is a Spring Boot backend API using SQLite as the database with comprehensive CRUD operations for Requests and Audit Events.

## Technology Stack
- **Spring Boot 3.5.16**
- **Java 21**
- **SQLite Database**
- **Spring Data JPA**
- **Lombok**
- **Maven**

## Project Structure

### Models
- **Request**: Manages marketing requests with fields like title, description, status, priority, requestedBy
- **AuditEvent**: Tracks all system events with eventType, entityType, entityId, eventDetails, performedBy

### Repositories
- **RequestRepository**: JPA repository for Request entity with custom query methods
- **AuditEventRepository**: JPA repository for AuditEvent entity with custom query methods

### Services
- **RequestService**: Business logic for Request operations with automatic audit logging
- **AuditEventService**: Business logic for AuditEvent operations

### Controller
- **CommandCentreController**: Single REST controller managing both Request and AuditEvent endpoints

## API Endpoints

### Authentication Endpoints
- `GET /api/auth/success` - OAuth success payload (includes JWT)
- `GET /api/auth/user` - Current user info (JWT or OAuth)

### Authorization (roles)
Access to `/api/**` is decided by the `roles` claim of the JWT sent as `Authorization: Bearer <jwt>`:

| Caller | Role | Allowed |
| --- | --- | --- |
| Dashboard user (JWT issued after Discord login) | `ROLE_USER` | `GET /api/**`, plus `POST /api/discord/users/bulk` and `POST /api/discord/roles/bulk` (read-only lookups) |
| Discord bot | `ROLE_BOT` | Everything, including every `POST`/`PUT`/`PATCH`/`DELETE` under `/api/**` |

- `/api/auth/**`, `/login/**`, `/oauth2/**`, `/error`, `/` and `/v3/api-docs` are public. `/api/workload/**` is not: it needs `ROLE_USER` or `ROLE_BOT` like the rest of `/api/**`.
- Only the role names in `model/Role.java` are honoured. A token with a missing/empty `roles` claim, or with only unknown roles, grants nothing and is treated as unauthenticated.
- Unauthenticated requests are redirected to the Discord login (302); authenticated callers without the required role get 403.
- `/api/**` data endpoints are JWT-only: a browser session from the Discord OAuth login has no `ROLE_USER`, so it gets 403 there. It can still use `/api/auth/**`.
- The bot's token must carry `ROLE_BOT`. Generate it with the `generate-bot-token` profile (`config/BotTokenGenerator.java`), which signs it with the configured `security.jwt.secret`; rotating that secret invalidates every existing token, so regenerate the bot token afterwards.

### Request Endpoints
- `GET /api/requests` - Get all requests
- `GET /api/requests/{id}` - Get request by ID
- `GET /api/requests/status/{status}` - Get requests by status
- `GET /api/requests/priority/{priority}` - Get requests by priority
- `GET /api/requests/requestedBy/{requestedBy}` - Get requests by user
- `POST /api/requests` - Create a new request
- `PUT /api/requests/{id}` - Update a request
- `DELETE /api/requests/{id}` - Delete a request

### Audit Event Endpoints
- `GET /api/audit-events` - Get all audit events
- `GET /api/audit-events/{id}` - Get audit event by ID
- `GET /api/audit-events/entity/{entityType}/{entityId}` - Get audit events for specific entity
- `GET /api/audit-events/type/{eventType}` - Get audit events by type
- `GET /api/audit-events/user/{performedBy}` - Get audit events by user
- `GET /api/audit-events/daterange?start={start}&end={end}` - Get audit events by date range
- `POST /api/audit-events` - Create a manual audit event

## Database
- SQLite database file: `marketing_command_centre.db` (created automatically in project root)
- Hibernate auto-generates tables from entity models
- Audit events are automatically logged for all Request operations (CREATE, UPDATE, DELETE)

## Running the Application

### Prerequisites
- Java 21
- Maven

### Build and Run
```bash
# Using Maven wrapper
./mvnw clean install
./mvnw spring-boot:run

# Or using Maven directly
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Configuration
Configuration can be found in `src/main/resources/application.properties.example` (copy to `application.properties` or supply env overrides):
- Database connection settings
- JPA/Hibernate settings
- Discord OAuth + guild requirements
- JWT settings for user and bot authentication (provide a Base64 secret)

## Features
✅ RESTful API with JSON responses
✅ Automatic audit logging for all Request operations
✅ SQLite database with JPA/Hibernate
✅ CORS enabled for frontend integration
✅ Lombok for clean code
✅ Spring Security with Discord OAuth + JWT authentication
✅ Comprehensive error handling

## Next Steps
- Add validation annotations to models
- Implement custom exception handling
- Add pagination and sorting
- Enable and configure Spring Security
- Add API documentation (Swagger/OpenAPI)
- Write integration tests

## License

Copyright (C) 2026 Ibrahim Chehab

This program is free software: you can redistribute it and/or modify it under
the terms of the **GNU General Public License v3.0** as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the [GNU General Public License](./LICENSE) for more
details, or visit <https://www.gnu.org/licenses/gpl-3.0.html>.
