package com.ortiz.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;

// Prueba de conexión a un servidor FTP externo

public class FTPConnectionUtil {

    public static void main(String[] args) {
        FTPClient client = new FTPClient();
        try {
            client.connect("83.213.104.51");
            boolean login = client.login("Asier", "12345Abcde");
            if (login) {
                FTPFile[] ftpFiles = client.listFiles();
                File[] files = new File[ftpFiles.length];
                for (int i = 0; i < ftpFiles.length; i++) {
                    files[i] = new File(ftpFiles[i].getName());
                }
                for (File f : files) {
                    System.out.println(f);
                }
                System.out.println("Connection established...");
                boolean logout = client.logout();
                if (logout) {
                    System.out.println("Connection close...");
                }
            } else {
                System.out.println("Connection fail...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}