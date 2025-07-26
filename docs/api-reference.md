# API Reference

This document outlines the REST APIs for the File Manager application.

## Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/login` | Authenticate a user and return JWT access and refresh tokens. |
| `POST` | `/auth/refresh` | Provide a new access token using a valid refresh token. |
| `POST` | `/auth/register` | Register a new user account. |

## File Operations

These endpoints handle file management. They require a valid JWT token.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/file/upload` | Upload a file to the configured storage. |
| `GET` | `/file/list` | List files owned by the current user. |
| `GET` | `/file/{id}` | Download or view a specific file. |
| `DELETE` | `/file/{id}` | Delete a file. |
| `POST` | `/file/{id}/rename` | Rename a file. |

## Folder Operations

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/folder/create` | Create a new folder. |
| `GET` | `/folder/list` | List folders for the current user. |
| `DELETE` | `/folder/{id}` | Delete a folder. |

## Admin APIs

APIs restricted to users with the `ADMIN` role.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/file/admin/only` | Test endpoint accessible only by admins. |
| `GET` | `/users` | List all users. |
| `POST` | `/storage` | Register a new storage configuration. |

## Future Enhancements

Additional endpoints such as collaborative editing or GraphQL queries can be added later. All APIs are documented via Swagger UI at `/swagger-ui.html` when the application is running.
