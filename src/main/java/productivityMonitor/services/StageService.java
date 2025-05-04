package productivityMonitor.services;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import productivityMonitor.controllers.AuthController;
import productivityMonitor.controllers.MonitoringSettingsController;
import productivityMonitor.controllers.modeWindowControllers.DelayGratificationWindowController;
import productivityMonitor.controllers.modeWindowControllers.MindfulnessWindowController;
import productivityMonitor.controllers.modeWindowControllers.SailorsKnotWindowController;
import productivityMonitor.interfaces.ModeWindowInterface;

import java.io.IOException;

import static productivityMonitor.application.MainApp.MainStage;
import static productivityMonitor.services.FocusMode.*;

public class StageService {
    // Заменяет основное окно
    public static void replaceMainScene(String path,String windowTitle) throws IOException{
        // Загрузка верстки
        FXMLLoader loader = new FXMLLoader(StageService.class.getResource(path));
        Parent root = loader.load();

        // Замена окна
        MainStage.setScene(new Scene(root));
        MainStage.setTitle(windowTitle);
        MainStage.show();
    }

    // Замена окна
    public static void replaceScene(String path, String windowTitle, Stage stage,boolean isResizable) throws IOException{
        // Загрузка верстки
        FXMLLoader loader = new FXMLLoader(StageService.class.getResource(path));
        Parent root=loader.load();

        // Замена окна
        stage.setScene(new Scene(root));
        stage.setTitle(windowTitle);
        stage.initOwner(MainStage);// Устанавливает владельца
        stage.initModality(Modality.WINDOW_MODAL);// Устанавливает модальность, то есть пока окно открыто, основное окно заблокировано
        stage.setResizable(isResizable);
        stage.show();
    }

    //TODO:Создание и Замена окон одинаковые

    // Создание окна
    public static void createScene(String path,String windowTitle,Stage stage,boolean isResizable)throws IOException{
        // Загрузка верстки
        FXMLLoader loader = new FXMLLoader(StageService.class.getResource(path));
        Parent root = loader.load();

        // Создание окна
        stage.setScene(new Scene(root));
        stage.setTitle(windowTitle);
        stage.initOwner(MainStage);// Устанавливает владельца
        stage.initModality(Modality.WINDOW_MODAL);// Устанавливает модальность, то есть пока окно открыто, основное окно заблокировано
        stage.setResizable(isResizable);
        stage.show();
    }

    // Создание окна для режима
    public static void createModeAlertWindow(String path,String windowTitle, Stage stage,boolean isResizable) throws IOException{
        // Загрузка верстки
        FXMLLoader loader = new FXMLLoader(StageService.class.getResource(path));
        Parent root = loader.load();

        // Создание окна
        stage.setTitle(windowTitle);
        stage.setScene(new Scene(root));
        stage.initOwner(MainStage);// Устанавливает владельца
        stage.initModality(Modality.WINDOW_MODAL);// Устанавливает модальность, то есть пока окно открыто, основное окно заблокировано
        stage.setResizable(isResizable);

        Object controller = loader.getController();
        if (controller instanceof ModeWindowInterface) { // Проверяет, что интерфейс действительно реализован
            ((ModeWindowInterface) controller).setStage(stage);
        }

        stage.show();
    }
}
