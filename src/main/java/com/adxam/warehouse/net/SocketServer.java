package com.adxam.warehouse.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketServer implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(SocketServer.class.getName());

    private final int port;
    private final AtomicInteger clientCounter = new AtomicInteger();
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public SocketServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        if (running) throw new IllegalStateException("Server already running");
        serverSocket = new ServerSocket(port);
        running = true;
        LOG.info(() -> "Server listening on port " + port);
        acceptLoop();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                Thread t = new Thread(new ClientHandler(client),
                        "client-" + clientCounter.incrementAndGet());
                t.setDaemon(false);
                t.start();
            } catch (IOException e) {
                if (running) {
                    LOG.log(Level.WARNING, e, () -> "Accept failed");
                }
            }
        }
    }

    @Override
    public void close() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try { serverSocket.close(); } catch (IOException ignored) {}
        }
        LOG.info("Server stopped");
    }

    public int port() { return port; }
    public boolean isRunning() { return running; }
}
