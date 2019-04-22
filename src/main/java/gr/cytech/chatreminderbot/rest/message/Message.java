package gr.cytech.chatreminderbot.rest.message;

public class Message {
    private String name;
    private Sender sender;
    private String text;
    private ThreadM thread;

    public Message() {
    }

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
