public class Message extends AbstractMessage {
    private String text;

    public Message(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
