import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * ============================================================
 *  ChatClient.java  –  Chat Client
 * ------------------------------------------------------------
 *  Connects to the ChatServer and runs TWO threads:
 *
 *    Thread 1 – ReadThread  : continuously reads messages
 *                             arriving from the server and
 *                             prints them to the console.
 *
 *    Thread 2 – main thread : reads the user's keyboard input
 *                             and sends it to the server.
 *
 *  This two-thread design means incoming messages appear
 *  instantly even while the user is typing.
 *
 *  Run:
 *    java ChatClient                        (localhost:12345)
 *    java ChatClient 192.168.1.10           (custom host)
 *    java ChatClient 192.168.1.10 9000      (custom host+port)
 * ============================================================
 */
public class ChatClient {

    // ── defaults ──────────────────────────────────────────
    private static final String DEFAULT_HOST = "localhost";
    private static final int    DEFAULT_PORT = 12345;

    // ── shared flag: set to false to stop both threads ────
    private volatile boolean running = true;

    // ── I/O streams ───────────────────────────────────────
    private PrintWriter   writer;
    private BufferedReader reader;
    private Socket        socket;

    // ── main ──────────────────────────────────────────────
    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : DEFAULT_HOST;
        int    port = (args.length > 1) ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        new ChatClient().start(host, port);
    }

    // ── connect and start ─────────────────────────────────
    private void start(String host, int port) {

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║         Java Chat Client             ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.printf ("  Connecting to %s:%d …%n", host, port);

        try {
            socket = new Socket(host, port);
            System.out.println("  ✔  Connected!\n");

            // set up streams
            writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()), true);
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // ── start the background reader thread ────────
            Thread readThread = new Thread(new ReadThread());
            readThread.setDaemon(true);   // dies automatically when main exits
            readThread.start();

            // ── main thread: keyboard → server ────────────
            Scanner scanner = new Scanner(System.in);
            while (running && scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!running) break;

                writer.println(line);        // send to server

                // local /quit → stop the client
                if (line.trim().equalsIgnoreCase("/quit")) {
                    running = false;
                }
            }
            scanner.close();

        } catch (ConnectException e) {
            System.err.println("\n[ERROR] Cannot connect to " + host + ":" + port);
            System.err.println("  Make sure ChatServer is running first.");
        } catch (UnknownHostException e) {
            System.err.println("\n[ERROR] Unknown host: " + host);
        } catch (IOException e) {
            if (running) {   // only print if it wasn't a clean shutdown
                System.err.println("\n[ERROR] Connection lost: " + e.getMessage());
            }
        } finally {
            shutdown();
        }

        System.out.println("\n  Connection closed. Goodbye!");
    }

    // ── clean shutdown ────────────────────────────────────
    private void shutdown() {
        running = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    // ══════════════════════════════════════════════════════
    //  ReadThread  –  background thread that prints incoming
    //                 server messages to stdout
    // ══════════════════════════════════════════════════════
    private class ReadThread implements Runnable {
        @Override
        public void run() {
            try {
                String serverMessage;
                while (running && (serverMessage = reader.readLine()) != null) {
                    // print on its own line, keep clean even while user types
                    System.out.println(serverMessage);
                }
            } catch (IOException e) {
                if (running) {
                    System.out.println("\n[Server disconnected]");
                }
            } finally {
                running = false;   // signal the main thread to stop too
            }
        }
    }
}
