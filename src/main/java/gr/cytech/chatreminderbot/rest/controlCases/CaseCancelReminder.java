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

import static gr.cytech.chatreminderbot.rest.message.Action.CANCEL_REMINDER;

@RequestScoped
public class CaseCancelReminder {
    private static final Logger logger = LoggerFactory.getLogger(CaseCancelReminder.class);

    @Inject
    Dao dao;

    public CaseCancelReminder() {
    }

    @Transactional
    String cancelReminder(Request request, List<String> splitMsg) {
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
        String spaceId = request.getMessage().getThread().getSpaceId();
        if (request.getAction() != null) {
            if (request.getAction().getActionMethodName().equals(CANCEL_REMINDER)) {
                return createCardResponse(reminders.get(0), "UPDATE_MESSAGE", spaceId);
            }
        }
        return createCardResponse(reminders.get(0), "NEW_MESSAGE", spaceId);
    }

    private String createCardResponse(Reminder reminder, String typeForMessage, String spaceId) {
        return new CardResponseBuilder(typeForMessage)
                .thread("spaces/" + spaceId)
                .textParagraph("Reminder with text:\n<b>"
                        + reminder.getWhat()
                        + "</b>\nsuccessfully canceled!")
                .build();
    }

}
