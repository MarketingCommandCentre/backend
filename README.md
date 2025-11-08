# Marketing Command Centre - Spring Boot API

## Overview
This is a Spring Boot backend API using SQLite as the database with comprehensive CRUD operations for Requests and Audit Events.

## Technology Stack
- **Spring Boot 3.5.7**
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
Configuration can be found in `src/main/resources/application.properties`:
- Database connection settings
- JPA/Hibernate settings
- Security settings (currently disabled for development)

## Features
✅ RESTful API with JSON responses
✅ Automatic audit logging for all Request operations
✅ SQLite database with JPA/Hibernate
✅ CORS enabled for frontend integration
✅ Lombok for clean code
✅ Spring Security excluded for easy development (can be enabled later)
✅ Comprehensive error handling

## Next Steps
- Add validation annotations to models
- Implement custom exception handling
- Add pagination and sorting
- Enable and configure Spring Security
- Add API documentation (Swagger/OpenAPI)
- Write integration tests
