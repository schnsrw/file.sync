# Pending API Endpoints

The current codebase exposes only basic authentication endpoints and a test file endpoint. To achieve full functionality we still need several REST APIs.

## File Operations
- `POST /file/upload` – upload a file to a folder
- `GET /file/{id}` – download or view a file
- `DELETE /file/{id}` – delete a file
- `GET /file/list?folderId={id}` – list files within a folder

## Folder Operations
- `POST /folder` – create a folder
- `DELETE /folder/{id}` – remove a folder recursively
- `GET /folder/tree` – fetch folder hierarchy for the user

## Permissions & Sharing
- `POST /rights` – grant a user rights on a file or folder
- `DELETE /rights/{id}` – revoke specific rights entry
- `GET /rights?resourceId={id}` – list rights for a resource

## User Management
- `GET /users/me` – retrieve the authenticated user's profile
- `PATCH /users/me` – update user's profile information
- `GET /admin/users` – admin endpoint to list all users

## Audit & Logs
- `GET /audit` – query audit logs with filters

These endpoints are essential for a complete file manager API and correspond to entities defined in the ER diagram.
