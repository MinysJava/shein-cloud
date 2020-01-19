// Коньроллер окна авторизации

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    VBox mainLogoStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void auth(ActionEvent actionEvent) {     // отправляем Логин и пароль на сервер для проверки
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();  //Ловим ответы от сервера
                    if (am instanceof Request){
                        Request rf = (Request) am;
                        switch (rf.getCommand()){
                            case ("loginOk"):                       // Если авторизация прошла то запускаем главное окно Main controller
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
                                        stage.setTitle("Shein_cloud");
                                        Scene scene = new Scene(root,600, 600);
                                        stage.setScene(scene);
                                        stage.show();
                                        mainLogoStage.getScene().getWindow().hide();

                                        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {   // Обработка нажатия на крестик окна
                                            @Override
                                            public void handle(WindowEvent event) {
                                                Network.sendMsg(new Close());
                                                Platform.exit();
                                                System.exit(0);
                                                Network.stop();
                                            }
                                        });
                                    }
                                });
                                break;
                            case ("loginFail"):     // Выводим Alert о не верной авторизации
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        Alert alert = new Alert(Alert.AlertType.ERROR, "Неверный логин/пароль", ButtonType.OK);
                                        alert.showAndWait();
                                    }
                                });
                                break;
                        }
                    }
                    break;
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();// Создаем соединение
        Network.sendMsg( new LoginRequest(login.getText(), password.getText()));
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
        Network.stop();
    }
}
