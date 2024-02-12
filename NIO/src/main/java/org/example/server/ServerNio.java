package org.example.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerNio {
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private ByteBuffer buf;
    private Path currentDir;

    public ServerNio(int port) throws IOException {
        currentDir = Paths.get("./");
        buf = ByteBuffer.allocate(1024);
        serverChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (serverChannel.isOpen()) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            try {
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        handleAccept();
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    iterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder msg = new StringBuilder();
        while (true) {
            int read = channel.read(buf);
            if (read == -1) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            buf.flip();
            while (buf.hasRemaining()) {
                msg.append((char) buf.get());
            }
            buf.clear();
            processMessage(channel, msg.toString().trim());
        }
    }

    public void processMessage(SocketChannel channel, String msg) throws IOException {
        String[] tokens = msg.split(" +");
        Command type = null;
        try {
            type = Command.byCommand(tokens[0]);
            switch (type) {
                case LS:
                    sendString(channel, getFileList());
                    break;
                case CAT:
                    processCatCommand(channel, tokens);
                    break;
                case CD:
                    processCdCommand(channel, tokens);
                    break;
                case QUIT:
                    processQuitCommand(channel, tokens);
                    break;
            }
        } catch (RuntimeException e) {
            String response = "Command " + tokens[0] + " is not exists!\n\r";
            sendString(channel, response);
        }
    }

    private void processCdCommand(SocketChannel channel, String[] tokens) throws IOException {
        if (tokens == null || tokens.length != 2) {
            sendString(channel, "Command cat should have 2 args");
        } else {
            String dir = tokens[1];
            if (Files.isDirectory(currentDir.resolve(dir))) {
                currentDir = currentDir.resolve(dir);
                channel.write(ByteBuffer.wrap(" -> ".getBytes(StandardCharsets.UTF_8)));
            } else sendString(channel, "You cannot use cd command to FILES\n\r");
        }
    }

    private void processCatCommand(SocketChannel channel, String[] tokens) throws IOException {
        if (tokens == null || tokens.length != 2) {
            sendString(channel, "Command cat should have 2 args");
        } else {
            String fileName = tokens[1];
            Path file = currentDir.resolve(fileName);
            if (!Files.isDirectory(file)) {
                String content = new String(Files.readAllBytes(file)) + "\n\r";
                sendString(channel, content);
            } else {
                sendString(channel, "You cannot use cat command to DIR\n\r");
            }
        }
    }

    private void processQuitCommand(SocketChannel channel, String[] tokens) throws IOException{
        System.out.println("Клиент отключился");
        channel.close();
    }

    private String getFileList() throws IOException {
        return Files.list(currentDir)
                .map(p -> p.getFileName().toString() + " " + getFileSuffix(p))
                .collect(Collectors.joining("\n\r")) + "\n\r";
    }

    private String getFileSuffix(Path path) {
        if (Files.isDirectory(path)) {
            return "[DIR]";
        } else {
            return "[FILE] " + path.toFile().length() + " bytes";
        }
    }

    private void sendString(SocketChannel channel, String msg) throws IOException {
        channel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
        channel.write(ByteBuffer.wrap(" -> ".getBytes(StandardCharsets.UTF_8)));
    }

    private void handleAccept() throws IOException {
        System.out.println("Client accepted...");
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, "Hello world!");
        socketChannel.write(ByteBuffer.wrap((
                "Welcom Sergey terminal\n\r"
        ).getBytes(StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws IOException {
        new ServerNio(8189);
    }
}
