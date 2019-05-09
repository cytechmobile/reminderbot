package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.db.Dao;
import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@RequestScoped
public class CaseDeleteReminder {
    private static final Logger logger = LoggerFactory.getLogger(CaseDeleteReminder.class);

    private Request request;

    @Inject
    Dao dao;

    private List<String> splitMsg;

    public CaseDeleteReminder() {
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
    String deleteReminder() {
        String reminderId;
        if (splitMsg.get(1).matches("[0-9]+")) {
            reminderId = splitMsg.get(1);
        } else {

            return "Wrong id format, must be only numbers";
        }
        // -- Checks reminder id AND userid
        logger.info("hello user: {}", request.getMessage().getSender().getName());
        int remId = Integer.parseInt(reminderId);
        List<Reminder> reminders = dao.findReminders(request.getMessage().getSender().getName(),
                remId);
        if (reminders.isEmpty()) {
            return "Couldn't find reminder with id: " + reminderId + " or maybe you don't own this reminder";
        }
        //in order to delete must use find first.
        dao.deleteReminder(remId);
        logger.info("Deleted reminder with ID: {}", remId);
        return "Reminder with ID: " + remId + " successfully deleted!";
    }

}
