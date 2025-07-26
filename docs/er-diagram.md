# Entity Relationship (ER) Diagram – File Manager System

This document outlines the core entities and relationships modeled in the File Manager system based on the ER diagram.

## 📁 Entities & Attributes

### 1. **User**
- `id`: UUID (Primary Key)
- `username`: String (Unique)
- `password`: Encrypted String
- `email`: String (Unique)
- `full_name`: String
- `enabled`: Boolean
- `created_at`: Timestamp

### 2. **Role**
- `id`: UUID (Primary Key)
- `name`: String (e.g., ROLE_ADMIN, ROLE_USER)

### 3. **UserRole**
- Composite relationship (Many-to-Many) between `User` and `Role`

### 4. **File**
- `id`: UUID (Primary Key)
- `original_filename`: String
- `stored_filename`: String
- `content_type`: String
- `size`: Long
- `upload_time`: Timestamp
- `uploader_id`: FK → `User.id`
- `storage_type`: ENUM (e.g., S3, LOCAL)

### 5. **Folder**
- `id`: UUID (Primary Key)
- `name`: String
- `parent_folder_id`: FK → `Folder.id` (Self-referencing)
- `owner_id`: FK → `User.id`

### 6. **AuditLog**
- `id`: UUID (Primary Key)
- `action`: String (UPLOAD, DELETE, DOWNLOAD, etc.)
- `file_id`: FK → `File.id`
- `user_id`: FK → `User.id`
- `timestamp`: Timestamp
- `status`: String

## 🔗 Relationships

- **User ↔ Role**: Many-to-Many via `UserRole`
- **User ↔ File**: One-to-Many (User uploads many files)
- **User ↔ Folder**: One-to-Many
- **Folder ↔ Folder**: Recursive relationship for nesting
- **User ↔ AuditLog**: One-to-Many (Logs actions by users)
- **File ↔ AuditLog**: One-to-Many

## 🗂 Storage Strategy

The design supports pluggable backends:
- S3-compliant buckets
- Local filesystem
- In-memory (testing/fallback)
- Easily extensible to NFS, FTP, etc.

## 🔐 Security Strategy

- Role-based access with `@PreAuthorize`
- JWT Auth for stateless session control
- Refresh token support for long-lived sessions

## 🧩 Extensibility

- Additional `StorageProvider` interface for third-party or hybrid storage
- Entity listeners for automatic auditing
- Swagger-enabled for easy API documentation
