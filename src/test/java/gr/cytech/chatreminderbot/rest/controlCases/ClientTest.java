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
import static org.mockito.Mockito.*;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    private Client client;
    private TypedQuery query;

    @BeforeEach
    public final void beforeEach() throws Exception {
        client = new Client();
        client.entityManager = mock(EntityManager.class);

        query = mock(TypedQuery.class);
        when(client.entityManager.createNamedQuery("get.configurationByKey", Configurations.class)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Configurations("default","localhost"));

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

        verify(query, times(1)).setParameter("configKey","buttonUrl");

        assertThat(result).as("unexpected result returned").isEqualTo("ok");

        assertThat(transport.getLowLevelHttpRequest().getUrl()).as("unexpected url in request")
                .isEqualTo("https://chat.googleapis.com/v1/spaces/" + reminder.getSpaceId() + "/messages");

        assertThat(transport.getLowLevelHttpRequest().getContentAsString())
                .as("unexpected content submitted for reminder")
                .isEqualTo(message);
    }

}