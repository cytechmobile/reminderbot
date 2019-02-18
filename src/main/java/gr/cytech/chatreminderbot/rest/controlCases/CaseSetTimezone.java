package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class CaseSetTimezone {
    private static final Logger logger = LoggerFactory.getLogger(CaseSetTimezone.class);

    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    private Request request;
    private ArrayList<String> splitMsg;
    private String keyWordMy;
    private String keyWordGlobal;
    private String response = "I didnt understand whose timezone to set, type help for instructions \n";

    public CaseSetTimezone() {
    }

    public void setKeyWordMy(String keyWordMy) {
        this.keyWordMy = keyWordMy;
    }

    public void setKeyWordGlobal(String keyWordGlobal) {
        this.keyWordGlobal = keyWordGlobal;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void setSplitMsg(ArrayList<String> splitMsg) {
        this.splitMsg = splitMsg;
    }

    @Transactional
    public String setTimezone() {
        String givenTimezone = extractTimeZone();
        //Checks given timezone
        if (givenTimezone == null) {
            return "Given timezone is wrong, try again.";
        }
        // setting global timezone
        if (splitMsg.get(1).equals(keyWordGlobal)) {
            logger.info("---Case Set global timezone---");

            TimeZone defaultTimeZone = new TimeZone(givenTimezone, "default");
            if (getGivenTimeZone("default").equals("")) {
                entityManager.persist(defaultTimeZone);
            } else {
                Query query = entityManager.createNamedQuery("set.timezone")
                        .setParameter("timezone", defaultTimeZone.getTimezone())
                        .setParameter("userid", "default");
                query.executeUpdate();
            }
            response = "You successfully set the global timezone at:" + defaultTimeZone.getTimezone();
            return response;

        } else {
            // setting user timezone
            if (splitMsg.get(1).equals(keyWordMy)) {
                logger.info("---Case Set user timezone---");
                String who = request.getMessage().getSender().getName();
                TimeZone timeZone = new TimeZone(givenTimezone, who);
                if (getGivenTimeZone(who).equals("")) {
                    entityManager.persist(timeZone);
                } else {
                    Query query = entityManager.createNamedQuery("set.timezone")
                            .setParameter("timezone", timeZone.getTimezone())
                            .setParameter("userid", who);
                    query.executeUpdate();
                }
                response = " <" + who + "> successfully set your timezone at:" + timeZone.getTimezone();
                return response;
            }
        }
        return response;
    }

    public String extractTimeZone() {
        String[] message = request.getMessage().getText().split("\\s+");
        String timeZone = null;
        for (int i = 0; i < message.length; i++) {
            if (message[i].equals("timezone") && message.length == i + 3) {
                TimeZone timeZoneFinder = new TimeZone();
                timeZone = timeZoneFinder.findTimeZones(message[i + 2]);
            }
        }
        return timeZone;
    }

    public String getGivenTimeZone(String user) {
        List<TimeZone> timeZones = entityManager
                .createNamedQuery("get.Alltimezone", TimeZone.class).getResultList();

        if (timeZones.isEmpty()) {
            logger.debug("timezones not found return - ");
            return "";
        } else {
            for (TimeZone z : timeZones) {
                if (z.getUserid().equals(user)) {
                    logger.debug("found zone: {}", z.getTimezone());
                    return z.getTimezone();
                }
            }
            logger.debug("Didnt find zone for this user");
            return "";
        }
    }
}
