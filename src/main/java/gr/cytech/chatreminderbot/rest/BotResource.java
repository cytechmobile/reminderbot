package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.GoogleCards.CardResponseBuilder;
import gr.cytech.chatreminderbot.rest.controlCases.Control;
import gr.cytech.chatreminderbot.rest.message.Message;
import gr.cytech.chatreminderbot.rest.message.Request;
import gr.cytech.chatreminderbot.rest.message.Sender;
import gr.cytech.chatreminderbot.rest.message.ThreadM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

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
        control.setRequest(req);
        String spaceId = req.getMessage().getThread().getSpaceId();
        String message;
        try {
            message = control.controlResponse();
        } catch (Exception e) {
            logger.warn("Error from message:{}", req.getMessage().getText(), e);
            message = "Not even a clue what you just said";
        }
        return responseBuild(spaceId, message);
    }

    @GET
    @Path("/button")
    public String onButtonClick(@Context HttpServletRequest request) {
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        sender.setName(request.getParameter("name"));
        threadM.setName("space/" + request.getParameter("space") + "/thread/" + request.getParameter("thread"));

        //create reminder and get the when
        //creating the full text for reminder
        String text = "remind me "
                + request.getParameter("text")
                + " in 10 minutes";

        //create the Request using the updated message
        Message message = new Message();

        message.setSender(sender);
        message.setThread(threadM);
        message.setText(text);

        Request req = new Request();
        req.setMessage(message);
        //open tab to get the requirements then immediately close it and handle the request
        handleReq(req);
        return "<html>"
                + "<head></head>"
                + "<body>"
                + "<script>"
                + "window.close();"
                + "</script>"
                + "</body>"
                + "</html>";
    }

    private String responseBuild(String spaceId, String message) {
        return new CardResponseBuilder()
                .thread("spaces/" + spaceId)
                .textParagraph(message)
                .build();

    }

}