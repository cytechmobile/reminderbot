package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.db.Dao;
import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

public class CaseSetTimezone {
    private static final Logger logger = LoggerFactory.getLogger(CaseSetTimezone.class);

    @Inject
    Dao dao;

    private Request request;
    private List<String> splitMsg;
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

    public void setSplitMsg(List<String> splitMsg) {
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
            defaultTimeZone = dao.merge(defaultTimeZone);
            response = "You successfully set the global timezone at:" + defaultTimeZone.getTimezone();
            return response;

        } else {
            // setting user timezone
            if (splitMsg.get(1).equals(keyWordMy)) {
                logger.info("---Case Set user timezone---");
                String who = request.getMessage().getSender().getName();
                TimeZone timeZone = new TimeZone(givenTimezone, who);
                timeZone = dao.merge(timeZone);
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
}
