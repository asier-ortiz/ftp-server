package com.ortiz.server;

import com.ortiz.util.DialogManagerUtil;
import com.ortiz.model.DirectoryStructure;
import com.ortiz.util.MethodsUtil;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame implements Runnable {

    private final int SERVERPORT = 4444;
    private ServerSocket server = null;
    private boolean isRunning = false;
    public static DirectoryStructure structure;
    private String serverDirectoryPath;
    private boolean isValidDirectory = false;
    private JPanel serverWindow;
    private JButton startShutdownServerButton;

    public JPanel getServerWindow() {
        startShutdownServerButton.setText("");
        startShutdownServerButton.setToolTipText("Start server");
        if (MethodsUtil.isWindows()) {
            startShutdownServerButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".\\assets\\serverOff.png"), 100, 100));
        } else {
            startShutdownServerButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".//assets//serverOff.png"), 100, 100));
        }
        return serverWindow;
    }

    public Server() {
        super("FTP SERVER");
        startShutdownServerButton.addActionListener(e -> {
            if (!isRunning) {
                selectServerDirectory();
                if (isValidDirectory) {
                    new Thread(this).start();
                    isRunning = true;
                    startShutdownServerButton.setToolTipText("Shutdown server");
                    if (MethodsUtil.isWindows()) {
                        startShutdownServerButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".\\assets\\serverOn.png"), 100, 100));
                    } else {
                        startShutdownServerButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".//assets//serverOn.png"), 100, 100));
                    }
                }
            } else {
                Object[] options = {"Yes", "No"};
                int reply = DialogManagerUtil.showOptionDialog(
                        options,
                        "Do you want to shutdown the server?",
                        "Attention!",
                        JOptionPane.YES_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        1);
                if (reply == JOptionPane.YES_OPTION) {
                    shutDownServer();
                }
            }
        });
    }

    private synchronized void startSever() {
        try {
            this.server = new ServerSocket(this.SERVERPORT);
            DialogManagerUtil.showInfoDialog("Server running on port " + SERVERPORT);
        } catch (IOException e) {
            DialogManagerUtil.showErrorDialog("Cannot open port " + SERVERPORT);
        }
    }

    private synchronized void shutDownServer() {
        this.isRunning = false;
        try {
            this.server.close();
        } catch (IOException e) {
            DialogManagerUtil.showErrorDialog("Error closing server");
        }
    }

    private void selectServerDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select the root directory of the FTP server");
        int returnVal = fileChooser.showDialog(fileChooser, "Select");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file.exists()) {
                serverDirectoryPath = file.getAbsolutePath();
                isValidDirectory = true;
            } else {
                DialogManagerUtil.showErrorDialog("Error: The directory does not exist...");
            }
        }
    }

    @Override
    public void run() {
        startSever();
        while (isRunning) {
            Socket clientSocket;
            try {
                clientSocket = this.server.accept();
                structure = new DirectoryStructure(serverDirectoryPath);
                new ServerThread(clientSocket, structure).start();
            } catch (IOException e) {
                if (!isRunning) {
                    System.exit(1);
                }
                DialogManagerUtil.showErrorDialog("Error accepting client connection");
            }
        }
    }

    public static void main(String[] args) {
        MethodsUtil.setApplicationLookAndFeel();
        Server server = new Server();
        server.setContentPane(server.getServerWindow());
        server.pack();
        server.setBounds(0, 0, 250, 200);
        server.setResizable(false);
        server.setLocationRelativeTo(null);
        server.setVisible(true);
    }
}
