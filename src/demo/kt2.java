package demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

interface ChatService extends Remote {
    boolean setPassword(String username, String password) throws RemoteException;
    boolean login(String username, String password) throws RemoteException;
    boolean logout(String username) throws RemoteException;
    void sendMessage(String username, String message) throws RemoteException;
    void sendImage(String username, byte[] imageData) throws RemoteException;
    void sendFile(String username, byte[] fileData, String fileName) throws RemoteException;
}

class ChatServiceImpl extends UnicastRemoteObject implements ChatService {
    private static final long serialVersionUID = 1L;

    public ChatServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean setPassword(String username, String password) throws RemoteException {
        // Implementation for setting the password
        return true;
    }

    @Override
    public boolean login(String username, String password) throws RemoteException {
        // Implementation for the login process
        return true;
    }

    @Override
    public boolean logout(String username) throws RemoteException {
        // Implementation for the logout process
        return true;
    }

    @Override
    public void sendMessage(String username, String message) throws RemoteException {
        System.out.println(username + ": " + message);
    }

    @Override
    public void sendImage(String username, byte[] imageData) throws RemoteException {
        ImageIcon imageIcon = new ImageIcon(imageData);
        JOptionPane.showMessageDialog(null, new JLabel(imageIcon));
    }

    @Override
    public void sendFile(String username, byte[] fileData, String fileName) throws RemoteException {
        String directoryPath = "downloads";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                System.out.println("Error creating directory.");
                return;
            }
        }

        String filePath = directoryPath + File.separator + fileName;
        try {
            Path file = Path.of(filePath);
            Files.write(file, fileData, StandardOpenOption.CREATE);
            System.out.println("File downloaded successfully.");
            JOptionPane.showMessageDialog(null, "File downloaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error downloading file.");
            JOptionPane.showMessageDialog(null, "Error downloading file.");
        }
    }
}//vay la xong roi nha

public class kt2 extends JFrame {
    private JButton sendButton;
    private JButton sendImageButton;
    private JButton sendFileButton;

    private ChatService chatService;
    private String username;

    public kt2(ChatService chatService, String username) {
        this.chatService = chatService;
        this.username = username;

        setTitle("Chat Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        sendButton = new JButton("Send Message");
        sendImageButton = new JButton("Send Image");
        sendFileButton = new JButton("Send File");

        sendButton.addActionListener(e -> {
            String message = JOptionPane.showInputDialog(kt2.this, "Enter message:");
            if (message != null && !message.isEmpty()) {
                try {
                    chatService.sendMessage(username, message);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        sendImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(kt2.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    byte[] imageData = Files.readAllBytes(selectedFile.toPath());
                    chatService.sendImage(username, imageData);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(kt2.this, "Error sending image.");
                }
            }
        });

        sendFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(kt2.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                    chatService.sendFile(username, fileData, selectedFile.getName());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(kt2.this, "Error sending file.");
                }
            }
        });

        add(sendButton);
        add(sendImageButton);
        add(sendFileButton);

        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            ChatServiceImpl chatService = new ChatServiceImpl();
            registry.rebind("ChatService", chatService);

            String username = JOptionPane.showInputDialog("Enter username:");
            String password = JOptionPane.showInputDialog("Enter password:");

            if (chatService.login(username, password)) {
                kt2 chatApplication = new kt2(chatService, username);

                Timer timer = new Timer(60000, e -> {
                    try {
                        if (chatService.logout(username)) {
                            System.out.println("Logged out due toinactivity.");
                            System.exit(0);
                        }
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                });
                timer.start();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password. Exiting...");
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}