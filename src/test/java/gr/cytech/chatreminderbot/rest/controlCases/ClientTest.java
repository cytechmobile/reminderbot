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
        final Reminder reminder = new Reminder("'what'", ZonedDateTime.now().plusMinutes(10),
                "DisplayName", spaceId, threadId);

        //Expectations
        final String message = "{ \"text\":\"" + "<" + reminder.getSenderDisplayName() + "> " + reminder.getWhat()
                + " \" ,  \"thread\": { \"name\": \"spaces/" + reminder.getSpaceId()
                + "/threads/" + reminder.getThreadId() + "\" }}";

        MockHttpTransport transport = new MockHttpTransport.Builder()
                .setLowLevelHttpResponse(new MockLowLevelHttpResponse()
                        .setContent("ok")
                        .setStatusCode(200))
                .build();

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
