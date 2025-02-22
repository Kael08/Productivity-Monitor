package productivityMonitor.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainController {
    @FXML
    private ImageView runImageView;
    @FXML
    private ImageView settingsImageView;
    @FXML
    private ImageView timerImageView;

    @FXML
    public void initialize(){
        Image runImg = new Image(getClass().getResource("/images/run-ico.png").toExternalForm());

        if(runImg==null)
            System.out.println("Картинка не найдена!");

        runImageView.setImage(runImg);
        settingsImageView.setImage(new Image(getClass().getResource("/images/settings-ico.png").toExternalForm()));
        timerImageView.setImage(new Image(getClass().getResource("/images/clock-ico.png").toExternalForm()));
    }
}