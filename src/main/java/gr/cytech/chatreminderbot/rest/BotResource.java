package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.GoogleCards.CardResponseBuilder;
import gr.cytech.chatreminderbot.rest.controlCases.Control;
import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/services")
public class BotResource {
    private static final Logger logger = LoggerFactory.getLogger(BotResource.class);

    @Inject
    Control control;

    //Constant variables
    private static final String REMIND_AGAIN_IN_10_MINUTES = "remindAgainIn10";
    private static final String REMIND_AGAIN_TOMORROW = "remindAgainTomorrow";
    private static final String REMIND_AGAIN_NEXT_WEEK = "remindAgainNextWeek";
    private static final String CANCEL_REMINDER = "CancelReminder";

    /*
     * Handles requests from google chat which are assign to this path
     * @param a Request object that it parses a json
     * @return synchronous response in String - form to be json
     *
     * */
    @POST
    @Path("/handleReq")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String handleReq(Request req) {
        if (req.getAction() != null) {
            manipulateRequestBasedOnParameters(req);
        }
        control.setRequest(req);
        String spaceId = req.getMessage().getThread().getSpaceId();
        String message;
        try {
            message = control.controlResponse();
        } catch (Exception e) {
            logger.warn("Error from message:{}", req.getMessage().getText(), e);
            message = "Not even a clue what you just said";
        }

        //case message is already build in json
        if (message.startsWith("{\"actionResponse\":{\"type\":")) {
            return message;
        }
        return responseBuild(spaceId, message);
    }

    private void manipulateRequestBasedOnParameters(Request req) {
        String text = "";
        int reminderId = 0;
        for (int i = 0; i < req.getAction().getParameters().size(); i++) {
            String tempValue = req.getAction().getParameters().get(i).get("value");
            String tempKey = req.getAction().getParameters().get(i).get("key");
            if ("text".equals(tempKey)) {
                text = tempValue;
            } else if ("reminderId".equals(tempKey)) {
                reminderId = Integer.valueOf(tempValue);
            }
        }

        req.getMessage().getSender().setName(req.getUser().getName());

        if (REMIND_AGAIN_IN_10_MINUTES.equals(req.getAction().getActionMethodName())) {
            req.getMessage().setText("remind me " + text + " in 10 minutes");
        } else if (REMIND_AGAIN_TOMORROW.equals(req.getAction().getActionMethodName())) {
            req.getMessage().setText("remind me " + text + " tomorrow");
        } else if (REMIND_AGAIN_NEXT_WEEK.equals(req.getAction().getActionMethodName())) {
            req.getMessage().setText("remind me " + text + " in next week");
        } else if (CANCEL_REMINDER.equals(req.getAction().getActionMethodName())) {
            req.getMessage().setText("delete " + reminderId);
        }
    }

    private String responseBuild(String spaceId, String message) {
        return new CardResponseBuilder()
                .thread("spaces/" + spaceId)
                .textParagraph(message)
                .build("NEW_MESSAGE");

    }

}