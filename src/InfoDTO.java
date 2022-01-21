import java.io.Serializable;

enum Info{
    JOIN, EXIT, SEND
}

public class InfoDTO implements Serializable {
    private String nickname;
    private String message;
    private Info command;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Info getCommand() {
        return command;
    }

    public void setCommand(Info command) {
        this.command = command;
    }
}
