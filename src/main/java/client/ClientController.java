package client;

import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class ClientController {
    public TextField tfLogin;
    public TextField tfPass;
    public ListView lvServer;
    public ListView lvClient;

    public ClientController() {
        Client client = new Client();
        try {
            client.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
