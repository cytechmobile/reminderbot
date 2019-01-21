package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
@Stateless
public class CaseSetTimezone {
    private final static Logger logger = LoggerFactory.getLogger(CaseSetTimezone.class.getName());

    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    String givenTimezone;
    Request request;
    ArrayList<String> splitMsg;
    String keyWord_my;
    String keyWord_global;


    String response = "I didnt understand whose timezone to set, type help for instructions \n";
    public CaseSetTimezone() {
    }


    public String getKeyWord_my() {
        return keyWord_my;
    }

    public void setKeyWord_my(String keyWord_my) {
        this.keyWord_my = keyWord_my;
    }

    public String getKeyWord_global() {
        return keyWord_global;
    }

    public void setKeyWord_global(String keyWord_global) {
        this.keyWord_global = keyWord_global;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public ArrayList<String> getSplitMsg() {
        return splitMsg;
    }

    public void setSplitMsg(ArrayList<String> splitMsg) {
        this.splitMsg = splitMsg;
    }






    public String setTimezone() {
        givenTimezone = extractTimeZone();
        //Checks given timezone
        if (givenTimezone == null) {
            return "Given timezone is wrong, try again.";
        }
        // setting global timezone
        if (splitMsg.get(1).equals(keyWord_global)) {
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

        } else
            // setting user timezone
            if (splitMsg.get(1).equals(keyWord_my)) {
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

        return response;
    }


    public String extractTimeZone() {
        String message[] = request.getMessage().getText().split("\\s+");
        String timeZone = null;
        for (int i = 0; i < message.length; i++) {
            if (message[i].equals("timezone") && message.length == i + 3) {
                TimeZone timeZoneFinder =  new TimeZone();
                timeZone = timeZoneFinder.findTimeZones(message[i + 2]);
            }
        }
        return timeZone;
    }


    public String getGivenTimeZone(String user) {
        List<TimeZone> timeZones = entityManager.
                createNamedQuery("get.Alltimezone", TimeZone.class).getResultList();

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
