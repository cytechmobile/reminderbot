package gr.cytech.chatreminderbot.rest;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Client {
    private final static Logger logger = LoggerFactory.getLogger(Client.class.getName());

    private final static List<String> SCOPE = Arrays.asList("https://www.googleapis.com/auth/chat.bot");
    public static final String KEY_FILE_PATH_ENV = "BOT_KEY_FILE_PATH";

    public void sendAsyncResponse(Reminder reminder) {

        //URL request - responses to current thread
        URI uri = URI.create("https://chat.googleapis.com/v1/spaces/" + reminder.getSpaceId() + "/messages");
        GenericUrl url = new GenericUrl(uri);

        //Construct string in json format
        String message = "{ \"text\":\"" + "<" + reminder.getSenderDisplayName() + "> " + reminder.getWhat()
                + " \" ,  \"thread\": { \"name\": \"spaces/" + reminder.getSpaceId()
                + "/threads/" + reminder.getThreadId() + "\" }}";

        //Check if message is to be sent to a room ex:reminder #TestRoom
        if(reminder.getSenderDisplayName().startsWith("#")){

            String spaceID = (String) getListOfSpacesBotBelongs().
                    getOrDefault(reminder.getSenderDisplayName().substring(1),
                            reminder.getSpaceId());

            String messageToRoom = "{ \"text\":\"" + "<" + "users/all" + "> " + reminder.getWhat()+"\" }";

            URI uri2 = URI.create("https://chat.googleapis.com/v1/spaces/" + spaceID+ "/messages");
            GenericUrl url2 = new GenericUrl(uri2);

            send(url2, messageToRoom, "POST");
        }
        else {
            send(url, message, "POST");
        }

    }


    //request to get members of a room
    public HashMap getListOfMembersInRoom(String spaceId) {
        URI uri = URI.create("https://chat.googleapis.com/v1/spaces/" + spaceId + "/members");
        GenericUrl url = new GenericUrl(uri);
        String emptyBodyMessage = "";
        //key=displayName Value:user/id
        HashMap<String, String> users = new HashMap<>();
        String[] splited = send(url, emptyBodyMessage, "GET").split("\"");
        for (int i = 0; i < splited.length; i++) {
            if (splited[i].equals("displayName")) {
                users.put(splited[i + 2], splited[i - 2]);
            }
        }
        return users;
    }

    public HashMap getListOfSpacesBotBelongs(){
        URI uri = URI.create("https://chat.googleapis.com/v1/spaces");
        GenericUrl url = new GenericUrl(uri);
        String emptyBodyMessage = "";
        String response = send(url, emptyBodyMessage, "GET");
        String[] results = response.split("\"");
        //key=displayNameOfRoom Value:spaceID
        HashMap<String, String> spaces = new HashMap<>();
        for (int i = 0; i < results.length; i++) {
            if (results[i].equals("displayName") && !(results[i+2].equals("")) ) {
                spaces.put(results[i+2],results[i-6].split("/")[1]);
            }
        }
        return spaces;
    }

    public String send(GenericUrl url, String message, String HttpMethod) {
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
        try {
            content = new ByteArrayContent("application/json", message.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("Error creating content from ByteArrayContent using  String message:{}", message, e);
        }

        HttpRequest request;

        if (HttpMethod.equals("POST")) {
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

    public String getBotKeyFilePath() {
        return System.getProperty(KEY_FILE_PATH_ENV,
                System.getenv().
                        getOrDefault(KEY_FILE_PATH_ENV, "./botnotifier-key.json"));
    }
}
