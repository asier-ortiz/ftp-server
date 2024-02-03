package com.ortiz.client;

import com.ortiz.model.DirectoryStructure;
import com.ortiz.server.Request;
import com.ortiz.util.DialogManagerUtil;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.ortiz.client.Client.fillUpStructure;

public class UploadTask extends SwingWorker<DirectoryStructure, Integer> {

    private final File file;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final String rootPathName;
    private final JList<DirectoryStructure> structureJList;
    private DirectoryStructure structure;
    private final JLabel numberOfFilesLabel;
    private final JPanel uploadsPanel;
    private final JLabel uploadFileNameLabel;
    private final JProgressBar uploadProgressBar;
    private final JLabel uploadProgressLabel;
    private final JButton uploadButton;
    private final JLabel pathLabel;

    public UploadTask(File file, ObjectInputStream in, ObjectOutputStream out, String rootPathName,
                      JList<DirectoryStructure> structureJList, DirectoryStructure structure, JLabel numberOfFilesLabel,
                      JPanel uploadsPanel, JLabel uploadFileNameLabel, JProgressBar uploadProgressBar,
                      JLabel uploadProgressLabel, JButton uploadButton, JLabel pathLabel) {
        this.file = file;
        this.in = in;
        this.out = out;
        this.rootPathName = rootPathName;
        this.structureJList = structureJList;
        this.structure = structure;
        this.numberOfFilesLabel = numberOfFilesLabel;
        this.uploadsPanel = uploadsPanel;
        this.uploadFileNameLabel = uploadFileNameLabel;
        this.uploadProgressBar = uploadProgressBar;
        this.uploadProgressLabel = uploadProgressLabel;
        this.uploadButton = uploadButton;
        this.pathLabel = pathLabel;
    }

    @Override
    protected DirectoryStructure doInBackground() {
        String filePath = file.getAbsolutePath();
        String fileName = file.getName();
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            uploadButton.setEnabled(false);
            uploadsPanel.setVisible(true);
            uploadFileNameLabel.setText(fileName);
            long bytes = file.length();
            byte[] buff = new byte[(int) bytes];
            int i, j = 0;
            int total = buff.length;
            uploadProgressBar.setMinimum(0);
            uploadProgressBar.setMaximum(total);
            uploadProgressLabel.setText("0 / " + total);
            while ((i = bis.read()) != -1) {
                uploadProgressLabel.setText(j + " / " + total);
                publish(j);
                buff[j] = (byte) i;
                j++;
            }
            Request request = new Request(buff, fileName, rootPathName);
            out.writeObject(request);
            return (DirectoryStructure) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
        uploadProgressBar.setValue(chunks.get(0));
    }

    @Override
    protected void done() {
        Toolkit.getDefaultToolkit().beep();
        try {
            structure = this.get();
            if (structure != null) {
                DirectoryStructure[] lista = structure.getStructures();
                fillUpStructure(lista, structure.getFileCount(), structureJList);
                numberOfFilesLabel.setText("Number of files in current directory : " + lista.length);
                DialogManagerUtil.showInfoDialog("File upload complete!");
            } else {
                DialogManagerUtil.showErrorDialog("File upload error!");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            uploadFileNameLabel.setText("");
            uploadProgressBar.setValue(0);
            uploadProgressLabel.setText("0 / 0");
            uploadButton.setEnabled(true);
            uploadsPanel.setVisible(false);
            pathLabel.setText(rootPathName);
        }
    }
}