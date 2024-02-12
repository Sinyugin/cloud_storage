package org.example.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.server.AuthService;
import org.example.server.Command;
import org.example.server.FileInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientController {

    @FXML
    VBox leftPanel, rightPanel;
    @FXML
    public TextField tfLogin;
    @FXML
    public PasswordField tfPass;
    @FXML
    public Button signIn;
    @FXML
    public Button signOut;
    @FXML
    public ListView lvLog;
    @FXML
    TableView<FileInfo> filesTable;
    @FXML
    TextField pathField;

    String nick = "";
    Path serverDir = Paths.get("C:\\GIT\\cloud_storage\\NIO\\src\\main\\java\\org\\example\\server\\files");

    SocketChannel socketChannel;
    private void openConnection() {
        InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 8189);

        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(serverAddress);
            if (socketChannel.isConnected()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.clear();
                sendRequest(socketChannel, buffer, Command.LS.getCommand());
                System.out.println(receiveResponse(socketChannel, buffer));
            } else {
                System.out.println("Failed to connect to the server");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(SocketChannel socketChannel, ByteBuffer buffer, String request) throws IOException {
        buffer.clear();
        buffer.put(request.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
    }

    private List<String> receiveResponse(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        List<String> fileList = new ArrayList<>();
//        buffer.clear();
        int bytesRead = socketChannel.read(buffer);
        buffer.flip();
        if (bytesRead > 0) {
            while (buffer.hasRemaining()) {
                String line = StandardCharsets.UTF_8.decode(buffer).toString();
                fileList.add(line);
            }
        }
        return fileList;
    }

    public void copyBtnAction(ActionEvent actionEvent) {
        PanelController leftPC = (PanelController) leftPanel.getProperties().get("ctrl");
        PanelController rightPC = (PanelController) rightPanel.getProperties().get("ctrl");

        if (leftPC.getSelectedFilename() == null && rightPC.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ни один файл не был выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        PanelController srcPC = null, dstPC = null;
        if (leftPC.getSelectedFilename() != null) {
            srcPC = leftPC;
            dstPC = rightPC;
        }
        if (rightPC.getSelectedFilename() != null) {
            srcPC = rightPC;
            dstPC = leftPC;
        }

        Path srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFilename());
        Path dstPath = Paths.get(dstPC.getCurrentPath()).resolve(srcPath.getFileName().toString());

        try {
            Files.copy(srcPath, dstPath);
            dstPC.updateList(Paths.get(dstPC.getCurrentPath()));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось скопировать указанный файл", ButtonType.OK);
            alert.showAndWait();
        }
    }


    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void signinBtnClick(ActionEvent actionEvent) {
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
            signOut.setVisible(true); //открывает кнопку Выход
            signIn.setVisible(false); //скрывает кнопку Войти

            PanelController leftPC = (PanelController) leftPanel.getProperties().get("ctrl");
//            Path pathServer = Paths.get(String.valueOf(serverDir.resolve(nick)));
//            leftPC.pathField.setText(String.valueOf(serverDir.relativize(pathServer)));
            leftPC.pathField.setText(String.valueOf(serverDir.resolve(nick)));
            leftPC.diskBox.setVisible(false);

            leftPanel.setVisible(true); //открывает левую панель
            leftPC.updateList(Paths.get(leftPC.pathField.getText()));

            Platform.runLater(() -> lvLog.getItems().addAll("Пользователь " + nick + " авторизовался "));
            if (!checkDirectory(nick)) { //проверяет наличие папки на сервере
                createDirectory(nick);   //создает папку на сервере
            } else System.out.println("Папка еже существует");
        } else Platform.runLater(() -> lvLog.getItems().addAll("Не правильный логин или пароль"));
    }

    public void closeConnect() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            sendRequest(socketChannel, buffer, Command.QUIT.getCommand());
            Platform.runLater(() -> lvLog.getItems().addAll("Пользователь отключился"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        signIn.setVisible(true); //показывает кнопку Войти
        signOut.setVisible(false); //скрывает кнопку Выход
        leftPanel.setVisible(false); //скрывает левую панель
        tfPass.setText("");
        tfLogin.setText("");

    }

    public void createFileInServer(ActionEvent actionEvent) {
        PanelController leftPC = (PanelController) leftPanel.getProperties().get("ctrl");
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setContentText("Введите название файла:");
        dialog.setTitle("имя файла");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Path newDir = Paths.get(leftPC.pathField.getText());
            String nameFile = dialog.getEditor().getText();
            try {
                Files.createFile(newDir.resolve(nameFile));
                leftPC.updateList(Paths.get(leftPC.pathField.getText()));
                Platform.runLater(() -> lvLog.getItems().addAll("Создан файл " + nameFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void createFileInClient(ActionEvent actionEvent) {
        PanelController rightPC = (PanelController) rightPanel.getProperties().get("ctrl");
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setContentText("Введите название файла:");
        dialog.setTitle("имя файла");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Path newDir = Paths.get(rightPC.pathField.getText());
            String nameFile = dialog.getEditor().getText();
            try {
                Files.createFile(newDir.resolve(nameFile));
                rightPC.updateList(Paths.get(rightPC.pathField.getText()));
                Platform.runLater(() -> lvLog.getItems().addAll("Создан файл " + nameFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void createDirBtnClick(ActionEvent actionEvent) {
        PanelController leftPC = (PanelController) leftPanel.getProperties().get("ctrl");
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setContentText("Введите название папки:");
        dialog.setTitle("имя папки");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Path newDir = Paths.get(leftPC.pathField.getText());
            String nameDir = dialog.getEditor().getText();
            try {
                Files.createDirectory(newDir.resolve(nameDir));
                leftPC.updateList(Paths.get(leftPC.pathField.getText()));
                Platform.runLater(() -> lvLog.getItems().addAll("Создана папка " + nameDir));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void createDirClient(ActionEvent actionEvent) {
        PanelController rightPC = (PanelController) rightPanel.getProperties().get("ctrl");
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setContentText("Введите название папки:");
        dialog.setTitle("имя папки");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Path newDir = Paths.get(rightPC.pathField.getText());
            String nameDir = dialog.getEditor().getText();
            try {
                Files.createDirectory(newDir.resolve(nameDir));
                rightPC.updateList(Paths.get(rightPC.pathField.getText()));
                Platform.runLater(() -> lvLog.getItems().addAll("Создана папка " + nameDir));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void deleteInServer(ActionEvent actionEvent) {
        PanelController leftPC = (PanelController) leftPanel.getProperties().get("ctrl");

        if (leftPC.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ни один файл не был выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        String text = leftPC.getSelectedFilename().toString();
        String[] arr = text.split(" ");
        String token = arr[0];
        Path currentDir = Paths.get(leftPC.pathField.getText());
        currentDir = currentDir.resolve(token);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Удалить " + token + " ?", ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            try {
                Path finalCurrentDir = currentDir;
                Platform.runLater(() -> {
                    try {
                        Files.delete(finalCurrentDir);
                        leftPC.updateList(Paths.get(leftPC.pathField.getText()));
                    } catch (DirectoryNotEmptyException e) {
                        Alert alert1 = new Alert(Alert.AlertType.WARNING, "Директория не пуста!");
                        alert1.showAndWait();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                leftPC.updateList(Paths.get(leftPC.pathField.getText()));
                Platform.runLater(() -> lvLog.getItems().addAll(token + " удалён"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void dellInClient(ActionEvent actionEvent) {
        PanelController rightPC = (PanelController) rightPanel.getProperties().get("ctrl");

        if (rightPC.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ни один файл не был выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        String text = rightPC.getSelectedFilename().toString();
        String[] arr = text.split(" ");
        String token = arr[0];
        Path currentDir = Paths.get(rightPC.pathField.getText());
        currentDir = currentDir.resolve(token);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Удалить " + token + " ?", ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            try {
                Path finalCurrentDir = currentDir;
                Platform.runLater(() -> {
                    try {
                        Files.delete(finalCurrentDir);
                        rightPC.updateList(Paths.get(rightPC.pathField.getText()));
                    } catch (DirectoryNotEmptyException e) {
                        Alert alert1 = new Alert(Alert.AlertType.WARNING, "Директория не пуста!");
                        alert1.showAndWait();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                rightPC.updateList(Paths.get(rightPC.pathField.getText()));
                Platform.runLater(() -> lvLog.getItems().addAll(token + " удалён"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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

}
