import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Client {
    public static void main(String[] args) {
        Network.start();


//        Thread t = new Thread(() -> {
            try {
                Network.sendMsg(new FileMessage(Paths.get("client_file/redCircle.png" )));
                Network.sendMsg(new Request("rename","poem_server.txt", "super_new_poem.txt"));
                Network.sendMsg(new Request("send", "music.mp3"));
                Network.sendMsg(new Request("delete", "del.txt"));

                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_file/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);

                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
//        });
//        t.setDaemon(true);
//        t.start();



    }
}
