package productivityMonitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/fxml/mainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);

        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/images/icon.png")));
        stage.setTitle("Productivity Monitor");

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}