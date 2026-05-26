COMPANY:CODTECH IT SOLUTIONS

NAME:SRIJAN MANDAL

INTERN ID:CTIS8634

DOMAIN:JAVA PROGRAMMING

DURATION:4 WEEKS

MENTOR:NEELA SANTOSH



##OUTPUT

<img width="630" height="501" alt="Image" src="https://github.com/user-attachments/assets/4307f2cc-4ad2-4fb4-8f19-2ed9ff5ba201" />

## Multithreaded Chat Application Using Java – Description

A Multithreaded Chat Application using Java is a network-based communication system that allows multiple users to exchange messages simultaneously over a client-server architecture. The project demonstrates the practical implementation of multithreading, socket programming, and real-time communication in Java. It is widely used for understanding networking concepts and concurrent programming techniques.

The application consists of two main components: the server and the client. The server acts as the central system that manages connections from multiple clients, while each client represents a user participating in the chat. The server continuously listens for incoming client requests using sockets. Whenever a new user connects, the server creates a separate thread to handle communication with that client independently. This multithreading approach allows multiple users to send and receive messages at the same time without interrupting one another.

Java provides strong support for this type of application through packages such as `java.net` for socket programming and `java.io` for input and output stream handling. Threads are created using the `Thread` class or the `Runnable` interface to manage concurrent execution. Each client thread operates independently, enabling smooth and efficient communication among connected users.

The chat application can support features such as private messaging, group chats, user login systems, timestamps, message broadcasting, online user lists, and chat history storage. Messages sent by one client are transmitted to the server, which then forwards them to all other connected clients. This ensures real-time communication across the network.

One of the major advantages of multithreading is improved performance and responsiveness. Since each client connection runs in its own thread, the application can handle multiple users efficiently without freezing or delaying communication. Exception handling is also implemented to manage network failures, abrupt client disconnections, and invalid inputs.

The project can be developed as a command-line application or enhanced with graphical user interfaces (GUI) using Java Swing or JavaFX for a more interactive user experience. Security features such as encrypted communication and user authentication can also be integrated for better reliability.

Overall, the Multithreaded Chat Application using Java is an excellent project for learning client-server communication, concurrent programming, networking protocols, and real-time data exchange. It provides hands-on experience in building scalable communication systems and demonstrates the power of Java in developing distributed applications.

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
