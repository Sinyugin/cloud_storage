package client;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import server.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


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
            new  Thread(() ->{
                while (true){
                    try {
                        String message = in.readUTF();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
    }

//    public void authentication() {
//        String login = tfLogin.toString();
//        String pass = tfPass.toString();
//        authService.getNickByLoginAndPassword(login, pass);
//    }

    public void signinBtnClick() {

        //1. проверка nick и password
        //2. если авторизация успешна - закрыть сцену авторизации и открыть основное окно программы
        //3. проверка папки nick на сервере, еслм нет создать
        //4. создание соединения под nick

        String login = tfLogin.getText();
        String pass = tfPass.getText();
        AuthService authService = new AuthService();
        String nick = authService.getNickByLoginAndPassword(login, pass);
        if (login.equals(nick)) {
            viewapp.setVisible(true);
            System.out.println("Пользователь " + nick + " авторизовался ");
        } else System.out.println("Введите логин и пароль");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

