# File Manager Java SDK

This module provides a lightweight client for the File Manager API. It handles
authentication, HTTP requests and WebSocket connectivity.

Features include:

- Login with automatic token refresh
- Simple REST helper methods
- WebSocket client with pluggable message handlers
- Automatic activation of handlers based on server supported features

The SDK targets **Java 17** and uses only the JDK HTTP and WebSocket APIs plus
Jackson for JSON mapping.
