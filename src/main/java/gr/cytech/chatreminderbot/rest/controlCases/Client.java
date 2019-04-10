package gr.cytech.chatreminderbot.rest.controlCases;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import gr.cytech.chatreminderbot.rest.GoogleCards.CardResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final List<String> SCOPE = Collections.singletonList("https://www.googleapis.com/auth/chat.bot");

    public EntityManager entityManager;
    protected static GoogleCredential credential;
    protected static HttpTransport httpTransport;
    protected static HttpRequestFactory requestFactory;

    public String cardCreation(String spaceId, String threadId, String what,
                               String senderName, String timezone, String url) {

        return new CardResponseBuilder()
                .thread("spaces/" + spaceId + "/threads/" + threadId)
                .textParagraph("<b>" + what + "</b>")
                .textButton("remind me again in 10 minutes", url
                        + "/bot/services/button?name=" + senderName
                        + "&text=" + what
                        + "&timezone=" + timezone
                        + "&space=" + spaceId
                        + "&thread=" + threadId)
                .build();
    }

    public Client() {
    }

    public Client(EntityManager entityManager, HttpRequestFactory requestFactory) {
        this.entityManager = entityManager;
        Client.requestFactory = requestFactory;
    }

    public String sendAsyncResponse(Reminder reminder) {
        //URL request - responses to current thread
        URI uri = URI.create("https://chat.googleapis.com/v1/spaces/" + reminder.getSpaceId() + "/messages");
        GenericUrl url = new GenericUrl(uri);

        //Construct string in json format
        String message = "{ \"text\":\"" + "<" + reminder.getSenderDisplayName() + "> \" "
                + ",  \"thread\": { \"name\": \"spaces/" + reminder.getSpaceId()
                + "/threads/" + reminder.getThreadId() + "\" }}";

        String buttonUrl = entityManager.createNamedQuery("get.configurationByKey", Configurations.class)
                .setParameter("configKey","buttonUrl")
                .getSingleResult().getValue();

        String cardResponse = cardCreation(reminder.getSpaceId(), reminder.getThreadId(),
                reminder.getWhat(), reminder.getSenderDisplayName(),
                reminder.getReminderTimezone(), buttonUrl);

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
        String[] split = send(url, emptyBodyMessage, "GET").split("\"");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("displayName")) {
                users.put(split[i + 2], split[i - 2]);
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
                request = getHttpRequestFactory().buildPostRequest(url, content);
            } else {
                request = getHttpRequestFactory().buildGetRequest(url);
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

    public static Client newClient(EntityManager entityManager) {
        return new Client(entityManager, requestFactory);
    }

    public String googlePrivateKey() {
        Configurations getGooglePrivateKey = entityManager
                .createNamedQuery("get.configurationByKey", Configurations.class)
                .setParameter("configKey", "googlePrivateKey")
                .getSingleResult();
        if (!getGooglePrivateKey.getKey().equals("")) {
            return getGooglePrivateKey.getValue();
        } else {
            Configurations configurations = new Configurations("googlePrivateKey", "");
            entityManager.persist(configurations);
            return configurations.getValue();
        }
    }

    @Produces
    protected GoogleCredential getCredential() {
        if (credential == null) {
            String keyFilePath = googlePrivateKey();
            try {
                InputStream inputStream = new ByteArrayInputStream(keyFilePath.getBytes(StandardCharsets.UTF_8));
                credential = GoogleCredential
                        .fromStream(inputStream)
                        .createScoped(SCOPE);
            } catch (IOException e) {
                logger.error("Error creating GoogleCredential using key file:{}", keyFilePath, e);
            }
        }

        return credential;
    }

    @Produces
    public HttpRequestFactory getHttpRequestFactory() {
        if (requestFactory == null) {
            requestFactory = getHttpTransport().createRequestFactory(getCredential());
        }
        return requestFactory;
    }

    @Produces
    protected HttpTransport getHttpTransport() {
        if (httpTransport != null) {
            return httpTransport;
        }
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            logger.error("Error -GeneralSecurityException- creating httpTransport ", e);
        } catch (IOException e) {
            logger.error("Error -IOException- creating httpTransport ", e);
        }

        return httpTransport;
    }
}
