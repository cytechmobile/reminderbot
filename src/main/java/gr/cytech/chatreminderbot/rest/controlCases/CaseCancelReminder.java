package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.GoogleCards.CardResponseBuilder;
import gr.cytech.chatreminderbot.rest.db.Dao;
import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@RequestScoped
public class CaseCancelReminder {
    private static final Logger logger = LoggerFactory.getLogger(CaseCancelReminder.class);

    private Request request;

    @Inject
    Dao dao;

    private List<String> splitMsg;

    public CaseCancelReminder() {
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
    String cancelReminder() {
        String reminderId;
        if (splitMsg.get(1).matches("[0-9]+")) {
            reminderId = splitMsg.get(1);
        } else {

            return "Wrong id format, must be only numbers";
        }
        // -- Checks reminder id AND userid
        int remId = Integer.parseInt(reminderId);
        List<Reminder> reminders = dao.findReminders(request.getMessage().getSender().getName(),
                remId);
        if (reminders.isEmpty()) {
            return "Couldn't find the reminder or maybe you don't own this reminder";
        }
        //in order to delete must use find first.
        dao.deleteReminder(remId);
        logger.info("Canceled reminder with ID: {}", remId);
        if (request.getAction() != null) {
            if (request.getAction().getActionMethodName().equals("CancelReminder")) {
                return createCardResponse(reminders.get(0), "UPDATE_MESSAGE");
            }
        }
        return createCardResponse(reminders.get(0), "NEW_MESSAGE");
    }

    private String createCardResponse(Reminder reminder, String typeForMessage) {
        return new CardResponseBuilder()
                .thread("spaces/" + request.getMessage().getThread().getSpaceId())
                .textParagraph("Reminder with text:\n<b>"
                        + reminder.getWhat()
                        + "</b>\nsuccessfully canceled!")
                .build(typeForMessage);
    }

}
