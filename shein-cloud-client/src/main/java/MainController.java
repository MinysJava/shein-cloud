// Контроллер гоавного окна

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



public class MainController implements Initializable {
    @FXML
    ListView<String> filesListClient;
    @FXML
    ListView<String> filesListServer;
    @FXML
    ListView<String> textResultList;

    private static String nikName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.sendMsg(new Request("loginOk"));            //Отправляем сообщение на сервер что залогинились чтобы получить Nikname пользователя

        Thread t = new Thread(() -> {
        try {
            while (Network.online) {        // принимаем сообщения с Сервера
                AbstractMessage am = Network.readObject();
                if (am instanceof FileMessage) {    // Прием файла
                    FileMessage fm = (FileMessage) am;
                    Files.write(Paths.get("client_file/" + nikName + "/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                    if (Files.exists(Paths.get("client_file/" + nikName + "/" + fm.getFilename()))) {
                        showMsg("Файл успешно скачан");
                    } else {
                        showMsg("Файл не был скачан");
                    }
                }
                if (am instanceof Request) {    // Обработка команд с сервера
                    Request rf = (Request) am;
                    switch (rf.getCommand()) {
                        case ("s_refresh"):
                            refreshServerFilesList(rf.getFileList());
                            break;
                        case ("c_refresh"):
                            refreshClientFilesList(nikName);
                            break;
                        case ("loginOk"):
                            nikName = rf.getFilename();
                            refreshClientFilesList(nikName);
                            break;
                    }
                }
                if (am instanceof Message) {
                    Message msg = (Message) am;
                    showMsg(msg.getText());
                }

            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        });
        t.setDaemon(true);
        t.start();
        Network.sendMsg(new Request("refresh"));
    }

    private void showMsg(String text) {  //Метод для отоброжения сообщений в нижнем ListView
        updateUI(() -> {
            textResultList.getItems().add(text);
        });
    }

    private void refreshClientFilesList(String user) {      //Метод для обновления списка файлов на клиенте
        updateUI(() -> {
            try {
                filesListClient.getItems().clear();
                Files.list(Paths.get("client_file/" + user + "/")).map(p -> p.getFileName().toString()).forEach(o -> filesListClient.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshServerFilesList(ArrayList arrayListServer) {    //Метод для обновления списка файлов на сервере
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

    public void sendFile(ActionEvent actionEvent) {     // Метод для отправки выбраного файла на сервер
        try {
            if(filesListClient.getSelectionModel().getSelectedItem() != null) {
                Network.sendMsg(new FileMessage(Paths.get("client_file/" + nikName + "/" + filesListClient.getSelectionModel().getSelectedItem())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renameFile(ActionEvent actionEvent) {   //Метод для  переименования выбраного файла
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Scene.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 200, 100);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            if(filesListClient.getSelectionModel().getSelectedItem() != null) {
                if (SceneController.fileName != null) {
                    Path oldName = Paths.get("client_file/" + nikName + "/" + filesListClient.getSelectionModel().getSelectedItem());
                    Files.move(oldName, oldName.resolveSibling(SceneController.fileName));
                    refreshClientFilesList(nikName);
                    showMsg("Файл переименован");
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

    public void deleteFile(ActionEvent actionEvent) {           //Метод для удаления выбраного файла
        if(filesListClient.getSelectionModel().getSelectedItem() != null) {
            try {
                Files.delete(Paths.get("client_file/" + nikName + "/" + filesListClient.getSelectionModel().getSelectedItem()));
                showMsg("Файл удален");
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshClientFilesList(nikName);
        } else if(filesListServer.getSelectionModel().getSelectedItem() != null) {
            Network.sendMsg(new Request("delete", filesListServer.getSelectionModel().getSelectedItem()));
        }
    }

    public void downloadFile(ActionEvent actionEvent) {             //Метод для скачивания выбраного файла с сервера
        if(filesListServer.getSelectionModel().getSelectedItem() != null) {
            Network.sendMsg(new Request("download", filesListServer.getSelectionModel().getSelectedItem()));
        }
        refreshClientFilesList(nikName);
    }
}
