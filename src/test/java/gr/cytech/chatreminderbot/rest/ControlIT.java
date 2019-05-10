package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.GoogleCards.CardResponseBuilder;
import gr.cytech.chatreminderbot.rest.message.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class ControlIT {
    private static final Logger logger = LoggerFactory.getLogger(ControlIT.class);

    private static Client client;
    private static final String REGEX_TO_REMINDER_ID = "\"value\":\"(\\d+)\"";

    private static final String ClientUrl = System.getenv()
            .getOrDefault("APP_HOST", "localhost") + ":8080";

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
    void buildReminder() throws Exception {
        Request req = getSampleRequest();

        String expectedWhen = "12/12/2019 12:00";
        String what = "something to do";

        req.getMessage().setText("remind me " + what + " at " + expectedWhen);

        //In order to use calculateRemainingTime need to define: timezone, when

        String successMsg = "Reminder with text:\n<b>" + what
                + "</b>.\nSaved successfully and will notify you in: \n<b>"
                + "12/12/2019 12:00" + "</b>";

        Response resp = client.target("http://" + ClientUrl + "/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        assertThat(resp.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String requestResponse = resp.readEntity(String.class);
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile(REGEX_TO_REMINDER_ID).matcher(requestResponse);
        while (m.find()) {
            matches.add(m.group(1));
        }
        assertThat(requestResponse).isEqualTo(expectedResponseMethodForReminder(successMsg, matches.get(0)));

    }

    @Test
    void buildReminderAndCancel() throws Exception {
        Request req = getSampleRequest();
        String expectedWhen = "12/12/2019 12:00";
        String what = "something to do";

        req.getMessage().setText("remind me " + what + " at " + expectedWhen);

        //In order to use calculateRemainingTime need to define: timezone, when

        String successMsg = "Reminder with text:\n<b>" + what
                + "</b>.\nSaved successfully and will notify you in: \n<b>"
                + "12/12/2019 12:00" + "</b>";

        Response resp = client.target("http://" + ClientUrl + "/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        assertThat(resp.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String requestResponse = resp.readEntity(String.class);
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile(REGEX_TO_REMINDER_ID).matcher(requestResponse);
        while (m.find()) {
            matches.add(m.group(1));
        }
        assertThat(requestResponse).isEqualTo(expectedResponseMethodForReminder(successMsg, matches.get(0)));

        Action action = new Action();
        action.setActionMethodName("CancelReminder");
        Map<String, String> mapForReminderId = Map.of(
                "key", "reminderId",
                "value", matches.get(0)
        );
        List<Map<String, String>> parameters = List.of(mapForReminderId);

        action.setParameters(parameters);
        Request request = getSampleRequest();
        request.setAction(action);
        Response response = client.target("http://" + ClientUrl + "/bot/services/handleReq")
                .request()
                .post(Entity.json(request));
        response.bufferEntity();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String requestResponseWhenDeletingReminder = response.readEntity(String.class);
        String successDeleteMessage = "Reminder with text:\n<b>" + what + "</b>\nsuccessfully canceled!";
        assertThat(requestResponseWhenDeletingReminder)
                .isEqualTo(expectedResponseMethodWithAction(successDeleteMessage));

    }

    @Test
    void buildReminderWithAction() {
        Request req = getSampleRequest();
        Action action = new Action();
        String what = "something to do";
        Map<String, String> mapForText = Map.of(
                "key", "text",
                "value", what
        );
        List<Map<String, String>> parameters = List.of(mapForText);

        action.setActionMethodName("remindAgainTomorrow");

        action.setParameters(parameters);
        req.getMessage().setText(what);
        req.setAction(action);

        //In order to use calculateRemainingTime need to define: timezone, when

        String successMsg = "Reminder with text:\n<b>" + what
                + "</b>.\nSaved successfully and will notify you in: \n<b>"
                + "tomorrow" + "</b>";

        Response resp = client.target("http://" + ClientUrl + "/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        assertThat(resp.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String requestResponse = resp.readEntity(String.class);
        assertThat(requestResponse).isEqualTo(expectedResponseMethodForActions(successMsg));
    }

    @Test
    void buildReminderAndPreventOtherUserFromCancel() {
        Request req = getSampleRequest();

        String expectedWhen = "12/12/2019 12:00";
        String what = "something to do";

        req.getMessage().setText("remind me " + what + " at " + expectedWhen);

        //In order to use calculateRemainingTime need to define: timezone, when

        String successMsg = "Reminder with text:\n<b>" + what
                + "</b>.\nSaved successfully and will notify you in: \n<b>"
                + "12/12/2019 12:00" + "</b>";

        Response resp = client.target("http://" + ClientUrl + "/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        assertThat(resp.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String requestResponse = resp.readEntity(String.class);
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile(REGEX_TO_REMINDER_ID).matcher(requestResponse);
        while (m.find()) {
            matches.add(m.group(1));
        }
        assertThat(requestResponse).isEqualTo(expectedResponseMethodForReminder(successMsg, matches.get(0)));

        Map<String, String> mapForText = Map.of(
                "key", "text",
                "value", what
        );
        Map<String,String> mapForReminderId = Map.of(
                "key","reminderId",
                "value",matches.get(0)
        );
        List<Map<String, String>> parameters = List.of(mapForReminderId, mapForText);

        Action action = new Action();
        action.setActionMethodName("CancelReminder");
        action.setParameters(parameters);
        User user = new User();
        user.setName("DifferentName");
        user.setDisplayName("DifferentName");
        Request request = getSampleRequest();
        request.setUser(user);
        request.setAction(action);
        Response response = client.target("http://" + ClientUrl + "/bot/services/handleReq")
                .request()
                .post(Entity.json(request));
        response.bufferEntity();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        requestResponse = response.readEntity(String.class);
        successMsg = "Couldn't find the reminder or maybe you don't own this reminder";
        assertThat(requestResponse).isEqualTo(expectedResponseMethod(successMsg));

    }

    @Test
    void buildReminderAndPreventOtherUserFromPostpone() {
        Request req = getSampleRequest();

        String expectedWhen = "12/12/2019 12:00";
        String what = "something to do";

        req.getMessage().setText("remind me " + what + " at " + expectedWhen);

        //In order to use calculateRemainingTime need to define: timezone, when

        String successMsg = "Reminder with text:\n<b>" + what
                + "</b>.\nSaved successfully and will notify you in: \n<b>"
                + "12/12/2019 12:00" + "</b>";

        Response resp = client.target("http://" + ClientUrl + "/bot/services/handleReq")
                .request()
                .post(Entity.json(req));
        resp.bufferEntity();
        assertThat(resp.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String requestResponse = resp.readEntity(String.class);
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile(REGEX_TO_REMINDER_ID).matcher(requestResponse);
        while (m.find()) {
            matches.add(m.group(1));
        }
        assertThat(requestResponse).isEqualTo(expectedResponseMethodForReminder(successMsg, matches.get(0)));

        Map<String, String> mapForUser = Map.of(
                "key", "name",
                "value", req.getMessage().getSender().getName()
        );
        Map<String, String> mapForText = Map.of(
                "key", "text",
                "value", what
        );
        Map<String, String> mapForReminderId = Map.of(
                "key", "reminderId",
                "value", matches.get(0)
        );

        List<Map<String, String>> parameters = List.of(mapForUser, mapForReminderId, mapForText);

        Action action = new Action();
        action.setActionMethodName("remindAgainTomorrow");
        action.setParameters(parameters);
        User user = new User();
        user.setName("DifferentName");
        user.setDisplayName("DifferentName");
        Request request = getSampleRequest();
        request.setUser(user);
        request.setAction(action);
        request.getMessage().getSender().setName("MyName");
        Response response = client.target("http://" + ClientUrl + "/bot/services/handleReq")
                .request()
                .post(Entity.json(request));
        response.bufferEntity();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        requestResponse = response.readEntity(String.class);
        successMsg = "You <b>can't</b> postpone another user's reminders.";
        assertThat(requestResponse).isEqualTo(expectedResponseMethod(successMsg));

    }

    @Test
    void listReminderTest() {
        Request req = getSampleRequest();
        req.getMessage().setText("list");

        String responseDefault = "I didnt understand you, type help for instructions \n";

        Response resp = client.target("http://" + ClientUrl + "/bot/services/handleReq")
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

        Response resp = client.target("http://" + ClientUrl + "/bot/services/handleReq")
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

        Response resp = client.target("http://" + ClientUrl + "/bot/services/handleReq")
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

        Response resp = client.target("http://" + ClientUrl + "/bot/services/handleReq")
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

        Response respForReq2 = client.target("http://" + ClientUrl + "/bot/services/handleReq")
                .request()
                .post(Entity.json(req2));
        respForReq2.bufferEntity();

        logger.info("makes sure that the response is 200(OK) to continue");
        assertThat(respForReq2.getStatus())
                .as("received error response when setting user time zone")
                .isEqualTo(200);

        Request req = getSampleRequest();
        req.getMessage().setText("timezones");

        Response resp = client.target("http://" + ClientUrl + "/bot/services/handleReq")
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

    String expectedResponseMethodWithAction(String expectedMessage) {
        return new CardResponseBuilder("UPDATE_MESSAGE")
                .thread("spaces/SPACE_ID")
                .textParagraph("" + expectedMessage + "")
                .build();
    }

    String expectedResponseMethodForActions(String expectedMessage) {
        return new CardResponseBuilder("UPDATE_MESSAGE")
                .thread("spaces/SPACE_ID/threads/THREAD_ID")
                .textParagraph("" + expectedMessage + "")
                .textParagraph("Reminder have been postponed!.")
                .build();

    }

    String expectedResponseMethodForReminder(String expectedMessage, String number) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("reminderId", number);
        parameters.put("name", "MyName");
        return new CardResponseBuilder()
                .thread("spaces/SPACE_ID/threads/THREAD_ID")
                .textParagraph(expectedMessage)
                .interactiveTextButton("Cancel Reminder", "CancelReminder", parameters)
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

        User user = new User();
        user.setDisplayName("MyName");
        user.setName("MyName");
        Request req = new Request();
        req.setUser(user);
        req.setMessage(mes);

        return req;
    }

}
