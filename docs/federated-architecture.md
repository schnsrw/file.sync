# Federated and Clustered Architecture for a Distributed File Manager

This document summarizes the reference design for a decentralized, federated file management system. The goal is to enable privacy-aware data placement, real-time federation, and stateful clustering without a single point of failure.

## Key Principles

### Federation
- **Full mesh** between all regions; no central server
- **Global user cache** containing username, region and federation ID
- **Rights stored regionally** and replicated only for users who need them
- **Kafka-based event sync** for federation topics

### Clustering
- **Redis service discovery** with TTL entries per service
- **Stateful services** that map active user sessions to node IPs
- **Redis Pub/Sub** for intra-cluster communication
- **Kafka** for inter-region synchronization

## High-Level Architecture

Each region hosts the following components:
- Spring Boot File Service (stateful)
- Redis for service registry, user sessions and Pub/Sub
- MongoDB or S3-compatible storage backend
- Kafka broker for federation events

### Kafka Topics
- `federation.user.register`
- `federation.rights.granted`
- `federation.rights.revoked`
- `file.accessed`
- `file.modified`
- `user.connected`
- `user.disconnected`

## Federation Flow: User Registration
1. User registers in region A.
2. Region A stores the user locally and publishes to `federation.user.register`.
3. All other regions consume the event and cache the user metadata.

```json
{
  "username": "raj",
  "region": "in",
  "federationId": "in:raj",
  "email": "raj@example.com",
  "timestamp": "2025-07-29T13:00:00Z"
}
```

## Federation Flow: Rights Granting
1. User A (region IN) shares a file with B (region EU).
2. Rights are stored in both the owner region and the target region.
3. Event is published to `federation.rights.granted`.

```json
{
  "fileId": "abc123",
  "ownerRegion": "in",
  "targetRegion": "eu",
  "targetUser": "eu:john",
  "rights": "read",
  "timestamp": "2025-07-29T13:01:00Z"
}
```

## File Access Flow
1. User B in EU requests a file stored in IN.
2. Routing sends the request to region IN.
3. Region IN validates rights and logs access to Kafka.

```json
{
  "fileId": "abc123",
  "byUser": "eu:john",
  "action": "viewed",
  "timestamp": "2025-07-29T13:02:00Z",
  "originRegion": "in"
}
```

## Clustering Flow: User Session Management

Redis keys:
- `service:{nodeId}` → `{ip}:{port}` (TTL 300)
- `user:{username}` → `{ip}:{port}` (TTL 300)
- `federation:user:{username}` → `{region}:{federationId}`
- `rights:{userId}:{fileId}` → rights entry

Keys are refreshed every 60 seconds.

Kafka schema for `user.connected`:
```json
{
  "username": "raj",
  "service": "192.168.1.20:8080",
  "region": "in",
  "timestamp": "2025-07-29T13:03:00Z"
}
```

## WebSocket Communication Flow
1. Client connects via WebSocket to a service node.
2. The node stores the user session in Redis.
3. To message another user, look up the recipient via `user:{username}` and route via REST, WebSocket, gRPC or Redis Pub/Sub.

Redis Pub/Sub message example:
```json
{
  "type": "message",
  "from": "in:raj",
  "to": "in:sara",
  "channel": "user.in.sara",
  "payload": {
    "type": "file_comment",
    "fileId": "abc123",
    "message": "Check this update.",
    "timestamp": "2025-07-29T13:05:00Z"
  }
}
```

## UML Sequences

**File Access by Federated User**
```
User(B) --> Region EU FileService : request file access
Region EU FileService --> Region IN FileService : access request
Region IN FileService --> Region IN DB : validate rights
Region IN FileService --> Region EU FileService : stream file
Region EU FileService --> User(B) : deliver file stream
```

**Cluster Message Routing (Redis Pub/Sub)**
```
User(A) --> Node1 : Send message to User(B)
Node1 --> Redis PubSub : PUBLISH user.in.bob { message }
Node2 (Subscribed) --> Receives message
Node2 --> WebSocket : Deliver message to User(B)
```

## Technologies Used
- Spring Boot
- Redis (service registry, session map, Pub/Sub)
- Kafka (federation events)
- MongoDB/S3 storage
- Docker Swarm or Kubernetes
- CloudFront or GeoDNS for region-aware routing

## Optional Enhancements
- JWTs containing region, user ID and auth level
- Rights with TTL that auto-expire
- Regional dashboards using Kafka Streams
- Kafka dead-letter topics for audit and retry

## Summary
This architecture focuses on minimal memory usage, regional load balancing and privacy-aware data placement. Federation is fully decentralized with Kafka-based sync, while Redis enables stateful clustering and direct message routing between nodes.
