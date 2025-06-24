package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;
    private Set<ClientHandler> clientHandlers;

    public ClientHandler(Socket socket, Set<ClientHandler> clientHandlers) {
        this.socket = socket;
        this.clientHandlers = clientHandlers;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeAll();
        }
    }

    @Override
    public void run() {
        try {
            writer.write("Enter your username:\n");
            writer.flush();
            username = reader.readLine();
            broadcast("🟢 " + username + " joined the chat");

            String msg;
            while ((msg = reader.readLine()) != null) {
                if (msg.equalsIgnoreCase("/exit")) break;
                broadcast("[" + username + "]: " + msg);
            }
        } catch (IOException e) {
            System.out.println("❌ Connection dropped: " + username);
        } finally {
            clientHandlers.remove(this);
            broadcast("🔴 " + username + " left the chat");
            closeAll();
        }
    }

    private void broadcast(String message) {
        for (ClientHandler handler : clientHandlers) {
            try {
                handler.writer.write(message + "\n");
                handler.writer.flush();
            } catch (IOException e) {
                handler.closeAll();
            }
        }
    }

    private void closeAll() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}

