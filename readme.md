# 📂 File Manager API

A Spring Boot-based file management server with full S3 capabilities, JWT authentication, role-based access control (`@PreAuthorize`), refresh token support, and Dockerized deployment.

---

## 🚀 Features

- 🔐 JWT Authentication (Access + Refresh tokens)
- 👤 User Registration & Login
- 🧑‍⚖️ Role-based Access Control (`@PreAuthorize`)
- 💾 S3-compatible file storage (e.g., MinIO, AWS S3)
- 📁 Upload, download, view, delete files
- 🐳 Dockerized application
- 🔄 Token refresh endpoint
- 📜 Swagger UI docs (`/swagger-ui.html`)

---

## 🧰 Tech Stack

- **Java 17**, **Spring Boot 3.x**
- **Spring Security**, **JJWT**
- **Amazon S3 / MinIO**
- **Docker**, **OpenAPI / Swagger**
- Optional: **MongoDB**, **Redis**, **Collabora Online**, **GraphQL**

---

## 📦 Setup

### 🔧 Prerequisites

- Java 17
- Maven 3.8+
- Docker (for MinIO or DB)

### 🛠️ Build & Run

```bash
# Build project
mvn clean install

# Run app
./mvnw spring-boot:run
```

For details about available REST endpoints see [docs/api-reference.md](docs/api-reference.md).
