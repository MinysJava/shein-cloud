import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Client extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Autorization");
        Scene scene = new Scene(root,400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Network.sendMsg(new Request("close"));

                Platform.exit();
                System.exit(0);
                Network.stop();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
