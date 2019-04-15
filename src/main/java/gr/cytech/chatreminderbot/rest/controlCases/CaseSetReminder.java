package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.nlp.parse.DateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class CaseSetReminder {
    private static final Logger logger = LoggerFactory.getLogger(CaseSetReminder.class);

    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    //Needs to set timer
    @Inject
    public TimerSessionBean timerSessionBean;

    //needs to use function extractTimeZone
    @Inject
    CaseSetTimezone caseSetTimezone;

    protected Client client;

    @Transactional
    public String buildReminder(Request request) {
        Reminder reminder = new Reminder();
        reminder.setSpaceId(request.getMessage().getThread().getSpaceId());
        reminder.setThreadId(request.getMessage().getThread().getThreadId());

        if (request.getMessage().getText().length() >= 255) {
            return "Part what can not be more than 255 chars.";
        }
        List<String> splitMsg = new ArrayList<>(List.of(request.getMessage().getText().split("\\s+")));

        setInfosForRemind(request, reminder, splitMsg);
        //pass from string to ZoneDateTime
        //Check if date has passed

        if (reminder.getWhen().isBefore(ZonedDateTime.now(ZoneId.of(reminder.getWhen().getZone().getId())))) {
            return "This date has passed "
                    + reminder.getWhen() + ". Check your timezone or insert in the current reminder";
        }

        saveAndSetReminder(reminder);

        return "Reminder with text:\n <b>" + reminder.getWhat() + "</b>.\n"
                + "Saved successfully and will notify you in: \n<b>"
                + calculateRemainingTime(reminder.getWhen()) + "</b>";
    }

    /*
     * uses the text message of the user from Request
     * message:(@reminder) remind me Something to do in 10 minutes
     *
     * Setting basic infos for the users reminder such us:
     * who
     *  1) me - returns <user/id>
     *  2) #RoomName (with @all) - returns roomsName
     *  3) @Firstname Lastname - returns <user/id>
     *  4) @all - returns <user/all>
     * what
     * when
     * timezone
     *   get it from message
     *   get it from users settings
     *   get it from global settings
     * */

    public Reminder setInfosForRemind(Request request, Reminder reminder,List<String> splitMsg) {
        String botName = new Dao().getBotName(entityManager);
        String timezone = new Dao().getUserTimezone(request.getMessage().getSender().getName(), entityManager);

        if (splitMsg.get(0).equals("@" + botName)) {
            splitMsg.remove(0);
        }

        String text = String.join(" ", splitMsg);

        ZoneId zoneId = ZoneId.of(timezone);
        TimeZone setTimeZone = TimeZone.getTimeZone(timezone);

        PrettyTimeParser prettyTimeParser = new PrettyTimeParser(setTimeZone);
        List<DateGroup> parse = prettyTimeParser.parseSyntax(text);
        DateGroup dateGroup = parse.get(0);
        int pos = dateGroup.getPosition();
        String upTo = text.substring(0, pos).trim();

        //removing ending words that PrettyTimeParser doesn't remove
        if (upTo.endsWith(" every")) {
            upTo = upTo.substring(0, upTo.length() - " every".length());
        }
        if (upTo.endsWith(" at") || upTo.endsWith(" in")) {
            upTo = upTo.substring(0, upTo.length() - " at".length());
        }

        Instant when = Instant.ofEpochMilli(dateGroup.getDates().get(0).getTime());

        reminder.setWhen(when.atZone(zoneId));

        logger.info("set when: {}", reminder.getWhen());

        if (splitMsg.get(0).equals("remind")) {
            // 1) me
            if (splitMsg.get(1).equals("me")) {
                //  ---- takes the ID of the sender ---
                reminder.setSenderDisplayName(request.getMessage().getSender().getName());
            } else if (splitMsg.get(1).equals("@all")) {
                reminder.setSenderDisplayName("users/all");
            } else {
                String displayName = "";
                if (splitMsg.get(1).startsWith("#")) {
                    // 2)#RoomName
                    displayName = splitMsg.get(1);
                }
                reminder.setSenderDisplayName(findIdUserName(displayName,
                        request.getMessage().getThread().getSpaceId()));
            }
        }

        if (upTo.startsWith("remind ")) {
            upTo = upTo.replace("remind ", "");
        }
        if (upTo.startsWith("me ")) {
            upTo = upTo.replace("me ", "");
        }
        if (upTo.startsWith(reminder.getSenderDisplayName())) {
            upTo = upTo.replace(reminder.getSenderDisplayName(), "");
        }
        if (upTo.startsWith("@all")) {
            upTo = upTo.replace("@all", "");
        }
        logger.info("updated text {}", upTo);
        if (client == null) {
            client = Client.newClient(entityManager);
        }
        Map<String, String> listOfMembersInRoom = client
                .getListOfMembersInRoom(request.getMessage().getThread().getSpaceId());
        List<String> memberNames = new ArrayList<>(listOfMembersInRoom.keySet());
        List<String> memberID = new ArrayList<>(listOfMembersInRoom.values());

        for (int i = 0; i < memberNames.size(); i++) {
            if (upTo.startsWith("@" + memberNames.get(i))) {
                reminder.setSenderDisplayName(memberID.get(i));
                upTo = upTo.replace("@" + memberNames.get(i), "");
            }
        }
        //what: Something to do
        reminder.setWhat(upTo);
        logger.info("set what: {}", reminder.getWhat());

        return reminder;

    }

    public void saveAndSetReminder(Reminder reminder) {
        entityManager.persist(reminder);
        //if there is no next reminder, sets this as next
        if (timerSessionBean.getNextReminderDate() == null) {
            logger.info("set NEW reminder to : {}", reminder.getWhen());
            timerSessionBean.setNextReminder(reminder, reminder.getWhen());
        } else {
            //ELSE  if the new reminder is before the nextReminder,
            // changes as next reminder this

            if (reminder.getWhen().isBefore(timerSessionBean.getNextReminderDate())) {
                logger.info("CHANGE next reminder to: {}", reminder.getWhen());
                timerSessionBean.setNextReminder(reminder, reminder.getWhen());
            }
        }
    }

    //Returns date from string, based on dd/MM/yyyy HH:mm format,
    //Is called after we ensure this is the current format
    public ZonedDateTime dateForm(String when, String timezone) {
        String format = "dd/MM/yyyy HH:mm";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        return ZonedDateTime.parse(when, formatter.withZone(ZoneId.of(timezone)));
    }

    //Check if given date in string is in valid format
    public boolean isValidFormatDate(String when) {
        String format = "dd/MM/yyyy HH:mm";
        LocalDateTime ldt;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        try {
            ldt = LocalDateTime.parse(when, formatter);
            String result = ldt.format(formatter);
            return result.equals(when);
        } catch (DateTimeParseException e) {
            try {
                LocalDate ld = LocalDate.parse(when, formatter);
                String result = ld.format(formatter);
                return result.equals(when);
            } catch (DateTimeParseException exp) {
                try {
                    LocalTime lt = LocalTime.parse(when, formatter);
                    String result = lt.format(formatter);
                    return result.equals(when);
                } catch (DateTimeParseException e2) {
                    // logger.error("Error Parse LocalDateTime:{}", value, e);
                }
            }
        }
        return false;
    }

    //Returns the reminding time after setting a reminder
    public String calculateRemainingTime(ZonedDateTime inputDate) {
        ZonedDateTime fromDateTime = ZonedDateTime.now(inputDate.getZone());

        ZonedDateTime tempDateTime = ZonedDateTime.from(fromDateTime);

        long years = tempDateTime.until(inputDate, ChronoUnit.YEARS);
        tempDateTime = tempDateTime.plusYears(years);

        long months = tempDateTime.until(inputDate, ChronoUnit.MONTHS);
        tempDateTime = tempDateTime.plusMonths(months);

        long days = tempDateTime.until(inputDate, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(days);

        long hours = tempDateTime.until(inputDate, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(hours);

        long minutes = tempDateTime.until(inputDate, ChronoUnit.MINUTES);

        if (months > 0) {
            return months + " Months,  "
                    + days + " Days, "
                    + hours + " Hours, "
                    + minutes + " Minutes";
        }
        if (days > 0) {
            return days + " Days, "
                    + hours + " Hours, "
                    + minutes + " Minutes";
        }
        if (hours > 0) {
            return hours + " Hours, "
                    + minutes + " Minutes";
        }

        return minutes + " Minutes";
    }

    /*
     * @param given displayName
     * @param spaceID
     * @return user/id if not found given displayName
     * */
    public String findIdUserName(String displayName, String spaceId) {
        if (client == null) {
            client = Client.newClient(entityManager);
        }
        Map<String, String> users = client.getListOfMembersInRoom(spaceId);
        //if displayName not found then just save the name as it is
        return users.getOrDefault(displayName, displayName);
    }

}
