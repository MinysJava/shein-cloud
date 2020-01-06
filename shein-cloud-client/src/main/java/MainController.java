import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    ListView<String> filesListClient;
    @FXML
    ListView<String> filesListServer;
    @FXML
    ListView<String> textResultList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Network.sendMsg(new Request("refresh"));
//        System.out.println("client send refresh");


        Thread t = new Thread(() -> {
        try {
//            Network.sendMsg(new Request("refresh"));
//            Network.sendMsg(new FileMessage(Paths.get("client_file/redCircle.png" )));
//            Network.sendMsg(new Request("rename","poem_server.txt", "super_new_poem.txt"));
//            Network.sendMsg(new Request("send", "music.mp3"));
//            Network.sendMsg(new Request("delete", "del.txt"));

            while (true) {
                AbstractMessage am = Network.readObject();
                if (am instanceof FileMessage) {
                    FileMessage fm = (FileMessage) am;
                    Files.write(Paths.get("client_file/User/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                }

                if (am instanceof Request){
                    Request rf = (Request) am;
                    switch (rf.getCommand()){
                        case ("refresh"):
//                            System.out.println("return refresh");
                           refreshServerFilesList(rf.getFileList());
                           break;
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            Network.stop();
        }
        });
        t.setDaemon(true);
        t.start();
        refreshClientFilesList();



    }

    private void refreshClientFilesList() {
        updateUI(() -> {
            try {
                filesListClient.getItems().clear();
                Files.list(Paths.get("client_file/User")).map(p -> p.getFileName().toString()).forEach(o -> filesListClient.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshServerFilesList(ArrayList arrayListServer) {
        updateUI(() -> {
//            Network.sendMsg(new Request("refresh"));
            filesListServer.getItems().clear();
            for (Object o : arrayListServer) {

                filesListServer.getItems().add((String) o);
            }
//                Files.list(Paths.get("client_file/User")).map(p -> p.getFileName().toString()).forEach(o -> filesListClient.getItems().add(o));
        });
    }



    private static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void sendFile(ActionEvent actionEvent) {
        try {
            Network.sendMsg(new FileMessage(Paths.get("client_file/User/redCircle.png" )));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
