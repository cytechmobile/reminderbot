package gr.cytech.chatreminderbot.rest.controlCases;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class ClientProducer {
    private static final Logger logger = LoggerFactory.getLogger(ClientProducer.class);

    private static final List<String> SCOPE = Collections.singletonList("https://www.googleapis.com/auth/chat.bot");
    private static final String KEY_FILE_PATH_ENV = "BOT_KEY_FILE_PATH";

    protected static GoogleCredential credential;
    protected static HttpTransport httpTransport;
    protected static HttpRequestFactory requestFactory;
    protected static String botKeyFilePath;

    public static String getBotKeyFilePath() {
        if (botKeyFilePath == null) {
            botKeyFilePath = System.getProperty(KEY_FILE_PATH_ENV,
                    System.getenv()
                            .getOrDefault(KEY_FILE_PATH_ENV, "./botnotifier-key.json"));
        }
        return botKeyFilePath;
    }

    @Produces
    protected GoogleCredential getCredential() {
        if (credential == null) {
            String keyFilePath = getBotKeyFilePath();
            try {
                credential = GoogleCredential
                        .fromStream(new FileInputStream(keyFilePath))
                        .createScoped(SCOPE);
            } catch (IOException e) {
                logger.error("Error creating GoogleCredential using key file:{}", keyFilePath, e);
            }
        }

        return credential;
    }

    @Produces
    protected HttpRequestFactory getHttpRequestFactory() {
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
