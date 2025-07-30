# 📂 File Manager API

A Spring Boot-based file management server with full S3 capabilities, JWT authentication, role-based access control (`@PreAuthorize`), refresh token support, and Dockerized deployment.

---

## 🚀 Features

- 🔐 JWT Authentication (Access + Refresh tokens)
- 👤 User Registration & Login
- 🧑‍⚖️ Role-based Access Control (`@PreAuthorize`)
- 💾 S3-compatible file storage (e.g., MinIO, AWS S3)
- 📁 Upload & download files
- 📂 Folder creation and listing
- 🗂️ Storage management endpoints
- 👥 Connection requests and WebSocket chat
- 📨 Recent messages history with timestamp filtering
- 👥 Roster management cached with LRU policy
- 🔐 Grant/revoke rights on files and folders
- 📝 API logging with Swagger documentation
- 🧩 Java SDK for client integrations
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

# Run app with the in-memory H2 database
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 🐳 Running with Docker Compose

To start the application together with a MySQL database run:

```bash
docker-compose up --build
```

The application will use the default MySQL configuration from `application.yml`.
Database credentials can be tweaked via the `DB_*` environment variables in
`docker-compose.yml`.

For local development without running the application container you can start
the supporting databases using:

```bash
docker-compose -f docker-compose.dev.yml up -d
```

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
  Messages are stored in MongoDB and the sender is notified once a message is delivered.

## 🔮 Future Plans

- Collaborative editing through Collabora Online integration.
- File versioning with configurable retention per storage.

## ✅ Completed Milestones

- Core authentication with JWT
- User registration and profile endpoints
- File upload and download with S3 support
- Folder management and listing
- Rights management for files and folders
- Connection requests and WebSocket chat
- Storage configuration APIs
- Java SDK skeleton for clients

## 📖 Storage Setup

You can attach AWS S3 buckets as additional storage locations.

1. Send a `POST /storage` request with `storageType` set to `S3`. Use the bucket
   name in `basePath` and provide your AWS credentials in `accessId` and
   `accessKey`.
2. Verify the credentials using `POST /storage/test`. This endpoint uploads and
   deletes a temporary file to ensure the provided details work. It works for all
   supported storage types.
3. Grant users or groups access rights on the new storage via the rights APIs.

Credentials are encrypted before being stored in the database. When using S3 the
application generates presigned URLs so files are uploaded and downloaded
directly from S3 without passing through the server.

## 🚧 In Pipeline

- File and folder deletion endpoints
- Folder tree and file listing APIs
- Admin user management endpoint
- Audit log querying
