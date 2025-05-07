package productivityMonitor.application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

import static productivityMonitor.utils.DataLoader.loadLocalizationFromFile;
import static productivityMonitor.services.SettingsService.localization;

public class MainApp extends Application {
    public static Stage MainStage;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/fxml/mainView.fxml"));

        Parent root = fxmlLoader.load();

        MainStage = stage;
        Scene scene = new Scene(root);

        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/images/icon.png")));
        stage.setTitle("Productivity Monitor");
        stage.setMinWidth(850);
        stage.setMinHeight(500);

        stage.setOnCloseRequest(event-> {
            Platform.exit();
            System.exit(0);
        });

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Чтения и сохранение файла локализации
        localization = loadLocalizationFromFile();

        launch();
    }
}