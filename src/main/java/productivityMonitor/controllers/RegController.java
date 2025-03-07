package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class RegController {
    @FXML
    private ImageView iconImageView;

    @FXML
    private TextField logimEmailTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button authButton;
    @FXML
    private void handleAuthButton(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/authView.fxml"));
        Parent authentificationRoot = fxmlLoader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(authentificationRoot));
        stage.setTitle("Authentification");
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private Button regButton;
    @FXML
    private void handleRegButton(ActionEvent event){

    }

    private Image iconImage = new Image(getClass().getResource("/images/icon.png").toExternalForm());

    public void initialize(){
        iconImageView.setImage(iconImage);
    }
}
