package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class CaseDeleteReminder {
    private static final Logger logger = LoggerFactory
            .getLogger(CaseDeleteReminder.class.getName());

    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    private Request request;

    private ArrayList<String> splitMsg;

    public CaseDeleteReminder() {
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
    String deleteReminder() {
        String reminderId;
        if (splitMsg.get(1).matches("[0-9]+")) {
            reminderId = splitMsg.get(1);
        } else {

            return "Wrong id format, must be only numbers";
        }
        // -- Checks reminder id AND userid
        List<Reminder> reminders = entityManager
                .createNamedQuery("reminder.findByUserAndReminderId", Reminder.class)
                .setParameter("userId", request.getMessage().getSender().getName())
                .setParameter("reminderId", Integer.parseInt(reminderId))
                .getResultList();
        if (reminders.isEmpty()) {
            return "Couldn't find reminder with id: " + reminderId;
        }
        //in order to delete must use find first.
        Reminder oldReminder = entityManager.find(Reminder.class, Integer.parseInt(reminderId));
        entityManager.remove(oldReminder);
        logger.info("Deleted reminder with ID: {}", oldReminder.getReminderId());
        return "Reminder with ID: " + oldReminder.getReminderId() + " successfully deleted!";
    }

}
