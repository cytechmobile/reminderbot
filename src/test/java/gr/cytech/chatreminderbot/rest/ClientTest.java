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


    //Send Card function. Not Used for now it but might needed in future
   /* public void sendCard() throws IOException, GeneralSecurityException {

        String what = "To go for a walk";
        String card = "{" +
                "  \"actionResponse\":{" +
                "    \"type\":\"NEW_MESSAGE\"" +
                "  }," +
                "  \"cards\": [" +
                "    {" +
                "      \"sections\": [" +
                "        {" +
                "          \"widgets\": [" +
                "            {" +
                "              \"keyValue\": {" +
                "                \"topLabel\": \"- Reminder -\"," +
                "                \"content\": \" " + what + " \"," +
                "                \"contentMultiline\": \"false\"," +
                "                \"bottomLabel\": \"Press me for Snooze\"," +
                "                \"onClick\": {" +
                "                  \"action\": {" +
                "                  \"actionMethodName\": \"snooze\"," +
                "                   \"parameters\": [" +
                "                   {" +
                "                   \"key\": \"time\"," +
                "                   \"value\": \"1 day\"" +
                "                     }," +
                "                      {" +
                "                    \"key\": \"id\"," +
                "                    \"value\": \"123456\"" +
                "                            }" +
                "                         ]" +
                "                      }" +
                "                  }," +
                "                \"icon\": \"CLOCK\"," +
                "                \"button\": {" +
                "                  \"textButton\": {" +
                "                    \"text\": \"I seen reminder delete it now \"," +
                "                    \"onClick\": {" +
                "                        \"action\": {" +
                "                           \"actionMethodName\": \"delete\"," +
                "                             \"parameters\": [" +
                "                                {" +
                "                             \"key\": \"time\"," +
                "                            \"value\": \"1 day\"" +
                "                                     }" +
                "                                   ]" +
                "                                }" +
                "                    }" +
                "                  }" +
                "                }" +
                "              }" +
                "            }" +
                "          ]" +
                "        }" +
                "      ]" +
                "    }" +
                "  ]" +
                "}";


        //Custom ids
        String space_id = "AAAA2f1t66I";
        String thread_id = "fguBUDlHUYE";

        URI uri = URI.create("https://chat.googleapis.com/v1/spaces/" + space_id + "/messages?threadKey=LXWgtWvqyWU");
        GenericUrl url = new GenericUrl(uri);
        GoogleCredential credential = GoogleCredential
                .fromStream(new FileInputStream(BOT_KEY_FILE_PATH))
                .createScoped(SCOPE);

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);

        HttpContent content =
                new ByteArrayContent("application/json", card.getBytes("UTF-8"));
        com.google.api.client.http.HttpRequest request = requestFactory.buildPostRequest(url, content);
        request.execute();

    }
*/
}
