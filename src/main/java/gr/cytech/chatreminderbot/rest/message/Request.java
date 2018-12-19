package gr.cytech.chatreminderbot.rest.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Request {

    private String type;
    private String token;
    private String eventTime;
    private Space space;
    private Message message;

    public Request(String type, String token, String eventTime, Space space, Message message) {
        this.type = type;
        this.token = token;
        this.eventTime = eventTime;
        this.space = space;
        this.message = message;
    }

    public Request() {
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }


}


