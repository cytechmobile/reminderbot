package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.GoogleCards.CardResponseBuilder;
import gr.cytech.chatreminderbot.rest.message.Message;
import gr.cytech.chatreminderbot.rest.message.Request;
import gr.cytech.chatreminderbot.rest.message.Sender;
import gr.cytech.chatreminderbot.rest.message.ThreadM;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ControlIT {
    private static final Logger logger = LoggerFactory.getLogger(ControlIT.class);

    private static Client client;

    @BeforeAll
    public static void beforeAll() throws Exception {
        client = ClientBuilder.newBuilder().build();

    }

    @AfterAll
    public static void afterAll() throws Exception {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Test
    void handleRequest() throws Exception {
        Request req = getSampleRequest();

        String expectedWhen = "12/12/2019 12:00";
        String what = "something to do";

        req.getMessage().setText("remind me " + what + " at " + expectedWhen);

        //In order to use calculateRemainingTime need to define: timezone, when

        String successMsg = "Reminder with text:\n <b>" + what
                + "</b>.\nSaved successfully and will notify you in: \n<b>"
                + "12/12/2019 12:00" + "</b>";

        Response resp = client.target("http://localhost:8081/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        assertThat(resp.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(resp.readEntity(String.class)).isEqualTo(expectedResponseMethod(successMsg));
    }

    @Test
    void listReminderTest() {
        Request req = getSampleRequest();
        req.getMessage().setText("list");

        String responseDefault = "I didnt understand you, type help for instructions \n";

        Response resp = client.target("http://localhost:8081/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        //Verify that i didn't get the default wrong message
        assertThat(resp.readEntity(String.class)).isNotEqualTo(expectedResponseMethod(responseDefault));
    }

    @Test
    void setGlobalTimezoneTest() {
        Request req = getSampleRequest();
        req.getMessage().setText("set global timezone to athens");

        String expectedResponse = "You successfully set the global timezone at:Europe/Athens";

        Response resp = client.target("http://localhost:8081/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        //Verify that i didn't get the default wrong message
        assertThat(resp.readEntity(String.class)).isEqualTo(expectedResponseMethod(expectedResponse));

    }

    @Test
    void setMyTimezone() {
        Request req = getSampleRequest();
        req.getMessage().setText("set my timezone to athens");

        String expectedResponse = " <"
                + req.getMessage().getSender().getName()
                + "> successfully set your timezone at:Europe/Athens";

        Response resp = client.target("http://localhost:8081/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();

        assertThat(resp.readEntity(String.class)).isEqualTo(expectedResponseMethod(expectedResponse));
    }

    @Test
    void showVersion() throws Exception {
        Request req = getSampleRequest();
        req.getMessage().setText("version");

        final Properties properties = new Properties();
        properties.load(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResourceAsStream("application.properties")));

        String expectedResponse = "Hi my version right now is: " + properties.getProperty("version");

        Response resp = client.target("http://localhost:8081/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        assertThat(properties.getProperty("version")).isNotEqualTo("${project.version}");
        assertThat(resp.readEntity(String.class)).isEqualTo(expectedResponseMethod(expectedResponse));
    }

    @Test
    void setAndReturnTimezone() {
        Request req2 = getSampleRequest();
        req2.getMessage().setText("set my timezone to athens");

        String expectedResponse = "---- Your timezone is  ---- \n"
                + "Timezone = Europe/Athens\n "
                + "---- Default timezone is ---- \n"
                + "Timezone = Europe/Athens";

        Response respForReq2 = client.target("http://localhost:8081/bot/services/handleReq")
                .request()
                .post(Entity.json(req2));
        respForReq2.bufferEntity();

        logger.info("makes sure that the response is 200(OK) to continue");
        assertThat(respForReq2.getStatus())
                .as("received error response when setting user time zone")
                .isEqualTo(200);

        Request req = getSampleRequest();
        req.getMessage().setText("timezones");

        Response resp = client.target("http://localhost:8081/bot/services/handleReq")
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

    public static Request getSampleRequest() {
        Message mes = new Message();

        Sender sender = new Sender();
        sender.setName("MyName");
        mes.setSender(sender);

        ThreadM threadM = new ThreadM();
        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        mes.setThread(threadM);

        Request req = new Request();
        req.setMessage(mes);

        return req;
    }

}
