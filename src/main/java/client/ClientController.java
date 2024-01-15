package client;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.example.server.AuthService;
import org.example.server.Command;
import server.Server;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    @FXML
    public TextField tfParentClient;
    @FXML
    public TextField tfParentServer;
    @FXML
    public TableView tvServer;
    @FXML
    public Button signUp;
    @FXML
    public Button createDirServer;
    @FXML
    public Button BtnDell;
    public ListView lvLog;

    private Client client;
    private Socket socket;
    private Server server;
    private byte[] buffer;
    private DataInputStream in;
    private DataOutputStream out;

    Map<String, String> pathFiles;

    Path clientDir = Paths.get("C:\\GIT\\cloud_storage\\src\\main\\java\\client\\files");
    Path serverDir = Paths.get("src\\main\\java\\server\\files");


    public ClientController() throws IOException {

    }

    private void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            while (true) {
                try {
                    String message = in.readUTF();
                    if (message.equals("quit")) {
                        in.close();
                        out.close();
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
            try {
                openConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
            viewapp.setVisible(true); //открывает основное окно программы
            signOut.setVisible(true); //открывает кнопку Выход
            signIn.setVisible(false); //скрывает кнопку Войти
            signUp.setVisible(false); //скрывает кнопку Зарегистрироватся
            Platform.runLater(() -> lvLog.getItems().addAll("Пользователь " + nick + " авторизовался "));
            if (!checkDirectory(nick)) { //проверяет наличие папки на сервере
                createDirectory(nick);   //создает папку на сервере
            } else System.out.println("Папка еже существует");
            try {
                fillFileView(nick);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else Platform.runLater(() -> lvLog.getItems().addAll("Не правильный логин или пароль"));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initClickListener();
        initDblClickListener();
    }

    public void signoutBtnClick() {
        try {
            in.readUTF((DataInput) Command.QUIT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        try {
//            socket = new Socket("localhost", 8189);
//            in = new DataInputStream(socket.getInputStream());
//            out = new DataOutputStream(socket.getOutputStream());
//
//            out.writeUTF("quit");
////            ClientHandler clientHandler = new ClientHandler(socket, server);
////            clientHandler.close();
////            in.close();
////            out.close();
////            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private boolean checkDirectory(String nick) { //проверяет наличие папки пользователя на сервере
        boolean status;
        status = Files.exists(serverDir.resolve(nick));
        return status;
    }

    private void createDirectory(String nick) { //создает папку на сервере
        try {
            Files.createDirectory(serverDir.resolve(nick));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillFileView(String nick) throws IOException {

        List<String> files = Files.list(serverDir.resolve(nick))
                .map(p -> p.getFileName().toString() + " " + sizeFiles(p))
                .collect(Collectors.toList());

        List<String> files2 = Files.list(clientDir)
                .map(p -> p.getFileName().toString() + " " + sizeFiles(p))
                .collect(Collectors.toList());
//        Path parentServer = serverDir.getParent();
        Path parentClient = clientDir.getParent();
        Platform.runLater(() -> lvServer.getItems().clear());
        Platform.runLater(() -> lvServer.getItems().addAll(files));
        Platform.runLater(() -> lvClient.getItems().addAll(files2));
//        Platform.runLater(() -> tfParentServer.setText(parentServer.resolve(nick).toString()));
        Platform.runLater(() -> tfParentServer.setText(serverDir.resolve(nick).toString()));
        Platform.runLater(() -> tfParentClient.setText(parentClient.toString()));
    }

    private String sizeFiles(Path path) {
        if (Files.isDirectory(path)) {
            return " [DIR]";
        } else {
            return " [FILE]  " + path.toFile().length() + " bytes";
        }
    }

    public void createDirBtnClick(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setContentText("Введите название папки:");
        dialog.setTitle("имя папки");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Path newDir = Paths.get(tfParentServer.getText());
            String nameDir = dialog.getEditor().getText();
            try {
                Files.createDirectory(newDir.resolve(nameDir));
                fillFileView("user");//todo
                Platform.runLater(() -> lvLog.getItems().addAll("Создана папка " + nameDir));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void initClickListener() {
        lvServer.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {

            }
        });
    }

    private void initDblClickListener() {
        lvServer.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String text = lvServer.getFocusModel().getFocusedItem().toString();
                String[] arr = text.split(" ");
                String token = arr[0];
                Path currentDir = Paths.get(tfParentServer.getText()).resolve(token);
                Platform.runLater(() -> tfParentServer.setText(currentDir.toString()));
            }
        });
    }

    public void deleteBtnClick() {
        String text = lvServer.getFocusModel().getFocusedItem().toString();
        String[] arr = text.split(" ");
        String token = arr[0];
        Path currentDir = Paths.get(tfParentServer.getText());
        currentDir = currentDir.resolve(token);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Удалить " + token + " ?", ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            try {
                Files.delete(currentDir);
                fillFileView("user"); //todo
                Platform.runLater(() -> lvLog.getItems().addAll(token + " удалён"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
