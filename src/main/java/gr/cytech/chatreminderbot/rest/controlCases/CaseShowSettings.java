package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class CaseShowSettings {
    private final static Logger logger = LoggerFactory.getLogger(CaseShowReminders.class.getName());

    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    Request request;

    public CaseShowSettings() {
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public String showSettings() {
        List<TimeZone> settings = entityManager.createNamedQuery("get.UserTimezone",TimeZone.class)
                .setParameter("id", request.getMessage().getSender().getName())
                .getResultList();

        String settingsShow = "---- Your timezone is  ---- \n";
        if (settings.isEmpty()) {
            return "---- No timezone Found ---";
        }else {
            return settingsShow + reminderListToString(settings);
        }
    }

    public String reminderListToString(List<TimeZone> timezone) {
        String timezoneShow = "";

        for (int i = 0; i < timezone.size(); i++) {
            timezoneShow += i + 1 + ") ID:" + timezone.get(i).getUserid() + "Timezone = " + timezone.get(i).getTimezone();
        }
        return timezoneShow;
    }
}
