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

import static gr.cytech.chatreminderbot.rest.GoogleCards.CardResponseBuilder.NEW_MESSAGE;
import static gr.cytech.chatreminderbot.rest.controlCases.Control.KEYWORD_REMIND;
import static gr.cytech.chatreminderbot.rest.message.Action.*;

@Path("/services")
public class BotResource {
    private static final Logger logger = LoggerFactory.getLogger(BotResource.class);

    @Inject
    Control control;

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
        if (message.startsWith(ALREADY_BUILD_MESSAGE_WITH_ACTION)) {
            return message;
        }
        return new CardResponseBuilder()
                .cardWithOnlyText("spaces/" + spaceId, message, NEW_MESSAGE);
    }

    private void manipulateRequestBasedOnParameters(Request req) {
        String text = req.getAction().getParameters().get(2).get("value");
        int reminderId = Integer.valueOf(req.getAction().getParameters().get(1).get("value"));

        req.getMessage().getSender().setName(req.getUser().getName());

        String remindMe = KEYWORD_REMIND + " me " + text + " ";

        if (REMIND_AGAIN_IN_10_MINUTES.equals(req.getAction().getActionMethodName())) {
            req.getMessage().setText(remindMe + "in 10 minutes");
        } else if (REMIND_AGAIN_TOMORROW.equals(req.getAction().getActionMethodName())) {
            req.getMessage().setText(remindMe + "tomorrow");
        } else if (REMIND_AGAIN_NEXT_WEEK.equals(req.getAction().getActionMethodName())) {
            req.getMessage().setText(remindMe + "in next week");
        } else if (CANCEL_REMINDER.equals(req.getAction().getActionMethodName())) {
            req.getMessage().setText("delete " + reminderId);
        }
    }

}