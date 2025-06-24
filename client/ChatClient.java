package client;

import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ChatClient(String username) throws IOException {
        this.socket = new Socket("192.168.0.197", 12345); // LAN IP
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    ChatGUI.appendMessage(line);
                }
            } catch (IOException e) {
                ChatGUI.appendMessage("🔌 Connection lost.");
            }
        }).start();

        writer.write(username + "\n");
        writer.flush();
    }

    public void sendMessage(String msg) throws IOException {
        writer.write(msg + "\n");
        writer.flush();
    }
}


