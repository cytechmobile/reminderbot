package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.beans.TimerSessionBean;
import gr.cytech.chatreminderbot.rest.db.Dao;
import gr.cytech.chatreminderbot.rest.message.Request;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.nlp.parse.DateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;
import java.util.*;

@RequestScoped
public class CaseSetReminder {
    private static final Logger logger = LoggerFactory.getLogger(CaseSetReminder.class);
    private static final Collection<String> WORDS_TO_IGNORE = Set.of("in", "on", "at", "every");

    //Needs to set timer
    @Inject
    TimerSessionBean timerSessionBean;

    @Inject
    Dao dao;

    @Inject
    UserTransaction transaction;

    Client client;

    /*
     * Build a reminder and persist if valid
     */
    String buildAndPersistReminder(Request request) {
        Reminder reminder = new Reminder();
        reminder.setFullText(request.getMessage().getText());
        reminder.setSpaceId(request.getMessage().getThread().getSpaceId());
        reminder.setThreadId(request.getMessage().getThread().getThreadId());

        if (request.getMessage().getText().length() >= 255) {
            return "Part what can not be more than 255 chars";
        }
        List<String> splitMsg = new ArrayList<>(List.of(request.getMessage().getText().split("\\s+")));

        String botName = dao.getBotName();
        String timezone = dao.getUserTimezone(request.getMessage().getSender().getName());

        if (splitMsg.get(0).equals("@" + botName)) {
            splitMsg.remove(0);
        }

        String text = String.join(" ", splitMsg);

        ZoneId zoneId = ZoneId.of(timezone);
        TimeZone setTimeZone = TimeZone.getTimeZone(timezone);

        PrettyTimeParser prettyTimeParser = new PrettyTimeParser(setTimeZone);
        List<DateGroup> parse = prettyTimeParser.parseSyntax(text);

        if (parse == null || parse.isEmpty()) {
            return "i couldn't extract the time.\n"
                    + "Check for misspelled word or use help command";
        }
        String timeToNotify = parse.get(0).getText();

        for (String check : WORDS_TO_IGNORE) {
            if (timeToNotify.startsWith(check + " ")) {
                timeToNotify = timeToNotify.substring(check.length() + 1);
            }
        }

        setInfosForRemind(request, reminder, splitMsg, parse, text, zoneId);

        //pass from string to ZoneDateTime
        //Check if date has passed

        if (reminder.getWhen().isBefore(ZonedDateTime.now())) {
            return "This date has passed "
                    + reminder.getWhen() + ". Check your timezone or insert in the current reminder";
        }
        if (reminder.getWhen().isBefore(ZonedDateTime.now().plusSeconds(58)) && reminder.isRecuring()) {
            logger.info("cant set reminder under 1 minute due to spam messages");
            return "Sorry you cant set reminder under 1 minute";
        }
        try {
            transaction.begin();
            dao.persist(reminder);
            transaction.commit();
        } catch (Exception e) {
            try {
                transaction.rollback();
                logger.warn("Database Error when tried to commit the transaction with Exception: ", e);
                return "Database Error transaction rollback";
            } catch (Exception e1) {
                logger.warn("Database Error when tried to rollback the transaction with Exception: ", e);
                return "Oops something went wrong when tried to save the reminder";
            }

        }

        timerSessionBean.setTimerForReminder(reminder);

        return "Reminder with text:\n <b>" + reminder.getWhat() + "</b>.\n"
                + "Saved successfully and will notify you in: \n<b>"
                + timeToNotify + "</b>";
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

    protected String updateUpToString(String upTo, Reminder reminder, List<String> splitMsg, Request request) {
        //add reminder display name and remove remind and who part of the upTo string to
        //display only the given text
        if (splitMsg.get(0).equals("remind")) {
            // 1) me
            if (splitMsg.get(1).equals("me")) {
                //  ---- takes the ID of the sender ---
                reminder.setSenderDisplayName(request.getMessage().getSender().getName());
            } else if (splitMsg.get(1).equals("@all")) {
                reminder.setSenderDisplayName(request.getMessage().getSender().getName());
                reminder.setForAll(true);
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
            upTo = upTo.substring("remind ".length());
        }
        if (upTo.startsWith("me ")) {
            upTo = upTo.substring("me ".length());
        } else if (upTo.startsWith(reminder.getSenderDisplayName())) {
            upTo = upTo.substring(reminder.getSenderDisplayName().length());
        } else if (upTo.startsWith("@all ")) {
            upTo = upTo.substring("@all ".length());
        }

        if (upTo.startsWith("to ")) {
            upTo = upTo.substring("to ".length());
        }

        return upTo;
    }

    protected Reminder setInfosForRemind(Request request, Reminder reminder, List<String> splitMsg,
                                         List<DateGroup> parse, String text, ZoneId zoneId) {

        DateGroup dateGroup = parse.get(0);
        int pos = dateGroup.getPosition();
        String upTo = text.substring(0, pos).trim();

        //removing ending words that PrettyTimeParser doesn't remove
        for (String check : WORDS_TO_IGNORE) {
            if (upTo.endsWith(" " + check)) {
                upTo = upTo.substring(0, upTo.length() - (" " + check).length());
            }
        }

        Instant when = Instant.ofEpochMilli(dateGroup.getDates().get(0).getTime());

        reminder.setWhen(when.atZone(zoneId));

        logger.info("set when: {}", reminder.getWhen());

        upTo = updateUpToString(upTo, reminder, splitMsg, request);

        if (upTo.startsWith("@")) {
            if (client == null) {
                client = Client.newClient(dao);
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

        }

        if (upTo.startsWith(" to ")) {
            upTo = upTo.substring(" to ".length());
        }
        //what: Something to do
        reminder.setWhat(upTo);

        if (parse.get(0).isRecurring()/* && reminder.getSenderDisplayName() != "users/all"*/) {
            reminder.setRecuring(true);
        } else {
            reminder.setRecuring(false);
        }
        logger.info("set what: {}", reminder.getWhat());

        return reminder;
    }

    /*
     * @param given displayName
     * @param spaceID
     * @return user/id if not found given displayName
     * */
    protected String findIdUserName(String displayName, String spaceId) {
        if (client == null) {
            client = Client.newClient(dao);
        }
        Map<String, String> users = client.getListOfMembersInRoom(spaceId);
        //if displayName not found then just save the name as it is
        return users.getOrDefault(displayName, displayName);
    }

    //Returns date from string, based on dd/MM/yyyy HH:mm format,
    //Is called after we ensure this is the current format
    public static ZonedDateTime dateForm(String when, String timezone) {
        String format = "dd/MM/yyyy HH:mm";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        return ZonedDateTime.parse(when, formatter.withZone(ZoneId.of(timezone)));
    }

    //Check if given date in string is in valid format
    public static boolean isValidFormatDate(String when) {
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
}
