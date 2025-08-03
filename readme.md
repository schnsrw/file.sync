# 📂 File Manager API

A Spring Boot–based file management server featuring S3‑compatible storage,
JWT authentication and role‑based access control. The project exposes REST and
WebSocket endpoints and includes SDKs for Java and JavaScript clients.

---

## 🚀 Features

- JWT authentication with access and refresh tokens
- User registration, login and profile management
- Role-based access control and fine‑grained rights for files and folders
- File upload/download and favourite marking
- Folder creation and content listing
- Connection requests and WebSocket chat with message history
- Storage management with S3/MinIO support and credential testing
- Activity logs for files and folders
- Java and JavaScript SDKs
- Dockerized deployment with Swagger UI

---

## 🧰 Tech Stack

- **Java 17**, **Spring Boot 3.x**
- **Spring Security**, **JJWT**
- **Amazon S3 / MinIO**
- **Docker**, **OpenAPI / Swagger**
- Optional: **MongoDB**, **Redis**, **Collabora Online**
- Redis-backed caches for user data, rosters, files, folders and rights

---

## 📦 Setup

### 🔧 Prerequisites

- Java 17
- Maven 3.8+
- Docker (for MinIO or DB)

### 🛠️ Build & Run

```bash
mvn clean install
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

Redis is bundled in the compose file for caching. Ensure the Redis container is
running if you start the application without Docker Compose.

For local development without running the application container you can start
the supporting databases using:

```bash
docker-compose -f docker-compose.dev.yml up -d
```

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

## 🌐 Web Interface

Besides the REST API the application ships with a simple browser UI. When the
server is running locally at `http://localhost:8080` the following pages are
available:

- `/login` – user sign‑in screen
- `/dashboard` – landing page after login
- `/drive` – file and folder browser
- `/chat` – real‑time chat view
- `/notes` – personal notes
- `/invitations` – manage connection requests
- `/admin/login` – admin sign‑in
- `/admin` – admin dashboard
- `/admin/logs` – view application logs
- `/swagger-ui/index.html` – interactive API docs

These pages surface the functionality implemented so far and are useful for
quick manual testing.

## 🛣 Roadmap

- File and folder deletion
- Recursive folder tree APIs
- Admin user management
- Collaborative editing via Collabora Online
- File versioning with retention policies
