package gr.cytech.chatreminderbot.rest.message;

import java.util.List;
import java.util.Map;

public class Action {
    private String actionMethodName;
    private List<Map<String,String>> parameters;

    public String getActionMethodName() {
        return actionMethodName;
    }

    public List<Map<String, String>> getParameters() {
        return parameters;
    }

    public void setParameters(List<Map<String, String>> parameters) {
        this.parameters = parameters;
    }

    public void setActionMethodName(String actionMethodName) {
        this.actionMethodName = actionMethodName;
    }
}
