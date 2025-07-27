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

### 🐳 Running with Docker Compose

To start the application together with a MySQL database run:

```bash
docker-compose up --build
```

The database credentials can be tweaked via the `DB_*` environment variables in
`docker-compose.yml`.

## 🎯 Project Goals

- **Application Storage**: users can store files in the application's default storage
  backend.
- **Workspace Management**: any user can create a workspace. The creator becomes
  the workspace admin and can manage storages and user permissions within it.
- **User Management**: workspace admins can add or remove members and adjust
  their rights on storage locations.
- **Pluggable Storage Types**: beyond the default storage, workspaces can attach
  custom storage locations. Supported types include local folders, FTP/SCP remote
  servers, S3-compatible buckets (via signed URLs), and generic blob storage.
- **Groups Inside Workspaces**: admins can organize members into groups to grant
  or restrict access to folders.
- **Chat**: users can chat one‑on‑one with others in their roster and share files;
  workspace chats enable group conversations via WebSocket.

## 🔮 Future Plans

- Collaborative editing through Collabora Online integration.
- File versioning with configurable retention per storage.
