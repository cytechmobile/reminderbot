package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Stateless
public class CaseShowReminders {
    private final static Logger logger = LoggerFactory.getLogger(CaseShowReminders.class.getName());

    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    Request request;

    public CaseShowReminders() {
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }


    public String showReminders() {
        List<Reminder> reminders = entityManager.
                createNamedQuery("reminder.showReminders", Reminder.class)
                .setParameter("userid", request.getMessage().getSender().getName())
                .getResultList();

        String remindersShow = "---- Reminders that will notify you ---- \n";
        if (reminders.isEmpty()) {
            logger.debug("Reminders not found return - ");
            return "---- Reminders not found ---";
        } else {
            return remindersShow + reminderListToString(reminders);
        }
    }

    public String reminderListToString(List<Reminder> reminders) {
        String remindersShow = "";

        for (int i = 0; i < reminders.size(); i++) {
            remindersShow += i + 1 + ") ID:" + reminders.get(i).getReminderId() + " what:' " +
                    reminders.get(i).getWhat() + " ' When: " +
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                            .format(reminders.get(i).getWhen()
                                    .withZoneSameLocal(ZoneId.of(reminders.get(i).getReminderTimezone()))) + " " +
                    reminders.get(i).getReminderTimezone() + "\n";
        }
        return remindersShow;
    }
}
