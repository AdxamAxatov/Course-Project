package com.adxam.warehouse.app;

import com.adxam.warehouse.net.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientMain {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 7070;

        try (Socket socket = new Socket(host, port);
             BufferedReader netIn = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter netOut = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {

            printHeader();
            if (!drainResponse(netIn)) return;

            while (true) {
                System.out.print("> ");
                System.out.flush();
                String line = userIn.readLine();
                if (line == null) break;
                if (line.isBlank()) continue;
                netOut.println(line);
                if (!drainResponse(netIn)) break;
                if (line.trim().equalsIgnoreCase("exit") || line.trim().equalsIgnoreCase("quit")) break;
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static boolean drainResponse(BufferedReader netIn) throws IOException {
        String line;
        while ((line = netIn.readLine()) != null) {
            if (Protocol.END_OF_RESPONSE.equals(line)) return true;
            System.out.println(line);
        }
        return false;
    }

    private static void printHeader() {
        System.out.println("========================================");
        System.out.println(" HOUSEHOLD APPLIANCES WAREHOUSE — CLIENT");
        System.out.println(" Version: 2.0");
        System.out.println(" Created: 2026");
        System.out.println(" Developer: Adkham Akhatov (Adkham_Akhatov@student.itpu.uz)");
        System.out.println("========================================");
    }
}
