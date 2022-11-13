package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("/cloud_storage/client/client.fxml"));
        primaryStage.setTitle("Сетевое хранилище");
        primaryStage.setScene(new Scene(parent));
        primaryStage.show();

    }
}
