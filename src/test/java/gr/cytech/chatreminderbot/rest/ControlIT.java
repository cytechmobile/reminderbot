package gr.cytech.chatreminderbot.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import gr.cytech.chatreminderbot.rest.GoogleCards.CardResponseBuilder;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

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
        caseSetReminder.setBotName("reminder");

        //In order to use calculateRemainingTime need to define: timezone, when
        caseSetReminder.setWhen(expectedWhen);
        caseSetReminder.setTimeZone("Europe/Athens");

        String successMsg = "Reminder with text:\n <b>" + what
                + "</b>.\nSaved successfully and will notify you in: \n<b>"
                + caseSetReminder.calculateRemainingTime(caseSetReminder.dateForm()) + "</b>";

        Client c = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        assertThat(resp.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(resp.readEntity(String.class)).isEqualTo(expectedResponseMethod(successMsg));
    }

    @Test
    void reminderWithoutDateGiven() {
        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);

        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Date time = cal.getTime();

        String expectedDate = timeFormat.format(time) + " athens";
        String what = "something to do";

        mes.setText("remind me '" + what + "' at " + expectedDate);
        req.setMessage(mes);
        Control control = new Control();
        control.setRequest(req);

        CaseSetReminder caseSetReminder = new CaseSetReminder();
        caseSetReminder.setRequest(req);
        caseSetReminder.setBotName("reminder");

        //In order to use calculateRemainingTime need to define: timezone, when
        DateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        caseSetReminder.setWhen(dateTimeFormat.format(time));
        caseSetReminder.setTimeZone("Europe/Athens");

        String successMsg = "Reminder with text:\n <b>" + what
                + "</b>.\nSaved successfully and will notify you in: \n<b>"
                + caseSetReminder.calculateRemainingTime(caseSetReminder.dateForm()) + "</b>";

        Client c = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        assertThat(resp.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(resp.readEntity(String.class)).isEqualTo(expectedResponseMethod(successMsg));
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
        assertThat(resp.readEntity(String.class)).isNotEqualTo(expectedResponseMethod(responseDefault));
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
        assertThat(resp.readEntity(String.class)).isEqualTo(expectedResponseMethod(expectedResponse));

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

        String expectedResponse = "Use  quotation marks  `'` only two times. "
                + "One before and one after what, type Help for example.";

        Client c = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();

        assertThat(resp.readEntity(String.class)).isEqualTo(expectedResponseMethod(expectedResponse));
    }

    @Test
    void setMyTimezone() {
        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);

        mes.setText("set my timezone to athens");
        req.setMessage(mes);

        String expectedResponse = " <"
                + req.getMessage().getSender().getName()
                + "> successfully set your timezone at:Europe/Athens";

        Client c = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();

        assertThat(resp.readEntity(String.class)).isEqualTo(expectedResponseMethod(expectedResponse));
    }

    @Test
    void showVersion() throws Exception {
        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);

        mes.setText("@reminder version");
        req.setMessage(mes);

        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));

        String expectedResponse = "Hi my version right now is: " + properties.getProperty("version");

        Client c = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response resp = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();

        assertThat(resp.readEntity(String.class)).isEqualTo(expectedResponseMethod(expectedResponse));
    }

    @Test
    void setAndReturnTimezone() {
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        Message mes2 = new Message();
        Request req2 = new Request();
        mes2.setThread(threadM);
        mes2.setSender(sender);
        mes2.setText("@reminder set my timezone to athens");
        req2.setMessage(mes2);

        String expectedResponse = "---- Your timezone is  ---- \n"
                + "Timezone = 'Europe/Athens'\n "
                + "---- Default timezone is ---- \n"
                + "Timezone = 'Europe/Athens'";

        Client c = ClientBuilder.newBuilder().register(new JacksonJsonProvider(new ObjectMapper())).build();
        Response respForReq2 = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req2));
        respForReq2.bufferEntity();

        logger.info("makes sure that the response is 200(OK) to continue");
        assertThat(respForReq2.getStatus())
                .as("received error response when setting user time zone")
                .isEqualTo(200);

        Request req = new Request();
        Message mes = new Message();
        mes.setThread(threadM);
        mes.setSender(sender);
        mes.setText("@reminder timezones");
        req.setMessage(mes);

        Response resp = c.target("http://localhost:8080/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();

        assertThat(resp.readEntity(String.class)).as("Unexpected response when getting user time zone")
                .isEqualTo(expectedResponseMethod(expectedResponse));
    }

    String expectedResponseMethod(String expectedMessage) {
        return new CardResponseBuilder()
                .thread("spaces/SPACE_ID")
                .textParagraph("" + expectedMessage + "")
                .build();

    }
}
