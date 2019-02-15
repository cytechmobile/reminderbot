package gr.cytech.chatreminderbot.rest.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThreadM {
    private String name;

    public ThreadM() {
    }
    public String getName() {
        return name;
    }
    public String getSpaceId() {
        return  this.getName().split("/")[1];
    }
    public String getThreadId() {
        return  this.getName().split("/")[3];
    }

    public void setName(String name) {
        this.name = name;
    }

}
