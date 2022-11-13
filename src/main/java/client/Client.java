package client;

import java.io.*;
import java.net.Socket;

public class Client {
    private InputStream in;
    private OutputStream out;
    private Socket socket;

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            System.out.println("Клиент авторизовался");
        }).start();
    }
}
