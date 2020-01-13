import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class Network {
    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;
    public static Boolean online = false;

    public static void start() {
        try {
            socket = new Socket("localhost", 8189);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
            online = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {

        online = false;
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendMsg(AbstractMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static AbstractMessage readObject() throws ClassNotFoundException, IOException {

            Object obj = in.readObject();
            return (AbstractMessage) obj;


    }
}