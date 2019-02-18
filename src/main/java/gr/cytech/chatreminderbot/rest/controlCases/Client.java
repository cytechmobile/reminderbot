package gr.cytech.chatreminderbot.rest.controlCases;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client {
    private static final Logger logger = LoggerFactory
            .getLogger(Client.class.getName());

    private static final List<String> SCOPE = Arrays.asList("https://www.googleapis.com/auth/chat.bot");
    private static final String KEY_FILE_PATH_ENV = "BOT_KEY_FILE_PATH";

    public void sendAsyncResponse(Reminder reminder) {
        //URL request - responses to current thread
        URI uri = URI.create("https://chat.googleapis.com/v1/spaces/" + reminder.getSpaceId() + "/messages");
        GenericUrl url = new GenericUrl(uri);

        //Construct string in json format
        String message = "{ \"text\":\"" + "<" + reminder.getSenderDisplayName() + "> " + reminder.getWhat()
                + " \" ,  \"thread\": { \"name\": \"spaces/" + reminder.getSpaceId()
                + "/threads/" + reminder.getThreadId() + "\" }}";

        //Check if message is to be sent to a room ex:reminder #TestRoom
        if (reminder.getSenderDisplayName().startsWith("#")) {

            String spaceID = getListOfSpacesBotBelongs()
                    .getOrDefault(reminder.getSenderDisplayName().substring(1),
                            reminder.getSpaceId());

            String messageToRoom = "{ \"text\":\"" + "<" + "users/all" + "> " + reminder.getWhat() + "\" }";

            URI uri2 = URI.create("https://chat.googleapis.com/v1/spaces/" + spaceID + "/messages");
            GenericUrl url2 = new GenericUrl(uri2);

            send(url2, messageToRoom, "POST");
        } else {
            send(url, message, "POST");
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

    private String send(GenericUrl url, String message, String httpMethod) {
        String response = "";

        GoogleCredential credential = null;
        String keyFilePath = getBotKeyFilePath();
        try {
            credential = GoogleCredential
                    .fromStream(new FileInputStream(keyFilePath))
                    .createScoped(SCOPE);
        } catch (IOException e) {
            logger.error("Error creating GoogleCredential using key file:{}", keyFilePath, e);
        }

        HttpTransport httpTransport = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            logger.error("Error -GeneralSecurityException- creating httpTransport ", e);
        } catch (IOException e) {
            logger.error("Error -IOException- creating httpTransport ", e);
        }

        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);

        HttpContent content = null;
        content = new ByteArrayContent("application/json", message.getBytes(StandardCharsets.UTF_8));

        HttpRequest request;

        if (httpMethod.equals("POST")) {
            try {
                request = requestFactory.buildPostRequest(url, content);
                request.execute();
            } catch (IOException e) {
                logger.error("Error creating request using url: {}", url, e);
            }
            return "";
        } else {
            HttpResponse httpResponse;
            try {
                request = requestFactory.buildGetRequest(url);
                httpResponse = request.execute();
                response += httpResponse.parseAsString();
            } catch (IOException e) {
                logger.error("Error creating request using url: {}", url, e);
            }
            return response;
        }
    }

    private String getBotKeyFilePath() {
        return System.getProperty(KEY_FILE_PATH_ENV,
                System.getenv()
                        .getOrDefault(KEY_FILE_PATH_ENV, "./botnotifier-key.json"));
    }
}
