import java.io.Serializable;

public class MyMessage implements Serializable {
    private String text;

    public String getText() {return text;}

    public MyMessage(String text){
        this.text = text;
    }
}
