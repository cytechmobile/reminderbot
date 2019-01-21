package gr.cytech.chatreminderbot.rest;

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
    private final static Logger logger = LoggerFactory.getLogger(BotResource.class.getName());
    private String space_id;
    private String message;
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
        space_id = req.getMessage().getThread().getSpaceId();
        try{
            message = control.controlResponse();
            return responseBuild();
        }catch (Exception e){
            logger.info("Error: {} from message:{}",e,req.getMessage().getText());
            message = "Not even a clue what you just said";
            return responseBuild();
        }
    }

    private String responseBuild( ) {
        return "{ \"text\": \"" + message + "\" ,  \"thread\": { \"name\": \"spaces/" + space_id + "\" }}";
    }

}
