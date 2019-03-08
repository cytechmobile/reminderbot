package gr.cytech.chatreminderbot.rest.controlCases;

import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    private Client client;

    @BeforeEach
    public final void beforeEach() throws Exception {
        client = new Client();
    }

    @Test
    public void sendAsTest() throws Exception {
        String threadId = "THREAD_ID";
        String spaceId = "SPACE_ID";
        Reminder reminder = new Reminder("'what'", ZonedDateTime.now().plusMinutes(10),
                "DisplayName", "Europe/Athens", spaceId, threadId);
        MockHttpTransport transport = new MockHttpTransport.Builder()
                .setLowLevelHttpResponse(new MockLowLevelHttpResponse()
                        .setContent("ok")
                        .setStatusCode(200))
                .build();

        //Expectations
//        final String message = "{ \"text\":\"" + "<" + reminder.getSenderDisplayName() + "> " + reminder.getWhat()
//                + " \" ,  \"thread\": { \"name\": \"spaces/" + reminder.getSpaceId()
//                + "/threads/" + reminder.getThreadId() + "\" }}";
        final String message = "{\n"
                + "  \"cards\" : [ {\n"
                + "    \"sections\" : [ {\n"
                + "      \"widgets\" : [ {\n"
                + "        \"textParagraph\" : {\n"
                + "          \"text\" : \"" + reminder.getWhat() + "\"\n"
                + "        }\n"
                + "      }, {\n"
                + "        \"buttons\" : [ {\n"
                + "          \"textButton\" : {\n"
                + "            \"onClick\" : {\n"
                + "              \"openLink\" : {\n"
                + "                \"url\" : \"https://users.cytech.gr/~pavlos/pavlos.php?http://pegasus.cytech.gr:8080/bot/services/button?name=" + reminder.getSenderDisplayName() + "&text=" + reminder.getWhat() + "&timezone=" + reminder.getReminderTimezone() + "&space=" + reminder.getSpaceId() + "&thread=" + reminder.getThreadId() + "\"\n"
                + "              }\n"
                + "            },\n"
                + "            \"text\" : \"remind me again in 10\"\n"
                + "          }\n"
                + "        } ]\n"
                + "      } ]\n"
                + "    } ]\n"
                + "  } ],\n"
                + "  \"thread\" : {\n"
                + "    \"name\" : \"spaces/SPACE_ID/threads/THREAD_ID\"\n"
                + "  }\n"
                + "}";
        client.requestFactory = transport.createRequestFactory();

        String result = client.sendAsyncResponse(reminder);

        assertThat(result).as("unexpected result returned").isEqualTo("ok");

        assertThat(transport.getLowLevelHttpRequest().getUrl()).as("unexpected url in request")
                .isEqualTo("https://chat.googleapis.com/v1/spaces/" + reminder.getSpaceId() + "/messages");

        assertThat(transport.getLowLevelHttpRequest().getContentAsString())
                .as("unexpected content submitted for reminder")
                .isEqualTo(message);
    }

}
