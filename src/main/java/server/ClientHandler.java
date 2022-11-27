package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class ClientHandler implements Runnable {
    private boolean running;
    private DataInputStream in;
    private OutputStream out;
    private byte[] buf;
    private Socket socket;
    private Server server;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;

        running = true;
        buf = new byte[8192];
        in = new DataInputStream(socket.getInputStream());
        out = socket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            while (running) {
                String message = in.readUTF(); // ожидает сообщения от клиента
                if (message.equals("quit")) {
                    out.write("Клиент отключился\n".getBytes(StandardCharsets.UTF_8));
                    System.out.println("Клиент отключился.");
                    close();
                    break;
                }
                System.out.println("echo ->" + message);
                out.write((message + "\n").getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }
}
