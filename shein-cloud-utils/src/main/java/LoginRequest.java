import java.util.ArrayList;

public class LoginRequest extends AbstractMessage{

    private String login;
    private String pass;
    private String command;

    public LoginRequest(String command, String login, String pass){
        this.login = login;
        this.pass = pass;
        this.command = command;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    public String getCommand() {
        return command;
    }


}
