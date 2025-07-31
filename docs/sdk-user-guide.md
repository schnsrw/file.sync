# File Manager SDK – User Guide

This guide explains how to use the Java SDK that ships with the File Manager project.
It covers registration, OTP verification, login, connection management and WebSocket communication.

## Installation

1. Build and install the SDK into your local Maven repository:

   ```bash
   mvn -f sdk/java/pom.xml install
   ```

2. Add the dependency to your application `pom.xml`:

   ```xml
   <dependency>
       <groupId>in.lazygod</groupId>
       <artifactId>filemanager-sdk</artifactId>
       <version>0.1-SNAPSHOT</version>
   </dependency>
   ```

## Registration and OTP Verification

```java
FileSyncClient client = FileSyncClient.builder()
        .baseUrl("http://localhost:8080")
        .build();

client.register(new RegisterRequest("alice", "secret", "Alice", "alice@example.com"));
client.verifyOtp("<userId>", "121212");
```

After verification the client stores the issued access and refresh tokens internally.

## Logging in

Once a user is verified, configure the builder with a username and password. The SDK
retrieves and refreshes tokens automatically when you invoke authenticated requests.

```java
FileSyncClient client = FileSyncClient.builder()
        .baseUrl("http://localhost:8080")
        .username("alice")
        .password("secret")
        .build();

String json = client.get("/users/me");
```

## Connection management

Use the helper methods to manage user connections:

```java
client.requestConnection("bob");
client.acceptConnection("<connectionId>");
client.rejectConnection("<connectionId>");
client.listPendingConnections();
client.listConnectedUsers(0, 10);
```

## WebSocket Features

Use `connectWebSocket()` to open a WebSocket connection. The SDK automatically
activates packet handlers only for server-supported features such as chat,
user presence and connection requests.

```java
client.registerHandler("chat", (p, payload) -> System.out.println(payload));
client.connectWebSocket().join();

// Connection automatically tries to rejoin if the server drops it

client.sendChatMessage("bob", "Hello");
client.requestRecent("bob", null);
client.sendPing();
```

## Further reading

Refer to the project README for server configuration details.
