import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * ============================================================
 *  ChatServer.java  –  Multithreaded Chat Server
 * ------------------------------------------------------------
 *  • Listens on a configurable port (default 12345)
 *  • Spawns a new ClientHandler thread per connected client
 *  • Broadcasts messages to ALL connected clients
 *  • Supports private messages:  @username message
 *  • Maintains a live list of online users
 *  • Gracefully handles client disconnections
 *
 *  Run:
 *    javac *.java
 *    java ChatServer          (uses port 12345)
 *    java ChatServer 9000     (uses port 9000)
 * ============================================================
 */
public class ChatServer {

    // ── configuration ─────────────────────────────────────
    private static final int DEFAULT_PORT    = 12345;
    private static final int MAX_CLIENTS     = 50;

    // ── shared state (thread-safe) ────────────────────────
    /** Maps username → their handler thread */
    private static final ConcurrentHashMap<String, ClientHandler> clients
            = new ConcurrentHashMap<>();

    private static final DateTimeFormatter TIME_FMT
            = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ── main ──────────────────────────────────────────────
    public static void main(String[] args) throws IOException {

        int port = (args.length > 0) ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       Multithreaded Chat Server          ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("  Port      : " + port);
        System.out.println("  Max users : " + MAX_CLIENTS);
        System.out.println("  Started   : " + timestamp());
        System.out.println("  Waiting for clients …\n");

        // thread pool – one thread per connected client
        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();  // blocks until a client connects
                pool.execute(new ClientHandler(clientSocket));
            }
        }
    }

    // ── broadcast to everyone ─────────────────────────────

    /**
     * Sends a message to every connected client.
     *
     * @param message    text to broadcast
     * @param senderName name of sender (excluded from receiving their own echo)
     *                   pass null to send to absolutely everyone (e.g. join/leave notices)
     */
    static void broadcast(String message, String senderName) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (senderName == null || !entry.getKey().equals(senderName)) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    // ── private message ───────────────────────────────────

    /**
     * Sends a private message to one specific user.
     *
     * @param targetName  recipient's username
     * @param message     text to send
     * @return true if user was found and message delivered
     */
    static boolean privateMessage(String targetName, String message) {
        ClientHandler target = clients.get(targetName);
        if (target != null) {
            target.sendMessage(message);
            return true;
        }
        return false;
    }

    // ── register / deregister ─────────────────────────────

    static boolean registerClient(String username, ClientHandler handler) {
        if (clients.containsKey(username)) return false;   // name taken
        clients.put(username, handler);
        return true;
    }

    static void deregisterClient(String username) {
        clients.remove(username);
    }

    // ── helpers ───────────────────────────────────────────

    static Set<String> getOnlineUsers() {
        return clients.keySet();
    }

    static int getOnlineCount() {
        return clients.size();
    }

    static String timestamp() {
        return LocalTime.now().format(TIME_FMT);
    }
}


/**
 * ============================================================
 *  ClientHandler  –  runs in its own thread, one per client
 * ============================================================
 */
class ClientHandler implements Runnable {

    private final Socket       socket;
    private BufferedReader     reader;
    private PrintWriter        writer;
    private String             username;

    // ── constructor ───────────────────────────────────────
    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // ── thread entry point ────────────────────────────────
    @Override
    public void run() {
        try {
            // set up I/O streams
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()), true); // auto-flush

            // ── Step 1: get & validate username ──────────
            sendMessage("╔══════════════════════════════════════╗");
            sendMessage("║     Welcome to Java Chat Server!     ║");
            sendMessage("╚══════════════════════════════════════╝");
            sendMessage("Enter your username: ");

            while (true) {
                username = reader.readLine();
                if (username == null || username.trim().isEmpty()) {
                    sendMessage("[ERROR] Username cannot be empty. Try again: ");
                    continue;
                }
                username = username.trim();
                if (ChatServer.registerClient(username, this)) break;
                sendMessage("[ERROR] \"" + username + "\" is already taken. Try another: ");
            }

            // ── Step 2: welcome the user ──────────────────
            sendMessage("\n✔  Logged in as: " + username);
            sendMessage("─────────────────────────────────────────");
            sendMessage("Commands:");
            sendMessage("  @username <msg>  →  private message");
            sendMessage("  /users           →  list online users");
            sendMessage("  /quit            →  disconnect");
            sendMessage("─────────────────────────────────────────\n");

            // notify everyone else
            String joinMsg = "[" + ChatServer.timestamp() + "] *** "
                    + username + " joined the chat ("
                    + ChatServer.getOnlineCount() + " online) ***";
            ChatServer.broadcast(joinMsg, username);
            System.out.println(joinMsg);

            // ── Step 3: main message loop ─────────────────
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // ── /quit ─────────────────────────────────
                if (line.equalsIgnoreCase("/quit")) {
                    sendMessage("Goodbye, " + username + "!");
                    break;
                }

                // ── /users ────────────────────────────────
                if (line.equalsIgnoreCase("/users")) {
                    StringBuilder sb = new StringBuilder(
                            "── Online users (" + ChatServer.getOnlineCount() + ") ──\n");
                    for (String u : ChatServer.getOnlineUsers()) {
                        sb.append("  • ").append(u);
                        if (u.equals(username)) sb.append(" (you)");
                        sb.append("\n");
                    }
                    sendMessage(sb.toString());
                    continue;
                }

                // ── @username private message ─────────────
                if (line.startsWith("@")) {
                    int space = line.indexOf(' ');
                    if (space == -1) {
                        sendMessage("[ERROR] Usage: @username <message>");
                        continue;
                    }
                    String target  = line.substring(1, space);
                    String privMsg = line.substring(space + 1).trim();
                    if (target.equals(username)) {
                        sendMessage("[ERROR] You cannot message yourself.");
                        continue;
                    }
                    String formatted = "[" + ChatServer.timestamp() + "] "
                            + "[PM from " + username + "] " + privMsg;
                    boolean sent = ChatServer.privateMessage(target, formatted);
                    if (sent) {
                        sendMessage("[" + ChatServer.timestamp() + "] "
                                + "[PM to " + target + "] " + privMsg);
                    } else {
                        sendMessage("[ERROR] User \"" + target + "\" is not online.");
                    }
                    continue;
                }

                // ── public broadcast ──────────────────────
                String formatted = "[" + ChatServer.timestamp() + "] "
                        + username + ": " + line;
                ChatServer.broadcast(formatted, username);   // others
                sendMessage(formatted);                      // echo to sender
                System.out.println(formatted);              // server log
            }

        } catch (IOException e) {
            // client disconnected abruptly
        } finally {
            disconnect();
        }
    }

    // ── send one line to this client ──────────────────────
    void sendMessage(String message) {
        if (writer != null) writer.println(message);
    }

    // ── clean up on disconnect ────────────────────────────
    private void disconnect() {
        if (username != null) {
            ChatServer.deregisterClient(username);
            String leaveMsg = "[" + ChatServer.timestamp() + "] *** "
                    + username + " left the chat ("
                    + ChatServer.getOnlineCount() + " online) ***";
            ChatServer.broadcast(leaveMsg, null);
            System.out.println(leaveMsg);
        }
        try { socket.close(); } catch (IOException ignored) {}
    }
}
