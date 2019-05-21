package gr.cytech.chatreminderbot.rest.message;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Action {
    public static final String REMIND_AGAIN_IN_10_MINUTES = "remindAgainIn10";
    public static final String REMIND_AGAIN_TOMORROW = "remindAgainTomorrow";
    public static final String REMIND_AGAIN_NEXT_WEEK = "remindAgainNextWeek";
    public static final String CANCEL_REMINDER = "CancelReminder";
    public static final String ALREADY_BUILD_MESSAGE_WITH_ACTION = "{\"actionResponse\":{\"type\":";
    private String actionMethodName;
    private List<Map<String,String>> parameters;
    private Map<String,String> buildParametersForButton;

    public Map<String, String> getBuildParametersForButton() {
        return buildParametersForButton;
    }

    public void setBuildParametersForButton(String name, int reminderId, String text) {
        Map<String,String> orderMapParameters = new LinkedHashMap<>();
        orderMapParameters.put("name", name);
        orderMapParameters.put("reminderId", String.valueOf(reminderId));
        orderMapParameters.put("text", text);
        this.buildParametersForButton = orderMapParameters;
    }

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
