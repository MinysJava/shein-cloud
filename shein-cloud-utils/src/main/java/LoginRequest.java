import java.util.ArrayList;

public class LoginRequest extends AbstractMessage{

    private String login;
    private String pass;

    public LoginRequest(String login, String pass){
        this.login = login;
        this.pass = pass;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

}
