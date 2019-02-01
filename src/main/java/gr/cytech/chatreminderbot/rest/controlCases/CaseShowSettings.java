package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class CaseShowSettings {
    private final static Logger logger = LoggerFactory.getLogger(CaseShowReminders.class.getName());

    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    private Request request;

    public CaseShowSettings() {
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    String showSettings() {

        TimeZone defaultTimezone = (TimeZone) entityManager.createQuery("SELECT t from TimeZone t where t.userid = :id").setParameter("id", "default").getSingleResult();

        TimeZone myTimezone = (TimeZone) entityManager.createQuery("SELECT t from TimeZone t where t.userid = :id").setParameter("id", request.getMessage().getSender().getName()).getSingleResult();


        String settingsShow = "---- Your timezone is  ---- \n";
        String settingsNoTimezoneFound = "---- No Timezone found default timezone is ---- \n";
        String settingsDefaultTimezone = "--- Default timezone is --- \n";
        if (defaultTimezone.toString().isEmpty()) {
            return settingsNoTimezoneFound + "Timezone = " + defaultTimezone.toString();
        } else {
            return settingsShow + "Timezone = " + myTimezone.toString() + "\n " + settingsDefaultTimezone + "Timezone = " + defaultTimezone.toString();
        }
    }


}
