package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

public class CaseShowSettings {
    private final static Logger logger = LoggerFactory.getLogger(CaseShowReminders.class.getName());

    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    private Request request;

    public Request getRequest() {
        return request;
    }

    public CaseShowSettings() {

    }

    String showSettings(Request request) {
        this.request = request;
        String settingsShow = "---- Your timezone is  ---- \n";
        String settingsNoTimezoneFound = "---- No Timezone found default timezone is ---- \n";
        String settingsDefaultTimezone = "--- Default timezone is --- \n";

        TimeZone defaultTimezone = (TimeZone) entityManager
                .createQuery("SELECT t from TimeZone t where t.userid = :id")
                .setParameter("id", "default")
                .getSingleResult();
        try {

            TimeZone myTimezone = (TimeZone) entityManager
                    .createQuery("SELECT t from TimeZone t where t.userid = :id")
                    .setParameter("id", request.getMessage().getSender().getName())
                    .getSingleResult();

            return settingsShow + "Timezone = " + myTimezone.toString() + "\n " + settingsDefaultTimezone +
                    "Timezone = " + defaultTimezone.toString();

        } catch (NoResultException e) {

            return settingsNoTimezoneFound + "Timezone = " + defaultTimezone.toString();

        }

    }


}
