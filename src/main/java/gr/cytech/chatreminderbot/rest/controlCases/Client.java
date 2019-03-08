package gr.cytech.chatreminderbot.rest.controlCases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.api.client.http.*;
import gr.cytech.chatreminderbot.rest.GoogleCards.CardResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static final List<String> SCOPE = Collections.singletonList("https://www.googleapis.com/auth/chat.bot");
    private static final String KEY_FILE_PATH_ENV = "BOT_KEY_FILE_PATH";

    @Inject
    protected HttpRequestFactory requestFactory;

    public String sendAsyncResponse(Reminder reminder) {
        //URL request - responses to current thread
        URI uri = URI.create("https://chat.googleapis.com/v1/spaces/" + reminder.getSpaceId() + "/messages");
        GenericUrl url = new GenericUrl(uri);

        //Construct string in json format
        String message = "{ \"text\":\"" + "<" + reminder.getSenderDisplayName() + "> \" "
                + ",  \"thread\": { \"name\": \"spaces/" + reminder.getSpaceId()
                + "/threads/" + reminder.getThreadId() + "\" }}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Object response = new CardResponseBuilder()
                .thread("spaces/" + reminder.getSpaceId() + "/threads/" + reminder.getThreadId())
                .textParagraph(reminder.getWhat())
                .textButton("remind me again in 10", "https://users.cytech.gr/~pavlos/pavlos.php?http://pegasus.cytech.gr:8080/bot/services/button?name=" + reminder.getSenderDisplayName() + "&text=" + reminder.getWhat() + "&timezone=" + reminder.getReminderTimezone() + "&space=" + reminder.getSpaceId() + "&thread=" + reminder.getThreadId())
                .build();

        String cardResponse;
        try {
            cardResponse = mapper.writeValueAsString(response);
        } catch (Exception e) {
            return "Internal server error";
        }

        //Check if message is to be sent to a room ex:reminder #TestRoom
        if (reminder.getSenderDisplayName().startsWith("#")) {

            String spaceID = getListOfSpacesBotBelongs()
                    .getOrDefault(reminder.getSenderDisplayName().substring(1),
                            reminder.getSpaceId());

            String messageToRoom = "{ \"text\":\"" + "<users/all> " + reminder.getWhat() + "\" }";

            URI uri2 = URI.create("https://chat.googleapis.com/v1/spaces/" + spaceID + "/messages");
            GenericUrl url2 = new GenericUrl(uri2);
            return send(url2, messageToRoom, "POST");
        } else {
            //            return send(url, cardResponse, "POST");
            return send(url,message,"POST") + send(url, cardResponse, "POST");
        }

    }

    //request to get members of a room
    public Map<String, String> getListOfMembersInRoom(String spaceId) {

        URI uri = URI.create("https://chat.googleapis.com/v1/spaces/" + spaceId + "/members");
        GenericUrl url = new GenericUrl(uri);
        String emptyBodyMessage = "";
        //key=displayName Value:user/id
        Map<String, String> users = new HashMap<>();
        String[] splited = send(url, emptyBodyMessage, "GET").split("\"");
        for (int i = 0; i < splited.length; i++) {
            if (splited[i].equals("displayName")) {
                users.put(splited[i + 2], splited[i - 2]);
            }
        }
        return users;
    }

    Map<String, String> getListOfSpacesBotBelongs() {
        URI uri = URI.create("https://chat.googleapis.com/v1/spaces");
        GenericUrl url = new GenericUrl(uri);
        String emptyBodyMessage = "";
        String response = send(url, emptyBodyMessage, "GET");
        String[] results = response.split("\"");
        //key=displayNameOfRoom Value:spaceID
        Map<String, String> spaces = new HashMap<>();
        for (int i = 0; i < results.length; i++) {
            if (results[i].equals("displayName") && !(results[i + 2].equals(""))) {
                spaces.put(results[i + 2], results[i - 6].split("/")[1]);
            }
        }
        return spaces;
    }

    public String send(GenericUrl url, String message, String httpMethod) {
        HttpContent content = new ByteArrayContent("application/json",
                message.getBytes(StandardCharsets.UTF_8));

        HttpRequest request;
        try {
            if (httpMethod.equals("POST")) {
                request = requestFactory.buildPostRequest(url, content);
            } else {
                request = requestFactory.buildGetRequest(url);
            }
        } catch (Exception e) {
            logger.error("Error creating request using url: {}", url, e);
            return null;
        }

        String response = "";
        try {
            HttpResponse httpResponse = request.execute();
            response = httpResponse.parseAsString();
        } catch (IOException e) {
            logger.error("Error creating request using url: {}", url, e);
        }

        return response;
    }
}
