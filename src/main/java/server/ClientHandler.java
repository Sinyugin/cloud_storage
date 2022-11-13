package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private boolean running;
    private InputStream in;
    private OutputStream out;
    private byte[] buf;
    private Socket socket;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        running = true;
        buf = new byte[8192];
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            while (running) {
                int read = in.read(buf); // ожидает сообщения от клиента
                String message = new String(buf, 0, read)
                        .trim();
                if (message.equals("quit")) {
                    out.write("Клиент отключился\n".getBytes(StandardCharsets.UTF_8));
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
