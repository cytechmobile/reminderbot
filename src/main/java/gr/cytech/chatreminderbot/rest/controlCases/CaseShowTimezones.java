package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.db.Dao;
import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;

public class CaseShowTimezones {
    private static final Logger logger = LoggerFactory.getLogger(CaseShowReminders.class);

    @Inject
    Dao dao;
    private Request request;

    public Request getRequest() {
        return request;
    }

    public CaseShowTimezones() {

    }

    @Transactional
    public String showTimezones(Request request) {
        this.request = request;
        String showTimezone = "---- Your timezone is  ---- \n";
        String noTimezoneFound = "---- No Timezone found default timezone is ---- \n";
        String defaultTimezone = "---- Default timezone is ---- \n";

        if (!dao.defaultTimezoneExists()) {
            logger.info("created default timezone");
            TimeZone timeZone = new TimeZone("Europe/Athens", "default");
            dao.persist(timeZone);

        }

        String defaultTimezoneQuery = dao.getUserTimezone("default");
        try {

            String myTimezone = dao.getUserTimezone(request.getMessage().getSender().getName());

            return showTimezone + "Timezone = " + myTimezone + "\n " + defaultTimezone
                    + "Timezone = " + defaultTimezoneQuery;

        } catch (NoResultException e) {
            logger.info("in case no timezone found for the user");
            return noTimezoneFound + "Timezone = " + defaultTimezoneQuery;

        }

    }
}
