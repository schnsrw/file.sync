# File Manager JavaScript SDK

This package provides a lightweight TypeScript client for the File Manager API. It mirrors the features of the Java SDK but is designed for use in browser based applications.

## Build

Install dependencies and compile the TypeScript sources:

```bash
npm install
npm run build
```

The compiled files are emitted in the `dist/` directory.

## Usage

Import the client into your project:

```ts
import { FileSyncClient } from 'filemanager-js-sdk';
```

Then create an instance and register handlers as needed:

```ts
const client = FileSyncClient.builder()
  .baseUrl('http://localhost:8080')
  .username('user')
  .password('pass')
  .build();

client.registerHandler('chat', (p, msg) => console.log(msg));
client.connectWebSocket();
```

The client automatically reconnects the WebSocket and refreshes tokens when required.

## Web Interface

The backend API also serves a browser interface that can be used for manual
testing. When running locally you can open:

- http://localhost:8080/login
- http://localhost:8080/dashboard
- http://localhost:8080/drive
- http://localhost:8080/chat
- http://localhost:8080/notes
- http://localhost:8080/invitations
- http://localhost:8080/admin (admin console)

