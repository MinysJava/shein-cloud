import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    VBox mainLogoStage;

//    public int id;

//    public MainController backController;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof Request){
                        Request rf = (Request) am;
                        switch (rf.getCommand()){
                            case ("loginOk"):
                                //                                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Login.fxml"));
//                                    Parent root = fxmlLoader.load();
//                                    primaryStage.setTitle("Autorization");
//                                    Scene scene = new Scene(root,400, 200);
//                                    primaryStage.setScene(scene);
//                                    primaryStage.show();

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        Stage stage = new Stage();
                                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
                                        Parent root = null;
                                        try {
                                            root = loader.load();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        stage.setTitle("Shein_Cloud Client");
                                        Scene scene = new Scene(root,600, 600);
                                        stage.setScene(scene);
                                        stage.show();

                                        mainLogoStage.getScene().getWindow().hide();
                                    }
                                });

//                                    LoginController lc = (LoginController) loader.getController();


//                                    stage.initModality(Modality.APPLICATION_MODAL);
//                                    stage.showAndWait();

                                break;
                            case ("loginFail"):
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        Alert alert = new Alert(Alert.AlertType.ERROR, "Неверный логин/пароль", ButtonType.OK);
                                        alert.showAndWait();
                                    }
                                });

                                break;
                        }
                        break;
                    }

                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
//            finally {
//                Network.stop();
//            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void auth(ActionEvent actionEvent) {
        Network.sendMsg( new LoginRequest("login", login.getText(), password.getText()));



    }
}
