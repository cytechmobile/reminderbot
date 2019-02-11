package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

public class CaseShowTimezones {
    private final static Logger logger = LoggerFactory.getLogger(CaseShowReminders.class.getName());

    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    private Request request;

    public Request getRequest() {
        return request;
    }

    public CaseShowTimezones() {

    }

    String showTimezones(Request request) {
        this.request = request;
        String showTimezone = "---- Your timezone is  ---- \n";
        String noTimezoneFound = "---- No Timezone found default timezone is ---- \n";
        String defaultTimezone = "---- Default timezone is ---- \n";

        TimeZone defaultTimezoneQuery = (TimeZone) entityManager.createNamedQuery("show.timezones")
                .setParameter("id","default")
                .getSingleResult();
        try {

            TimeZone myTimezone = (TimeZone) entityManager
                    .createNamedQuery("show.timezones")
                    .setParameter("id", request.getMessage().getSender().getName())
                    .getSingleResult();

            return showTimezone + "Timezone = " + myTimezone.toString() + "\n " + defaultTimezone +
                    "Timezone = " + defaultTimezoneQuery.toString();

        } catch (NoResultException e) {

            return noTimezoneFound + "Timezone = " + defaultTimezoneQuery.toString();

        }

    }


}
