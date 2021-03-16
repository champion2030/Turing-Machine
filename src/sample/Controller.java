package sample;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;

public class Controller implements Initializable {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Tab Tab2;

    @FXML
    private Tab Tab1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            AnchorPane pane1 = FXMLLoader.load(getClass().getResource("Tab1.fxml"));
            Tab1.setContent(pane1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            AnchorPane pane2 = FXMLLoader.load(getClass().getResource("Tab2.fxml"));
            Tab2.setContent(pane2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

