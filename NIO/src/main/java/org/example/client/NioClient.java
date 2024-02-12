package org.example.client;

import org.example.server.Command;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8189;
    static SocketChannel socketChannel;

    public static void main(String[] args) {
        try {
            InetSocketAddress serverAddress = new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT);
            socketChannel = SocketChannel.open(serverAddress);

            sendRequest(socketChannel, Command.LS.getCommand());

            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void fillFileView() throws IOException {
        sendRequest(socketChannel, "LIST_FILES");
        String response = receiveResponse(socketChannel);
        System.out.println("Files on server: " + response);
    }
    private static void sendRequest(SocketChannel socketChannel, String request) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());
        socketChannel.write(buffer);
        buffer.clear();
    }

    private static String receiveResponse(SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        socketChannel.read(buffer);
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes);
    }

}