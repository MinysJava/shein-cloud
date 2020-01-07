import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;

public class MainController implements Initializable {
    @FXML
    ListView<String> filesListClient;
    @FXML
    ListView<String> filesListServer;
    @FXML
    ListView<String> textResultList;

    private static String oldName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Network.sendMsg(new Request("refresh"));

        Thread t = new Thread(() -> {
        try {
            while (true) {
                AbstractMessage am = Network.readObject();
                if (am instanceof FileMessage) {
                    FileMessage fm = (FileMessage) am;
                    Files.write(Paths.get("client_file/User/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                }
                if (am instanceof Request){
                    Request rf = (Request) am;
                    switch (rf.getCommand()){
                        case ("s_refresh"):
                           refreshServerFilesList(rf.getFileList());
                           break;
                        case ("c_refresh"):
                            refreshClientFilesList();
                            break;
                    }
                }
                if (am instanceof Message){
                    Message msg = (Message) am;
                    showMsg(msg.getText());
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

    private void showMsg(String text) {
        updateUI(() -> {
            textResultList.getItems().add(text);
        });
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
            filesListServer.getItems().clear();
            for (Object o : arrayListServer) {

                filesListServer.getItems().add((String) o);
            }
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
            if(filesListClient.getSelectionModel().getSelectedItem() != null) {
                Network.sendMsg(new FileMessage(Paths.get("client_file/User/" + filesListClient.getSelectionModel().getSelectedItem())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renameFile(ActionEvent actionEvent) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/Scene.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            if(filesListClient.getSelectionModel().getSelectedItem() != null) {
                if (SceneController.fileName != null) {
                    Path oldName = Paths.get("client_file/User/" + filesListClient.getSelectionModel().getSelectedItem());
                    Files.move(oldName, oldName.resolveSibling(SceneController.fileName));
                    refreshClientFilesList();
                }
            } else if(filesListServer.getSelectionModel().getSelectedItem() != null) {
                if (SceneController.fileName != null) {
                    Network.sendMsg(new Request("rename", filesListServer.getSelectionModel().getSelectedItem(), SceneController.fileName));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(ActionEvent actionEvent) {
        if(filesListClient.getSelectionModel().getSelectedItem() != null) {
            try {
                Files.delete(Paths.get("client_file/User/" + filesListClient.getSelectionModel().getSelectedItem()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshClientFilesList();

        } else if(filesListServer.getSelectionModel().getSelectedItem() != null) {
            Network.sendMsg(new Request("delete", filesListServer.getSelectionModel().getSelectedItem()));
        }
    }

    public void downloadFile(ActionEvent actionEvent) {
        if(filesListServer.getSelectionModel().getSelectedItem() != null) {
            Network.sendMsg(new Request("download", filesListServer.getSelectionModel().getSelectedItem()));
        }
        refreshClientFilesList();
    }
}
