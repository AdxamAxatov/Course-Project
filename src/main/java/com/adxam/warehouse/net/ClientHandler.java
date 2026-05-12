package com.adxam.warehouse.net;

import com.adxam.warehouse.controller.ControllerFactory;
import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.RequestImpl;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger LOG = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final Session session = new Session();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String remote = socket.getRemoteSocketAddress().toString();
        LOG.info(() -> "Client connected: " + remote);
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new java.io.OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            sendWelcome(out);

            String line;
            while ((line = in.readLine()) != null) {
                Request request = RequestImpl.parse(line, session);
                Response response = ControllerFactory.getInstance().execute(request);
                writeResponse(out, response);
                if (response.isOut()) break;
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, e, () -> "Client error: " + remote);
        } finally {
            LOG.info(() -> "Client disconnected: " + remote);
        }
    }

    private void sendWelcome(PrintWriter out) {
        out.println("Welcome to the Warehouse Server.");
        out.println("Type 'help' for commands, or 'login <user> <pass>' to begin.");
        out.println(Protocol.END_OF_RESPONSE);
    }

    private void writeResponse(PrintWriter out, Response response) {
        out.println(response.responseString());
        out.println(Protocol.END_OF_RESPONSE);
    }
}
