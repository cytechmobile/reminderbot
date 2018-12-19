package gr.cytech.chatreminderbot.rest.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private String name;
    private Sender sender;
    private String createTime;
    private String text;
    private ThreadM thread;

    public Message(String name, Sender sender, String createTime, String text, ThreadM thread) {
        this.name = name;
        this.sender = sender;
        this.createTime = createTime;
        this.text = text;
        this.thread = thread;
    }

    public Message() {
    }
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }
    @JsonProperty("createTime")
    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @JsonProperty("text")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ThreadM getThread() {
        return thread;
    }

    public void setThread(ThreadM thread) {
        this.thread = thread;
    }
}
