package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8189);
        System.out.println("Сервер запущен...");
        while (true) {
            try {
                System.out.println("Ожидаю подключение клиента...");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился...");
                Server server = new Server();
                new Thread(new ClientHandler(socket, server)).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
