package productivityMonitor.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import productivityMonitor.interfaces.ModeWindow;
import java.io.IOException;
import static productivityMonitor.application.MainApp.MainStage;

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
        if(stage.getOwner()==null) {// тк мы заменяем окно, следовательно, необходимо проверить наличие окна-владельца, тк нельзя два раза устанавливать ссылку на владельца
            stage.initOwner(MainStage);// Устанавливает владельца
            stage.initModality(Modality.WINDOW_MODAL);// Устанавливает модальность, то есть пока окно открыто, основное окно заблокировано
        }
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
    // Специальная функция с возвращением ссылки на контроллер
    public static <T> T createSceneAndGetController(String path, String windowTitle, Stage stage, boolean isResizable) throws IOException {
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

        return loader.getController();
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
        if (controller instanceof ModeWindow) { // Проверяет, что интерфейс действительно реализован
            ((ModeWindow) controller).setStage(stage);
        }

        stage.show();
    }
}
