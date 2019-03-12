package gr.cytech.chatreminderbot.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Path("/services")
public class BotResource {
    private static final Logger logger = LoggerFactory.getLogger(BotResource.class);
    private String spaceId;
    private String message;
    @Inject
    private Control control;

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
        spaceId = req.getMessage().getThread().getSpaceId();
        try {
            message = control.controlResponse();
            return responseBuild();
        } catch (Exception e) {
            logger.warn("Error from message:{}", req.getMessage().getText(), e);
            message = "Not even a clue what you just said";
            return responseBuild();
        }
    }

    @GET
    @Path("/button")
    public String button(@Context HttpServletRequest request) {
        Request req = new Request();
        Message message = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        sender.setName(request.getParameter("name"));
        threadM.setName("space/" + request.getParameter("space") + "/thread/" + request.getParameter("thread"));

        //create reminder and get the when

        ZonedDateTime fromNowPlus10 = ZonedDateTime.now(ZoneId.of(request.getParameter("timezone"))).plusMinutes(10);

        String text = "remind me '"
                + request.getParameter("text")
                + "' at "
                + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(fromNowPlus10)
                + " "
                + request.getParameter("timezone");

        //create the he Request using the updated text
        message.setSender(sender);
        message.setThread(threadM);
        message.setText(text);

        req.setMessage(message);

        return handleReq(req)
                + "<html>"
                + "<head></head>"
                + "<body>"
                + "<script>"
                + "window.close();"
                + "</script>"
                + "</body>"
                + "</html>";
    }

    private String responseBuild() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Object response = new CardResponseBuilder()
                .thread("spaces/" + spaceId)
                .textParagraph(message)
                .build();

        String cardResponse;
        try {
            cardResponse = mapper.writeValueAsString(response);
        } catch (Exception e) {
            return "Internal server error";
        }
        return cardResponse;

    }

}
