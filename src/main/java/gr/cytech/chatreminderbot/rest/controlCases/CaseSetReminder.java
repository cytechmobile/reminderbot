package gr.cytech.chatreminderbot.rest.controlCases;

import com.google.common.base.Strings;
import gr.cytech.chatreminderbot.rest.message.Request;
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
    public String setRequestForReminder(Request request) {
        Reminder reminder = new Reminder();
        reminder.setSpaceId(request.getMessage().getThread().getSpaceId());
        reminder.setThreadId(request.getMessage().getThread().getThreadId());

        List<String> splitMsg = List.of(request.getMessage().getText().split("\'"));
        List<String> whoPart = new ArrayList<>(List.of(splitMsg.get(0).split("\\s+")));

        String errorResult = checkRemindMessageFormat(splitMsg, whoPart);
        if (!Strings.isNullOrEmpty(errorResult)) {
            return errorResult;
        }

        setInfosForRemind(request, reminder, splitMsg, whoPart);
        //pass from string to ZoneDateTime
        //Check if date has passed

        if (reminder.getWhen().isBefore(ZonedDateTime.now(ZoneId.of(reminder.getReminderTimezone())))) {
            return "This date has passed "
                    + reminder.getWhen() + ". Check your timezone or insert in the current reminder";
        }

        saveAndSetReminder(reminder);

        return "Reminder with text:\n <b>" + reminder.getWhat() + "</b>.\n"
                + "Saved successfully and will notify you in: \n<b>"
                + calculateRemainingTime(reminder.getWhen(), reminder.getReminderTimezone()) + "</b>";
    }

    /*
     * uses the text message of the user from Request
     * message:(@reminder) remind me 'Something to do' at 16/03/2019 15:05 athens
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

    public String findDateParts(Request request, Reminder reminder, List<String> splitMsg) {
        //dateParts: at 16/03/2019 15:05 athens
        String[] dateParts = splitMsg.get(2).split("\\s+");
        String when = "";
        for (int i = 0; i < dateParts.length; i++) {
            if (dateParts[i].equals("at")) {
                when += dateParts[i + 1];
                when += " " + dateParts[i + 2];
                if (dateParts.length == 5) {
                    TimeZone timeZoneFinder = new TimeZone();
                    String zone = timeZoneFinder.findTimeZones(dateParts[i + 3]);
                    if (zone != null) {
                        reminder.setReminderTimezone(zone);
                        logger.info("SET timeZone became: {}", reminder.getReminderTimezone());
                    } else {
                        when += "Make fail date format.";
                    }
                } else {
                    String userTimezone = caseSetTimezone.getGivenTimeZone(request.getMessage().getSender().getName());
                    if (!(userTimezone.equals(""))) {
                        logger.info("User timeZone found: {}", reminder.getReminderTimezone());
                        reminder.setReminderTimezone(userTimezone);
                    } else {
                        logger.info("Global timeZone");
                        reminder.setReminderTimezone(caseSetTimezone.getGivenTimeZone("default"));
                    }
                }
            }
        }

        if (!(isValidFormatDate(when))) {
            return "Wrong date or Timezone format.\n"
                    + " DateFormat must be: dd/MM/yyyy HH:mm . \n"
                    + " For a timezone format you can also use GMT";
        }

        return when;
    }

    public Reminder setInfosForRemind(Request request, Reminder reminder,
                                      List<String> splitMsg, List<String> whoPart) {
        //what: Something to do
        reminder.setWhat(splitMsg.get(1));
        logger.info("set what: {}", reminder.getWhat());

        reminder.setWhen(dateForm(findDateParts(request, reminder, splitMsg), reminder.getReminderTimezone()));

        logger.info("set when: {}", reminder.getWhen());

        if (whoPart.get(0).equals("remind")) {
            // 1) me
            if (whoPart.get(1).equals("me")) {
                //  ---- takes the ID of the sender ---
                reminder.setSenderDisplayName(request.getMessage().getSender().getName());
            } else if (whoPart.get(1).equals("@all")) {
                reminder.setSenderDisplayName("users/all");
            } else {
                String displayName;
                if (whoPart.get(1).startsWith("#")) {
                    // 2)#RoomName
                    displayName = whoPart.get(1);
                } else {
                    // 3) @Firstname Lastname
                    if (whoPart.size() == 3) {
                        displayName = whoPart.get(1).substring(1)
                                + " " + whoPart.get(2);
                    } else {
                        // A name that is not 2 parts
                        displayName = "";
                        for (int i = 1; i < whoPart.size(); i++) {
                            displayName += whoPart.get(i) + " ";
                        }
                    }
                }
                reminder.setSenderDisplayName(findIdUserName(displayName,
                        request.getMessage().getThread().getSpaceId()));
            }
        }
        logger.info("set who: {}", reminder.getSenderDisplayName());

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
    public String calculateRemainingTime(ZonedDateTime inputDate, String timezone) {
        ZonedDateTime fromDateTime = ZonedDateTime.now(ZoneId.of(timezone));

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

    public String checkRemindMessageFormat(List<String> splitMsg, List<String> whoPart) {
        String botName = entityManager.createNamedQuery("get.configurationByKey", Configurations.class)
                .setParameter("configKey", "BOT_NAME")
                .getSingleResult().getValue();

        if (whoPart.get(0).equals("@" + botName)) {
            whoPart.remove(0);
        }

        if (splitMsg.get(1).length() >= 255) {
            return "Part what can not be more than 255 chars.";
        } else if (splitMsg.size() != 3) {
            return "Use  quotation marks  `'` only two times. One before and one after what, type Help for example.";
        } else if (whoPart.size() < 2) {
            return "You missed add who, type Help for example.";
        } else if (whoPart.size() > 3) {
            return "Valid names must be two parts, type Help for example.";
        } else if (!splitMsg.get(2).contains("at")) {
            return "You missed `at` before date, type Help for example.";
        }

        return "";
    }

}
