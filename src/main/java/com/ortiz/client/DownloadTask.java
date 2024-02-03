package com.ortiz.client;

import com.ortiz.server.Request;
import com.ortiz.util.DialogManagerUtil;
import com.ortiz.util.MethodsUtil;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DownloadTask extends SwingWorker<Boolean, Integer> {

    private final Request request;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final String fileName;
    private final JPanel downloadsPanel;
    private final JLabel donwloadFileNameLabel;
    private final JProgressBar donwloadProgressBar;
    private final JLabel downloadProgressLabel;
    private final JButton downloadButton;

    public DownloadTask(Request request, ObjectInputStream in, ObjectOutputStream out, String fileName, JPanel downloadsPanel,
                        JLabel donwloadFileNameLabel, JProgressBar donwloadProgressBar,
                        JLabel downloadProgressLabel, JButton downloadButton) {
        this.request = request;
        this.in = in;
        this.out = out;
        this.fileName = fileName;
        this.downloadsPanel = downloadsPanel;
        this.donwloadFileNameLabel = donwloadFileNameLabel;
        this.donwloadProgressBar = donwloadProgressBar;
        this.downloadProgressLabel = downloadProgressLabel;
        this.downloadButton = downloadButton;
    }

    @Override
    protected Boolean doInBackground() throws IOException {
        File downloadsPath;
        if (MethodsUtil.isWindows()) {
            downloadsPath = new File(".\\downloads\\" + fileName);
        } else {
            downloadsPath = new File(".//downloads//" + fileName);
        }
        downloadsPath.createNewFile();
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(downloadsPath))) {
            downloadButton.setEnabled(false);
            downloadsPanel.setVisible(true);
            donwloadFileNameLabel.setText(fileName);
            out.writeObject(request);
            Request callback = (Request) in.readObject();
            byte[] buff = callback.getData();
            int i = 0;
            int total = buff.length;
            donwloadProgressBar.setMinimum(0);
            donwloadProgressBar.setMaximum(total);
            downloadProgressLabel.setText("0 / " + total);
            for (byte b : buff) {
                downloadProgressLabel.setText(i + " / " + total);
                publish(i);
                bos.write(b);
                i++;
            }
            return true;
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected void process(List<Integer> chunks) {
        donwloadProgressBar.setValue(chunks.get(0));
    }

    @Override
    protected void done() {
        Toolkit.getDefaultToolkit().beep();
        try {
            boolean success = get();
            if (success) {
                DialogManagerUtil.showInfoDialog("File download complete!");
            } else {
                DialogManagerUtil.showErrorDialog("File download error!");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            donwloadFileNameLabel.setText("");
            donwloadProgressBar.setValue(0);
            downloadProgressLabel.setText("0 / 0");
            downloadButton.setEnabled(true);
            downloadsPanel.setVisible(false);
        }
    }
}