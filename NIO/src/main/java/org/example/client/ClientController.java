package org.example.client;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.example.server.AuthService;
import org.example.server.FileInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
    @FXML
    public ListView lvLog;
    @FXML
    public Button BtnDellInClient;

    private Socket socket;

    private byte[] buffer;
    private DataInputStream in;
    private DataOutputStream out;
    String nick = "";

    Path clientDir = Paths.get("C:\\GIT\\cloud_storage\\src\\main\\java\\client\\files");
    Path serverDir = Paths.get("C:\\GIT\\cloud_storage\\NIO\\src\\main\\java\\org\\example\\server\\files");

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
                        System.out.println("Клиент отключился");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnect();
                    System.out.println("Клиент отключился");
                }
            }
        }).start();
    }

    private void closeConnect() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void signinBtnClick() { //Нажатие кнопки войти

        String login = tfLogin.getText();
        String pass = tfPass.getText();
        AuthService authService = new AuthService();
        nick = authService.getNickByLoginAndPassword(login, pass);
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
            out.writeUTF("quit");
            System.out.println("quit");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkDirectory(String nick) { //проверяет наличие папки пользователя на сервере
        boolean status;
        status = Files.exists(serverDir.resolve(nick));
        return status;
    }

    private void createDirectory(String nick) { //создает папку пользователя на сервере
        try {
            Files.createDirectory(serverDir.resolve(nick));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<FileInfo> scanFiles(Path root) {
        try {
            return Files.list(root).map(FileInfo::new).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillFileView(String nick) throws IOException {

        List<String> files = Files.list(serverDir.resolve(nick))
                .map(p -> p.getFileName().toString() + " " + sizeFiles(p))
                .collect(Collectors.toList());

        List<String> files2 = Files.list(clientDir)
                .map(p -> p.getFileName().toString() + " " + sizeFiles(p))
                .collect(Collectors.toList());

        Platform.runLater(() -> lvServer.getItems().clear());
        Platform.runLater(() -> lvServer.getItems().addAll(files));

        Platform.runLater(() -> lvClient.getItems().clear());
        Platform.runLater(() -> lvClient.getItems().addAll(files2));

        Platform.runLater(() -> tfParentServer.setText(serverDir.resolve(nick).toString()));
        Platform.runLater(() -> tfParentClient.setText(clientDir.toString()));
    }

    private void refreshList() throws IOException {

        List<String> filesInServer = Files.list(Paths.get(tfParentServer.getText()))
                .map(p -> p.getFileName().toString() + " " + sizeFiles(p))
                .collect(Collectors.toList());

        List<String> filesInClient = Files.list(Paths.get(tfParentClient.getText()))
                .map(p -> p.getFileName().toString() + " " + sizeFiles(p))
                .collect(Collectors.toList());

        Platform.runLater(() -> lvServer.getItems().clear());
        Platform.runLater(() -> lvServer.getItems().addAll(filesInServer));

        Platform.runLater(() -> lvClient.getItems().clear());
        Platform.runLater(() -> lvClient.getItems().addAll(filesInClient));

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
                refreshList();
                Platform.runLater(() -> lvLog.getItems().addAll("Создана папка " + nameDir));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void createDirClient(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setContentText("Введите название папки:");
        dialog.setTitle("имя папки");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Path newDir = Paths.get(tfParentClient.getText());
            String nameDir = dialog.getEditor().getText();
            try {
                Files.createDirectory(newDir.resolve(nameDir));
                refreshList();
                Platform.runLater(() -> lvLog.getItems().addAll("Создана папка " + nameDir));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void createFileInServer(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setContentText("Введите название файла:");
        dialog.setTitle("имя файла");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Path newDir = Paths.get(tfParentServer.getText());
            String nameFile = dialog.getEditor().getText();
            try {
                Files.createFile(newDir.resolve(nameFile));
                refreshList();
                Platform.runLater(() -> lvLog.getItems().addAll("Создан файл " + nameFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void createFileInClient(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setContentText("Введите название файла:");
        dialog.setTitle("имя файла");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Path newDir = Paths.get(tfParentClient.getText());
            String nameFile = dialog.getEditor().getText();
            try {
                Files.createFile(newDir.resolve(nameFile));
                refreshList();
                Platform.runLater(() -> lvLog.getItems().addAll("Создан файл " + nameFile));
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
                Path tempDir = Paths.get(tfParentServer.getText()).resolve(token);
                if (Files.isDirectory(tempDir)) {
                    Path currentDir = tempDir;
                    Platform.runLater(() -> tfParentServer.setText(currentDir.toString()));
                    Platform.runLater(() -> lvServer.getItems().clear());
                    List<String> files;
                    try {
                        files = Files.list(currentDir)
                                .map(p -> p.getFileName().toString() + " " + sizeFiles(p))
                                .collect(Collectors.toList());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    Platform.runLater(() -> lvServer.getItems().addAll(files));
                }
            }
        });
        lvClient.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String text = lvClient.getFocusModel().getFocusedItem().toString();
                String[] arr = text.split(" ");
                String token = arr[0];
                Path tempDir = Paths.get(tfParentClient.getText()).resolve(token);
                if (Files.isDirectory(tempDir)) {
                    Path currentDir = tempDir;
                    Platform.runLater(() -> tfParentClient.setText(currentDir.toString()));
                    Platform.runLater(() -> lvClient.getItems().clear());
                    List<String> files;
                    try {
                        files = Files.list(currentDir)
                                .map(p -> p.getFileName().toString() + " " + sizeFiles(p))
                                .collect(Collectors.toList());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    Platform.runLater(() -> lvClient.getItems().addAll(files));
                }
            }
        });
    }

    public void deleteInServer() {
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
                refreshList();
                Platform.runLater(() -> lvLog.getItems().addAll(token + " удалён"));
            } catch (DirectoryNotEmptyException e) {
                Alert alert1 = new Alert(Alert.AlertType.WARNING, "Директория не пуста!");
                alert1.showAndWait();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

        public void dellInClient () {
            String text = lvClient.getFocusModel().getFocusedItem().toString();
            String[] arr = text.split(" ");
            String token = arr[0];
            Path currentDir = Paths.get(tfParentClient.getText());
            currentDir = currentDir.resolve(token);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Удалить " + token + " ?", ButtonType.OK, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.OK) {
                try {
                    Files.delete(currentDir);
                    refreshList();
                    Platform.runLater(() -> lvLog.getItems().addAll(token + " удалён"));
                } catch (DirectoryNotEmptyException e) {
                    Alert alert1 = new Alert(Alert.AlertType.WARNING, "Директория не пуста!");
                    alert1.showAndWait();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @FXML
        private void ParentDirInServer () throws IOException {
            Path currentDir = Paths.get(tfParentServer.getText());
            Path parentPath = currentDir.getParent();
            tfParentServer.setText(String.valueOf(parentPath));
            refreshList();
        }

        @FXML
        private void ParentDirInClient () throws IOException {
            Path currentDir = Paths.get(tfParentClient.getText());
            Path parentPath = currentDir.getParent();
            tfParentClient.setText(String.valueOf(parentPath));
            refreshList();
        }
    }
