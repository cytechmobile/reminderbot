package gr.cytech.chatreminderbot.rest;


import com.google.api.client.http.*;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {
    private final static Logger logger = LoggerFactory.getLogger(ClientTest.class.getName());

    @Mocked
    HttpRequest request;
    @Mocked
    HttpRequestFactory requestFactory;

    Client client;


    @BeforeEach
    public final void beforeEach() throws Exception {
        client = new Client();
    }

    @Test
    public void sendAsTest() throws Exception {
        final Reminder reminder = new Reminder("'what'", LocalDateTime.now().plusMinutes(10),
                "DisplayName", "uPWJ7AAAAAE", "1E_d3mjJGyM");


        //Expectations
        String message = "{ \"text\":\"" + "<" + reminder.getSenderDisplayName() + "> " + reminder.getWhat()
                + " \" ,  \"thread\": { \"name\": \"spaces/" + reminder.getSpaceId()
                + "/threads/" + reminder.getThreadId() + "\" }}";

        HttpContent content2 = new ByteArrayContent("application/json", message.getBytes("UTF-8"));

        URI uri = URI.create("https://chat.googleapis.com/v1/spaces/" + reminder.getSpaceId() + "/messages");
        GenericUrl url2 = new GenericUrl(uri);


        new Expectations() {{
            requestFactory.buildPostRequest((GenericUrl) any, (HttpContent) any);
            result = new Delegate<HttpRequest>() {
                public HttpRequest buildPostRequest(GenericUrl url, HttpContent content) throws IOException {

                    assertThat(url).isEqualTo(url2);
                    //Cant compare 2 httpContent?
                    assertThat(content.getLength()).isEqualTo(content2.getLength());
                    return request;
                }
            };
        }};

        client.sendAsyncResponse(reminder);

        new Verifications() {{
            request.execute();
            times = 1;
        }};
    }

    @Test
    public void getListOfMembersInRoom() {

        Client client = new Client();

        Reminder reminder = new Reminder("trololo", LocalDateTime.now().plusMinutes(10),
                "DisplayName", "uPWJ7AAAAAE", "pbH-zOAtr8E");
        client.sendAsyncResponse(reminder);
        // client.getListOfMembersInRoom("AAAADvB8eGY");
    }
}
