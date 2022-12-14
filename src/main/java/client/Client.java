package client;

import java.io.*;
import java.net.Socket;

public class Client {
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private ClientController controller;

    public Client() {
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    System.out.println("Клиент авторизовался");
                    out.writeUTF(message);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }
}
