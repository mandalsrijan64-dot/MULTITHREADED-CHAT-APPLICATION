# Java Multithreaded Chat Application

A **real-time client-server chat app** built with Java Sockets and Multithreading.  
No external libraries — pure JDK only.

---

## Project Structure

```
ChatApp/
├── src/
│   ├── ChatServer.java     ← server + ClientHandler thread
│   └── ChatClient.java     ← client + ReadThread
└── README.md
```

---

## Architecture

```
                        ChatServer (main thread)
                        ServerSocket.accept() loop
                               │
              ┌────────────────┼────────────────┐
              │                │                │
     ClientHandler          ClientHandler    ClientHandler
      (Thread-1)             (Thread-2)       (Thread-3)
      Alice                  Bob              Charlie
              │                │                │
              └────────────────┴────────────────┘
                       broadcast() / privateMessage()
                       ConcurrentHashMap<username, handler>


    Each ChatClient runs TWO threads:
    ┌─────────────────────────────┐
    │  Main Thread                │  keyboard input → server
    │  ReadThread (daemon)        │  server messages → screen
    └─────────────────────────────┘
```

---

## How to Run

### Step 1 — Compile (once)
```bash
cd src
javac *.java
```

### Step 2 — Start the server (Terminal 1)
```bash
java ChatServer
```
Output:
```
╔══════════════════════════════════════════╗
║       Multithreaded Chat Server          ║
╚══════════════════════════════════════════╝
  Port      : 12345
  Waiting for clients …
```

### Step 3 — Start Client 1 (Terminal 2)
```bash
java ChatClient
```
Enter username: `Alice`

### Step 4 — Start Client 2 (Terminal 3)
```bash
java ChatClient
```
Enter username: `Bob`

### Step 5 — Start Client 3 (Terminal 4)
```bash
java ChatClient
```
Enter username: `Charlie`

---

## Commands

| Command | Description |
|---------|-------------|
| Just type and Enter | Public message to everyone |
| `@username message` | Private message to one user |
| `/users` | List all online users |
| `/quit` | Disconnect from server |

---

## Example Session

**Alice's terminal:**
```
Enter your username: Alice
✔  Logged in as: Alice
─────────────────────────────────────
Commands:
  @username <msg>  →  private message
  /users           →  list online users
  /quit            →  disconnect
─────────────────────────────────────

*** Bob joined the chat (2 online) ***
Hello everyone!
[10:23:01] Alice: Hello everyone!
[10:23:05] Bob: Hey Alice!
@Bob This is a private message
[10:23:10] [PM to Bob] This is a private message
/users
── Online users (2) ──
  • Alice (you)
  • Bob
```

**Bob's terminal:**
```
[10:23:01] Alice: Hello everyone!
Hey Alice!
[10:23:05] Bob: Hey Alice!
[10:23:10] [PM from Alice] This is a private message
```

---

## Custom Host & Port

```bash
# server on port 9000
java ChatServer 9000

# client connecting to a different machine
java ChatClient 192.168.1.10 9000
```

---

## Key Concepts

| Concept | Where |
|---------|-------|
| `ServerSocket` + `Socket` | `ChatServer.main()` |
| One thread per client | `ClientHandler implements Runnable` |
| `ExecutorService` thread pool | `ChatServer.main()` |
| `ConcurrentHashMap` (thread-safe) | `ChatServer.clients` |
| Broadcast to all clients | `ChatServer.broadcast()` |
| Private messaging | `ChatServer.privateMessage()` |
| Two-thread client (read + write) | `ChatClient` + `ReadThread` |
| `volatile` shared flag | `ChatClient.running` |
| Graceful disconnect handling | `ClientHandler.disconnect()` |
