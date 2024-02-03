package com.ortiz.client;

import com.ortiz.model.DirectoryStructure;
import com.ortiz.server.Request;
import com.ortiz.util.DialogManagerUtil;
import com.ortiz.util.MethodsUtil;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame implements Runnable {

    private JPanel clientWindow;
    private JList<DirectoryStructure> structuresList;
    private JButton uploadButton;
    private JButton downloadButton;
    private JButton exitButton;
    private JLabel pathLabel;
    private JLabel numberOfFilesLabel;
    private JLabel selectedFileLabel;
    private JButton backButton;
    private JButton homeButton;
    private JPanel downloadsPanel;
    private JLabel donwloadFileNameLabel;
    private JProgressBar donwloadProgressBar;
    private JLabel downloadProgressLabel;
    private JPanel uploadsPanel;
    private JLabel uploadFileNameLabel;
    private JProgressBar uploadProgressBar;
    private JLabel uploadProgressLabel;
    private static Socket socket;
    private static DirectoryStructure structure = null;
    private static ObjectInputStream in;
    private static ObjectOutputStream out;
    private static DirectoryStructure rootStructure;
    private static String selectedDirectory = "";
    private static String selectedFile = "";
    private static String filePath = "";
    private String rootPathName;

    public JPanel getClientWindow() {
        setButtonsSettings();
        downloadsPanel.setVisible(false);
        uploadsPanel.setVisible(false);
        return clientWindow;
    }

    public Client(Socket socket) {
        super("FTP CLIENT");
        Client.socket = socket;
        try {
            out = new ObjectOutputStream(Client.socket.getOutputStream());
            in = new ObjectInputStream(Client.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        homeButton.addActionListener(e -> {
            rootStructure = new DirectoryStructure(rootPathName);
            DirectoryStructure[] structures = rootStructure.getStructureFiles();
            selectedDirectory = rootStructure.getPath();
            fillUpStructure(structures, rootStructure.getFileCount(), structuresList);
            pathLabel.setText(selectedDirectory);
            numberOfFilesLabel.setText("Number of files in current directory : " + rootStructure.getFileCount());
        });

        backButton.addActionListener(e -> {
            char separator;
            if (MethodsUtil.isWindows()) {
                separator = '\\';
            } else {
                separator = '/';
            }
            String newpath = rootStructure.getPath().substring(0, rootStructure.getPath().lastIndexOf(separator));
            if (newpath.equalsIgnoreCase(rootPathName) || newpath.length() > rootPathName.length()) {
                rootStructure = new DirectoryStructure(newpath);
                DirectoryStructure[] structures = rootStructure.getStructureFiles();
                selectedDirectory = rootStructure.getPath();
                fillUpStructure(structures, rootStructure.getFileCount(), structuresList);
                pathLabel.setText(selectedDirectory);
                numberOfFilesLabel.setText("Number of files in current directory : " + rootStructure.getFileCount());
            }
        });

        structuresList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                selectedFile = "";
                filePath = "";
                structure = structuresList.getSelectedValue();
                if (structure.isDir()) {
                    rootStructure = new DirectoryStructure(structure.getName(), structure.getPath(), structure.isDir(), structure.getFileCount());
                    DirectoryStructure[] structures = rootStructure.getStructureFiles();
                    selectedDirectory = rootStructure.getPath();
                    fillUpStructure(structures, rootStructure.getFileCount(), structuresList);
                    pathLabel.setText(selectedDirectory);
                    numberOfFilesLabel.setText("Number of files in current directory : " + rootStructure.getFileCount());
                } else {
                    selectedFile = structure.getName();
                    filePath = structure.getPath();
                    selectedFileLabel.setText("Selected file : " + selectedFile + " (" + getFileSize(new File(structure.getPath())) + ")");
                }
            }
        });

        exitButton.addActionListener(e -> {
            Object[] options = {"Yes", "No"};
            int reply = DialogManagerUtil.showOptionDialog(
                    options,
                    "Do you want to close this client?",
                    "Attention!",
                    JOptionPane.YES_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    1);
            if (reply == JOptionPane.YES_OPTION) {
                try {
                    Client.socket.close();
                    System.exit(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        downloadButton.addActionListener(e -> {
            if (filePath.equals("")) {
                DialogManagerUtil.showErrorDialog("Please select a file first");
                return;
            }
            Request request = new Request(filePath);
            DownloadTask downloadTask = new DownloadTask(request, in, out, selectedFile, downloadsPanel,
                    donwloadFileNameLabel, donwloadProgressBar, downloadProgressLabel, downloadButton);
            downloadTask.execute();
        });

        uploadButton.addActionListener(e -> {
            JFileChooser f = new JFileChooser();
            f.setFileSelectionMode(JFileChooser.FILES_ONLY);
            f.setDialogTitle("Choose a file to upload");
            int returnVal = f.showDialog(f, "Upload");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = f.getSelectedFile();
                UploadTask uploadTask = new UploadTask(file, in, out, rootPathName, structuresList, structure,
                        numberOfFilesLabel, uploadsPanel, uploadFileNameLabel, uploadProgressBar, uploadProgressLabel,
                        uploadButton, pathLabel);
                uploadTask.execute();
            }
        });
    }

    public static void fillUpStructure(DirectoryStructure[] structures, int fileCount, JList<DirectoryStructure> structuresList) {
        if (fileCount == 0) return;
        DefaultListModel<DirectoryStructure> model = new DefaultListModel<>();
        structuresList.setForeground(Color.blue);
        Font font = new Font("monospaced", Font.BOLD, 12);
        structuresList.setFont(font);
        structuresList.removeAll();
        for (DirectoryStructure structure : structures) {
            model.addElement(structure);
        }
        try {
            structuresList.setModel(model);
        } catch (NullPointerException ignored) {
        }
    }

    private String getFileSize(File file) {
        int fileSize = (int) file.length();
        if (fileSize < 1024) {
            return String.format("%.2f", ((float) file.length())) + " Bytes";
        } else if (fileSize < Math.pow(1024, 2)) {
            return String.format("%.2f", (((float) file.length() / 1024)) * 1.024) + " KB";
        } else if (fileSize < Math.pow(1024, 3)) {
            return String.format("%.2f", (((float) file.length() / Math.pow(1024, 2))) * 1.04858) + " MB";
        } else {
            return String.format("%.2f", (((float) file.length() / Math.pow(1024, 3))) * 1.07374) + " GB";
        }
    }

    private void setButtonsSettings() {
        if (MethodsUtil.isWindows()) {
            homeButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".\\assets\\home.png"), 25, 25));
            backButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".\\assets\\back.png"), 25, 25));
            uploadButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".\\assets\\upload.png"), 25, 25));
            downloadButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".\\assets\\download.png"), 25, 25));
            exitButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".\\assets\\exit.png"), 25, 25));
        } else {
            homeButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".//assets//home.png"), 25, 25));
            backButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".//assets//back.png"), 25, 25));
            uploadButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".//assets//upload.png"), 25, 25));
            downloadButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".//assets//download.png"), 25, 25));
            exitButton.setIcon(MethodsUtil.resizeImageIcon(new ImageIcon(".//assets//exit.png"), 25, 25));
        }
        homeButton.setText("");
        backButton.setText("");
        uploadButton.setText("");
        downloadButton.setText("");
        exitButton.setText("");
    }

    @Override
    public void run() {
        try {
            rootStructure = (DirectoryStructure) in.readObject();
            DirectoryStructure[] structures = rootStructure.getStructures();
            selectedDirectory = rootStructure.getPath();
            fillUpStructure(structures, rootStructure.getFileCount(), structuresList);
            pathLabel.setText(selectedDirectory);
            numberOfFilesLabel.setText("Number of files in current directory : " + rootStructure.getFileCount());
            rootPathName = rootStructure.getPath();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        MethodsUtil.setApplicationLookAndFeel();
        try {
            int port = 4444;
            Socket socket = new Socket("localhost", port);
            Client client = new Client(socket);
            client.setContentPane(client.getClientWindow());
            client.pack();
            client.setBounds(0, 0, 550, 500);
            client.setVisible(true);
            new Thread(client).start();
        } catch (IOException e) {
            DialogManagerUtil.showErrorDialog("Server connection error...");
            System.exit(1);
        }
    }
}