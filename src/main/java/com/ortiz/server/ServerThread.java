package com.ortiz.server;

import com.ortiz.model.DirectoryStructure;
import com.ortiz.util.MethodsUtil;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Properties;

public class ServerThread extends Thread {

    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final DirectoryStructure structure;

    public ServerThread(Socket socket, DirectoryStructure structure) throws IOException {
        this.socket = socket;
        this.structure = structure;
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            out.writeObject(structure);
            while (true) {
                Request request = (Request) in.readObject();
                switch (request.getRequestType()) {
                    case REQUESTFILE -> sendFile(request);
                    case SENDFILE -> saveFile(request);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendFile(Request request) {
        File file = new File(request.getFileName());
        try (BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file))) {
            long bytes = file.length();
            byte[] buff = new byte[(int) bytes];
            int i, j = 0;
            while ((i = bin.read()) != -1) {
                buff[j] = (byte) i;
                j++;
            }
            Request callBackRequest = new Request(buff);
            out.writeObject(callBackRequest);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(Request request) {
        File directory = new File(request.getDirectory());
        File file = new File(directory, request.getFileName());
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            bos.write(request.getData());
            DirectoryStructure structure = new DirectoryStructure(request.getDirectory());
            out.writeObject(structure);
            out.flush();
           // sendMail(request.getFileName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMail(String fileName) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", true);
            props.put("mail.smtp.starttls.enable", true);
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", 587);
            final Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("asier.ortiz@ikasle.egibide.org", "");
                }
            });
            Message message = new MimeMessage(session);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("admin@test.com"));
            message.setSubject("File upload confirmation");
            String messageText = String.format("""
                    File %s was uploaded successfully to the server by client %s at %s.
                    """, fileName, socket.getInetAddress(), MethodsUtil.formatDate(LocalDate.now()));
            message.setText(messageText);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
