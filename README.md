# ⛪ Gestão Paróquia

[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.13-brightgreen.svg)](https://spring.io/projects/spring-boot)

**Gestão Paróquia** is a robust, monolithic API designed to modernize and centralize the administrative management of the **Paróquia Nossa Senhora de Fátima**. Built with a focus on reliability, security, and maintainability, it provides a solid foundation for managing various parish domains.

---

## 🌟 Vision

The project aims to empower parish administrators with a centralized platform to manage schedules, events, members, and organizational governance, moving away from fragmented processes to a unified digital solution.

## 🚀 Key Features

- **📅 Event Management**: Full lifecycle management of parish events, including recurrence support and automated conflict detection.
- **⚖️ Approval Workflow**: Integrated governance system for sensitive actions, requiring formal review and decision-making.
- **🏗️ Project Tracking**: Group events into strategic initiatives, with high-level aggregation of status and collaboration metrics.
- **🔒 IAM & Security**: Robust Identity and Access Management with role-based access control (RBAC) and organizational scoping.
- **📍 Location Management**: Detailed tracking of parish spaces, including capacity and resource characteristics.
- **👥 Member Management**: Comprehensive database of parish members, their involvement in pastorals, and sacramental records.
- **📊 Observability & Auditing**: Detailed operation logs, audit trails, and performance metrics for transparent administration.

## 🏗️ Architecture

The application is built following **Clean Architecture** and **Domain-Driven Design (DDD)** principles, organized as a **Modular Monolith**.

### Layering Strategy (per Domain):
1. **Domain**: Pure business logic, entities, and repository interfaces.
2. **Application**: Use cases orchestrating domain objects and external ports.
3. **API**: REST controllers, DTOs, and mappers.
4. **Infrastructure**: Implementations for persistence (JPA), security, and external integrations.

## 🛠️ Tech Stack

- **Backend**: Java 21, Spring Boot 3.3
- **Database**: PostgreSQL (Migrations with Flyway)
- **Security**: Spring Security (Session-based with custom JSON login)
- **Documentation**: 
    - **Swagger/OpenAPI**: Available at `/swagger-ui.html`
    - **Docsify**: Internal documentation site in `/docs`
- **Quality**: SonarQube, JaCoCo, JUnit 5

## 🏁 Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21 JDK (optional for local builds)

### Running with Docker
```bash
docker-compose up --build
```
This will start the API (port 8080), PostgreSQL (port 5432), and SonarQube (port 9000).

### Local Development
1. Start the database: `docker-compose up -d db`
2. Run the app: `./gradlew bootRun`

### Running the Demo
A comprehensive demonstration script is available to showcase the MVP features:
```bash
./demo_mvp.sh
```

## 📚 Documentation

The detailed documentation is available in the `/docs` folder and can be served via Docsify.
Once the app is running, you can also explore the API via **Swagger UI** at:
`http://localhost:8080/swagger-ui.html`

---

Developed with ❤️ for the **Paróquia Nossa Senhora de Fátima**.
