# File Manager Java SDK

This module provides a lightweight client for the File Manager API. It handles
authentication, user registration and OTP verification as well as HTTP requests
and WebSocket connectivity.

## Features

- Login with automatic token refresh
- Registration and OTP verification helpers
- Simple REST helper methods
- WebSocket client with pluggable message handlers
- Automatic activation of handlers based on server supported features
- Singleton WebSocket connection that auto-reconnects unless explicitly closed
- Access to user and connection management endpoints
- Helpers for sending chat messages, ping/pong and recent message queries

The SDK targets **Java 17** and uses only the JDK HTTP and WebSocket APIs plus
Jackson for JSON mapping.

## Add to your project

Build and install the SDK locally:

```bash
mvn -f sdk/pom.xml install
```

Then declare the dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>in.lazygod</groupId>
    <artifactId>filemanager-sdk</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```

## Quick start

```java
FileSyncClient client = FileSyncClient.builder()
        .baseUrl("http://localhost:8080")
        .username("user")
        .password("pass")
        .build();

client.registerHandler("chat", (p, payload) -> System.out.println(payload));
client.connectWebSocket().join();

// the connection will automatically try to reconnect if dropped

client.sendChatMessage("bob", "Hello there!");
client.sendPing();
```

See [docs/sdk-user-guide.md](../docs/sdk-user-guide.md) for a more detailed guide.
