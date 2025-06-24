package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class ChatGUI {
    private static JTextArea messageArea;
    private static ChatClient client;
    private static JLabel statusLabel;
    private static final String LOG_FILE = "chat_history.txt";

    public static void main(String[] args) {
        String username = JOptionPane.showInputDialog("Enter your username:");
        if (username != null && !username.trim().isEmpty()) {
            launch(username.trim());
        }
    }

    public static void launch(String username) {
        JFrame frame = new JFrame("💬 Chat - " + username);
        messageArea = new JTextArea(20, 45);
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(messageArea);

        loadChatHistory();

        JTextField inputField = new JTextField(28);
        JButton sendButton = new JButton("Send");
        JButton emojiButton = new JButton("😊");
        JButton clearButton = new JButton("🧹");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);

        JPanel inputPanel = new JPanel();
        inputPanel.add(inputField);
        inputPanel.add(sendButton);
        inputPanel.add(emojiButton);
        inputPanel.add(clearButton);

        sendButton.addActionListener(e -> sendMessage(inputField));
        inputField.addActionListener(e -> sendMessage(inputField));

        emojiButton.addActionListener(e -> {
            inputField.setText(inputField.getText() + " 😊");
            inputField.requestFocus();
        });

        clearButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Clear local chat history?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) clearChatHistory();
        });

        try {
            client = new ChatClient(username);
        } catch (Exception e) {
            appendMessage("❌ Could not connect to server");
            return;
        }

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.add(statusLabel, BorderLayout.NORTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void sendMessage(JTextField inputField) {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String formatted = "[You @ " + time + "]: " + msg;
            try {
                client.sendMessage(msg);
                appendMessage(formatted);
                saveMessage(formatted);
                inputField.setText("");
            } catch (Exception ex) {
                appendMessage("❌ Message failed");
            }
        }
    }

    public static void appendMessage(String msg) {
        messageArea.append(msg + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
        saveMessage(msg);
    }

    private static void saveMessage(String msg) {
        try {
            statusLabel.setText("📤 Saving...");
            String encrypted = Base64.getEncoder().encodeToString((msg + "\n").getBytes());
            Files.write(Paths.get(LOG_FILE), (encrypted + System.lineSeparator()).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            statusLabel.setText("✅ Saved");
        } catch (IOException e) {
            System.err.println("❌ Save error: " + e.getMessage());
        }
    }

    private static void loadChatHistory() {
        try {
            File file = new File(LOG_FILE);
            if (file.exists()) {
                for (String line : Files.readAllLines(file.toPath())) {
                    if (!line.trim().isEmpty()) {
                        String decoded = new String(Base64.getDecoder().decode(line));
                        messageArea.append(decoded);
                    }
                }
                messageArea.setCaretPosition(messageArea.getDocument().getLength());
            }
        } catch (IOException e) {
            System.err.println("⚠️ History load failed: " + e.getMessage());
        }
    }

    private static void clearChatHistory() {
        try {
            Files.deleteIfExists(Paths.get(LOG_FILE));
            messageArea.setText("");
            statusLabel.setText("🧹 Chat history cleared");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Couldn't clear chat.");
        }
    }
}
