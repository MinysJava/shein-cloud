import java.util.ArrayList;

public class Request extends AbstractMessage {

    private String filename;
    private String newFilename;
    private String command;

    private ArrayList arrayList;

    public Request(String command, String filename, String newFilename){
        this.filename = filename;
        this.newFilename = newFilename;
        this.command = command;
    }

    public Request(String command, String filename){
        this.filename = filename;
        this.command = command;
    }

    public Request(String command){
        this.command = command;
    }

    public Request(String command, ArrayList arrayList){
        this.command = command;
        this.arrayList = arrayList;
    }

    public String getFilename() {
        return filename;
    }

    public String getNewFilename() {
        return newFilename;
    }

    public String getCommand() {
        return command;
    }

    public ArrayList getFileList() {
        return arrayList;
    }
}
