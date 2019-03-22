package gr.cytech.chatreminderbot.rest.controlCases;

import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    private Client client;

    @BeforeEach
    public final void beforeEach() throws Exception {
        client = new Client();
        client.entityManager = mock(EntityManager.class);

        TypedQuery query = mock(TypedQuery.class);
        when(client.entityManager.createNamedQuery("get.default", ButtonUrl.class)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new ButtonUrl("localhost"));

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

        String message = client.cardCreation(reminder.getSpaceId(), reminder.getThreadId(), reminder.getWhat(),
                reminder.getSenderDisplayName(), reminder.getReminderTimezone(), "localhost");

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