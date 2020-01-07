import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class SceneController implements Initializable {

    @FXML
    VBox mainVBox;
    @FXML
    TextField tfNewFileName;

    protected static String fileName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void renameFile(ActionEvent actionEvent) {
        fileName = null;
        if (tfNewFileName.getText() != null) {
            fileName = tfNewFileName.getText();
            mainVBox.getScene().getWindow().hide();
        } else {
            mainVBox.getScene().getWindow().hide();
        }
    }
}
