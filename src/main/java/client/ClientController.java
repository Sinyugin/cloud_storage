package client;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import server.AuthService;
import server.ClientHandler;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class ClientController implements Initializable {
    @FXML
    public TextField tfLogin;
    @FXML
    public TextField tfPass;
    @FXML
    public ListView lvServer;
    @FXML
    public ListView lvClient;
    @FXML
    public Button signIn;
    @FXML
    public AnchorPane viewapp;
    @FXML
    public Button signOut;

    private Client client;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientController() {
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openConnection() throws IOException {
        Socket socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            while (true) {
                try {
                    String message = in.readUTF();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void signinBtnClick() { //Нажатие кнопки войти

        String login = tfLogin.getText();
        String pass = tfPass.getText();
        AuthService authService = new AuthService();
        String nick = authService.getNickByLoginAndPassword(login, pass);
        if (login.equals(nick)) {
            viewapp.setVisible(true);
            System.out.println("Пользователь " + nick + " авторизовался ");
            if (!checkDirectory(nick)) { //проверяет наличие папки на сервере
                createDirectory(nick);   //создает папку на сервере
            } else System.out.println("Папка еже существует");
        } else System.out.println("Не правильный логин или пароль");
        try {
            fillFileView(nick);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void signoutBtnClick() {

    }

    private boolean checkDirectory(String nick) { //проверяет наличие папки пользователя на сервере
        boolean status;
        Path path = Paths.get("C:\\GIT\\cloud_storage\\src\\main\\java\\server\\files", nick);
        status = Files.exists(path);
        return status;
    }

    private void createDirectory(String nick) { //создает папку на сервере
        try {
            Path path = Paths.get("C:\\GIT\\cloud_storage\\src\\main\\java\\server\\files", nick);
            Files.createDirectory(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillFileView(String nick) throws IOException {
        Path clientDir = Paths.get("./");
        Path serverDir = Paths.get("C:\\GIT\\cloud_storage\\src\\main\\java\\server\\files", nick);
        List<String> files = Files.list(serverDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        List<String> files2 = Files.list(clientDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(() -> lvServer.getItems().addAll(files));
        Platform.runLater(() -> lvClient.getItems().addAll(files2));
    }
}

