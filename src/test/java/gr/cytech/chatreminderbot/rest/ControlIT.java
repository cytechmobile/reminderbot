package gr.cytech.chatreminderbot.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import gr.cytech.chatreminderbot.rest.controlCases.CaseSetReminder;
import gr.cytech.chatreminderbot.rest.controlCases.Control;
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

class ControlIT {
    private static final Logger logger = LoggerFactory.getLogger(ControlIT.class);

    @Test
    void handleRequest() {


        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();


        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);

        String expectedWhen = "12/12/2019 12:00";
        String expectedDate = expectedWhen + " athens";
        String spaceId = "SPACE_ID";
        String what = "something to do";


        mes.setText("remind me '" + what + "' at " + expectedDate);
        req.setMessage(mes);
        Control control = new Control();
        control.setRequest(req);

        CaseSetReminder caseSetReminder = new CaseSetReminder();
        caseSetReminder.setRequest(req);
        caseSetReminder.setBOT_NAME("reminder");

        //In order to use calculateRemainingTime need to define: timezone, when
        caseSetReminder.setWhen(expectedWhen);
        caseSetReminder.setTimeZone("Europe/Athens");


        String successMsg = "Reminder: <<" + what +
                ">> saved successfully and will notify you in: " +
                caseSetReminder.calculateRemainingTime(caseSetReminder.dateForm());

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
    void listReminderTest() {

        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();


        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);


        mes.setText("list");
        req.setMessage(mes);

        String responseDefault = "I didnt understand you, type help for instructions \n";

        Client c = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        //Verify that i didn't get the default wrong message
        assertThat(resp.readEntity(String.class)).isNotEqualTo("{ \"text\": \"" + responseDefault +
                "\" ,  \"thread\": { \"name\": \"spaces/" + "SPACE_ID" + "\" }}");
    }

    @Test
    void setGlobalTimezoneTest() {
        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();


        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);

        mes.setText("@reminder set global timezone to athens");
        req.setMessage(mes);

        String expectedResponse = "You successfully set the global timezone at:Europe/Athens";

        Client c = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        //Verify that i didn't get the default wrong message
        assertThat(resp.readEntity(String.class)).isEqualTo("{ \"text\": \"" + expectedResponse +
                "\" ,  \"thread\": { \"name\": \"spaces/" + "SPACE_ID" + "\" }}");
    }

    @Test
    void checkRemindFormatTest() {
        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();


        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);

        mes.setText("@reminder remind me 'can't save that'at 17/01/2020 13:12");
        req.setMessage(mes);

        String expectedResponse = "Use  quotation marks  `'` only two times. One before and one after what, type Help for example.";

        Client c = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();

        assertThat(resp.readEntity(String.class)).isEqualTo("{ \"text\": \"" + expectedResponse +
                "\" ,  \"thread\": { \"name\": \"spaces/" + "SPACE_ID" + "\" }}");
    }



    @Test
    void setAndReturnTimezone() {
        Request req = new Request();
        Request req_for_timezone = new Request();

        Message mes = new Message();
        Message mes_set_timezone = new Message();

        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();


        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");


        mes_set_timezone.setThread(threadM);
        mes_set_timezone.setSender(sender);


        mes_set_timezone.setText("@reminder set my timezone to Athens");
        req_for_timezone.setMessage(mes_set_timezone);

        Client c1 = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp1 = c1.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req_for_timezone));
        resp1.bufferEntity();

        mes.setThread(threadM);
        mes.setSender(sender);

        mes.setText("@reminder timezones");
        req.setMessage(mes);

        String expectedResponse = "---- Your timezone is  ---- \n" +
                "Timezone = 'Europe/Athens'\n" +
                " ---- Default timezone is ---- \n" +
                "Timezone = 'Europe/Athens'";

        Client c2 = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp2 = c2.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp2.bufferEntity();

        assertThat(resp2.readEntity(String.class)).isEqualTo("{ \"text\": \"" + expectedResponse +
                "\" ,  \"thread\": { \"name\": \"spaces/" + "SPACE_ID" + "\" }}");
    }




}