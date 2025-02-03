module eneev.monitor1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens eneev.monitor1 to javafx.fxml;
    exports eneev.monitor1;
}