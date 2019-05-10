package gr.cytech.chatreminderbot.rest.message;

public class Request {

    public static final String ALREADY_BUILD_MESSAGE = "{\"cards\":[{\"sections\":[{\"widgets\":[{\"textParagraph\":{";

    private Message message;
    private Action action;
    private User user;

    public Request() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

}


