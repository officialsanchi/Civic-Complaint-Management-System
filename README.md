# Civic Complaint Management System

A production-ready Spring Boot REST API that allows citizens to report civic issues (road damage, waste, electricity, water, etc.), track their complaints, and receive updates. Admins can manage, assign, and resolve complaints with full analytics.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Security | Spring Security + JWT (jjwt 0.12) |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL (prod) / H2 (dev) |
| Validation | Jakarta Bean Validation |
| Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Build | Maven |

---

## Architecture — Separation of Concerns (SOC)

```
com.civic.complaint
├── config/           # Security, OpenAPI, CORS, DataSeeder
├── controller/       # REST controllers (HTTP layer only)
├── dto/
│   ├── request/      # Inbound validated request objects
│   └── response/     # Outbound response objects + ApiResponse wrapper
├── exception/        # Custom exceptions + GlobalExceptionHandler
├── model/            # JPA entities + enums
├── repository/       # Spring Data JPA repositories
├── security/         # JwtUtils + JwtAuthFilter
└── service/
    ├── AuthService.java
    ├── ComplaintService.java
    ├── NotificationService.java
    ├── AdminService.java
    └── impl/         # Service implementations
```

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+ (or use the dev H2 profile)

### Run with H2 (development — no database needed)

```bash
cd civic-complaint-system
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The app starts on `http://localhost:8080`.
H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:civic_complaints`)

### Run with PostgreSQL (production)

1. Create the database:
```sql
CREATE DATABASE civic_complaints;
```

2. Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/civic_complaints
spring.datasource.username=YOUR_USER
spring.datasource.password=YOUR_PASSWORD
```

3. Run:
```bash
mvn spring-boot:run
```

### Build JAR
```bash
mvn clean package -DskipTests
java -jar target/complaint-system-1.0.0.jar
```

---

## Default Accounts (seeded on first startup)

| Role | Username | Password |
|---|---|---|
| ADMIN | `admin` | `Admin@1234` |
| CITIZEN | `citizen1` | `Citizen@1234` |

> **Change these passwords immediately in production!**

---

## API Documentation

Swagger UI is available at: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON: `http://localhost:8080/api-docs`

### Authentication

All protected endpoints require:
```
Authorization: Bearer <JWT_TOKEN>
```

---

## API Reference

### Auth Endpoints (`/api/auth`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new citizen |
| POST | `/api/auth/login` | Public | Login, get JWT |
| GET | `/api/auth/me` | Required | Get current user profile |
| PUT | `/api/auth/me` | Required | Update profile |
| PATCH | `/api/auth/me/change-password` | Required | Change password |

#### Register
```json
POST /api/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Secret@123",
  "fullName": "John Doe",
  "phone": "+2348012345678"
}
```

#### Login
```json
POST /api/auth/login
{
  "usernameOrEmail": "john_doe",
  "password": "Secret@123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "role": "CITIZEN"
    }
  }
}
```

---

### Complaint Endpoints (`/api/complaints`)

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| POST | `/api/complaints` | Required | CITIZEN/ADMIN | Submit a complaint |
| GET | `/api/complaints` | Public | — | Search/filter complaints |
| GET | `/api/complaints/{id}` | Public | — | Get complaint details |
| GET | `/api/complaints/my` | Required | Any | My complaints |
| PUT | `/api/complaints/{id}` | Required | Owner/ADMIN | Update complaint |
| DELETE | `/api/complaints/{id}` | Required | Owner/ADMIN | Delete complaint |
| PATCH | `/api/complaints/{id}/status` | Required | ADMIN | Update status |
| POST | `/api/complaints/{id}/upvote` | Required | Any | Upvote a complaint |
| GET | `/api/complaints/all` | Required | ADMIN | All complaints (paginated) |

#### Submit Complaint
```json
POST /api/complaints
Authorization: Bearer <token>
{
  "title": "Large pothole on Broad Street",
  "description": "There is a large pothole that has been there for 3 weeks causing accidents.",
  "category": "Road",
  "location": {
    "latitude": 6.4550,
    "longitude": 3.3841,
    "address": "12 Broad Street, Lagos Island",
    "ward": "Lagos Island I",
    "lga": "Lagos Island",
    "state": "Lagos"
  },
  "imageUrls": ["https://example.com/image1.jpg"]
}
```

#### Search Complaints (all params optional)
```
GET /api/complaints?status=PENDING&category=Road&search=pothole&lga=Lagos Island&page=0&size=10&sortBy=reportedAt&sortDir=desc
```

#### Update Status (Admin only)
```json
PATCH /api/complaints/1/status
Authorization: Bearer <admin-token>
{
  "status": "IN_PROGRESS",
  "adminNote": "Assigned to Lagos State Public Works team"
}
```

Valid statuses: `PENDING`, `IN_PROGRESS`, `RESOLVED`, `REJECTED`

---

### Notification Endpoints (`/api/notifications`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/notifications` | Required | Get all notifications (paginated) |
| GET | `/api/notifications/unread` | Required | Get unread notifications |
| GET | `/api/notifications/unread/count` | Required | Get unread count |
| PATCH | `/api/notifications/{id}/read` | Required | Mark one as read |
| PATCH | `/api/notifications/read-all` | Required | Mark all as read |

---

### Admin Endpoints (`/api/admin`) — ADMIN role only

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/dashboard` | Full analytics dashboard |
| GET | `/api/admin/users` | All users (search by name/email) |
| GET | `/api/admin/users/{id}` | Get specific user |
| PATCH | `/api/admin/users/{id}/toggle` | Enable/disable user |
| PATCH | `/api/admin/users/{id}/promote` | Promote to ADMIN |
| DELETE | `/api/admin/users/{id}` | Delete user |

#### Dashboard Response
```json
{
  "totalComplaints": 250,
  "pendingComplaints": 80,
  "inProgressComplaints": 45,
  "resolvedComplaints": 110,
  "rejectedComplaints": 15,
  "totalUsers": 500,
  "totalCitizens": 495,
  "totalAdmins": 5,
  "complaintsThisMonth": 38,
  "complaintsThisWeek": 12,
  "topCategories": [
    { "category": "Road", "count": 95 },
    { "category": "Waste", "count": 72 }
  ],
  "topLgas": [
    { "lga": "Lagos Island", "count": 65 }
  ],
  "statusBreakdown": {
    "PENDING": 80,
    "IN_PROGRESS": 45,
    "RESOLVED": 110,
    "REJECTED": 15
  }
}
```

---

## Complaint Categories

Supported categories (free-text, but these are recommended):
- `Road` — potholes, damaged roads, missing signs
- `Waste` — refuse dumps, blocked drainage
- `Electricity` — power outages, fallen cables
- `Water` — pipe bursts, water supply issues
- `Security` — streetlights, vandalism
- `Environment` — flooding, deforestation

---

## Security Details

- Passwords hashed with **BCrypt** (strength 10)
- Stateless **JWT** authentication (configurable TTL, default 24h)
- Role-based access control via `@PreAuthorize` + Spring Security
- CORS configured for all origins (restrict in production)
- Public endpoints: auth, GET complaints, Swagger UI
- Admin endpoints: fully guarded by `ROLE_ADMIN`

---

## Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=ComplaintServiceImplTest

# With coverage
mvn test jacoco:report
```

---

## Environment Variables (Production)

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/civic_complaints
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your-db-password
APP_JWT_SECRET=your-256-bit-base64-secret
APP_JWT_EXPIRATION_MS=86400000
```

Generate a secure JWT secret:
```bash
openssl rand -base64 32
```

---

## Notification Types

| Type | Trigger |
|---|---|
| `COMPLAINT_SUBMITTED` | Complaint created |
| `STATUS_UPDATED` | Status changed to IN_PROGRESS or PENDING |
| `COMPLAINT_RESOLVED` | Status changed to RESOLVED |
| `COMPLAINT_REJECTED` | Status changed to REJECTED |
| `COMPLAINT_UPVOTED` | Complaint receives an upvote |
| `ADMIN_MESSAGE` | Admin sends a direct message |
| `SYSTEM` | System-level notification |
