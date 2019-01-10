package gr.cytech.chatreminderbot.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import gr.cytech.chatreminderbot.rest.message.Message;
import gr.cytech.chatreminderbot.rest.message.Request;
import gr.cytech.chatreminderbot.rest.message.Sender;
import gr.cytech.chatreminderbot.rest.message.ThreadM;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class BotResourceIT {
    private static final Logger logger = LoggerFactory.getLogger(BotResourceIT.class);

    @Test
    public void handleRequest() throws Exception {
        BotResource botResource = new BotResource();

        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();


        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);
        String expectedDate = "12/12/2020 12:00 GMT-2";
        String spaceId = "SPACE_ID";
        String what = "something to do";
        botResource.setTimeZone("Europe/Athens");

        String expectedDate2 = "12/12/2020 12:00";
        String successMsg = "Reminder: <<" + what +
                ">> saved succesfully and will notify you in: " +
                botResource.calculateRemainingTime(botResource.dateForm(expectedDate2));

        mes.setText("reminder me '" + what + "' at " + expectedDate);
        req.setMessage(mes);

        Client c = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        assertThat(resp.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(resp.readEntity(String.class)).isEqualTo("{ \"text\": \"" + successMsg +
                "\" ,  \"thread\": { \"name\": \"spaces/" + spaceId + "\" }}");

    }


    @Test
    public void showRemindersTest() {
        BotResource botResource = new BotResource();

        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();


        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("users/102853879309256732507");

        mes.setThread(threadM);
        mes.setSender(sender);

        botResource.setTimeZone("Europe/Athens");


        mes.setText("delete 134");
        req.setMessage(mes);

        Client c = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        //  assertThat(resp.readEntity(String.class)).isEqualTo("{ \"text\": \"" + successMsg
        //         "\" ,  \"thread\": { \"name\": \"spaces/" + spaceId + "\" }}");
        logger.info("{}",resp.readEntity(String.class));
    }
}