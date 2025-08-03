# API Overview

This document summarises the REST endpoints currently exposed by the File
Manager API and highlights work that is still pending.

## Auth
- `POST /auth/register` – register a new user
- `POST /auth/{userId}/verify` – verify a user with an OTP
- `POST /auth/login` – obtain access and refresh tokens
- `POST /auth/refresh` – refresh an access token

## Users
- `GET /users/me` – fetch the authenticated user's profile
- `PATCH /users/me` – update the current user's profile
- `GET /users/connected` – list connected users
- `POST /users/{username}/disconnect` – disconnect from a user

## Connections
- `POST /connections/request/{username}` – send a connection request
- `POST /connections/{id}/accept` – accept a pending request
- `POST /connections/{id}/reject` – reject a pending request
- `GET /connections/pending` – list pending requests

## Files
- `POST /file/upload` – upload a file
- `GET /file/{id}/download` – download a file
- `POST /file/{id}/favourite` – mark or unmark a file as favourite

## Folders
- `POST /folder` – create a new folder
- `GET /folder/list` – list contents of a folder
- `POST /folder/{id}/favourite` – mark or unmark a folder as favourite

## Storage
- `POST /storage` – create a storage location
- `GET /storage` – list configured storage locations
- `POST /storage/test` – verify storage credentials

## Rights
- `POST /rights` – grant rights on a file or folder
- `DELETE /rights` – revoke rights
- `GET /rights` – list rights for a resource

## Activity Logs
- `GET /logs` – list activity logs for a file or folder

## Chat
- `GET /chat/{username}` – fetch recent messages with a user

---

### Pending APIs

The following endpoints are planned but not yet implemented:

- File and folder deletion
- Recursive folder tree APIs
- Admin user management
- Enhanced audit log querying

