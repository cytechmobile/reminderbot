package gr.cytech.chatreminderbot.rest.controlCases;

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
import java.util.Arrays;
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

    @Inject
    public Client client;

    private String botName;

    private String timeZone;

    private String who;

    private String when;

    private String what;

    private Request request;

    private String spaceId;

    private String threadId;

    private ArrayList<String> splitMsg;

    private ArrayList<String> whoPart;

    private ZonedDateTime inputDate;

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
        spaceId = request.getMessage().getThread().getSpaceId();
        threadId = request.getMessage().getThread().getThreadId();
    }

    public void setInputDate(ZonedDateTime inputDate) {
        this.inputDate = inputDate;
    }

    @Transactional
    public String setReminder() {

        if (!checkRemindMessageFormat().equals("")) {
            return checkRemindMessageFormat();
        }

        setInfosForRemind();

        if (!(isValidFormatDate(when))) {
            return "Wrong date or Timezone format.\n"
                    + " DateFormat must be: dd/MM/yyyy HH:mm . \n"
                    + " For a timezone format you can also use GMT";
        }

        //pass from string to ZoneDateTime
        setInputDate(dateForm());
        //Check if date has passed
        if (inputDate.isBefore(ZonedDateTime.now(ZoneId.of(getTimeZone())))) {
            return "This date has passed "
                    + inputDate + ". Check your timezone or insert in the current reminder";
        }

        Reminder reminder = new Reminder(what, inputDate, who, timeZone, spaceId, threadId);
        saveAndSetReminder(reminder);

        return "Reminder with text:\n <b>" + what + "</b>.\n"
                + "Saved successfully and will notify you in: \n<b>"
                + calculateRemainingTime(inputDate) + "</b>";
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

    public void setInfosForRemind() {

        //what: Something to do
        setWhat(splitMsg.get(1));
        logger.info("set what: {}", what);

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
                        setTimeZone(zone);
                        logger.info("SET timeZone became: {}", zone);
                    } else {
                        when += "Make fail date format.";
                    }
                } else {
                    String userTimezone = caseSetTimezone.getGivenTimeZone(request.getMessage().getSender().getName());
                    if (!(userTimezone.equals(""))) {
                        logger.info("User timeZone found: {}", userTimezone);
                        setTimeZone(userTimezone);
                    } else {
                        logger.info("Global timeZone");
                        setTimeZone(caseSetTimezone.getGivenTimeZone("default"));
                    }
                }
            }
        }
        setWhen(when);
        logger.info("set when: {}", when);

        if (whoPart.get(0).equals("remind")) {
            // 1) me
            if (whoPart.get(1).equals("me")) {
                //  ---- takes the ID of the sender ---
                setWho(request.getMessage().getSender().getName());
            } else if (whoPart.get(1).equals("@all")) {
                setWho("users/all");
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
                setWho(findIdUserName(displayName, request.getMessage().getThread().getSpaceId()));
            }
        }
        logger.info("set who: {}", who);

    }

    public void saveAndSetReminder(Reminder reminder) {
        entityManager.persist(reminder);
        //if there is no next reminder, sets this as next
        if (timerSessionBean.getNextReminderDate() == null) {
            logger.info("set NEW reminder to : {}", inputDate);
            timerSessionBean.setNextReminder(reminder, inputDate);
        } else {
        //ELSE  if the new reminder is before the nextReminder,
        // changes as next reminder this

            if (inputDate.isBefore(timerSessionBean.getNextReminderDate())) {
                logger.info("CHANGE next reminder to: {}", inputDate);
                timerSessionBean.setNextReminder(reminder, inputDate);
            }
        }
    }

    //Returns date from string, based on dd/MM/yyyy HH:mm format,
    //Is called after we ensure this is the current format
    public ZonedDateTime dateForm() {
        String format = "dd/MM/yyyy HH:mm";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        return ZonedDateTime.parse(when, formatter.withZone(ZoneId.of(getTimeZone())));
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
        ZonedDateTime fromDateTime = ZonedDateTime.now(ZoneId.of(getTimeZone()));

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
        Map<String, String> users = client.getListOfMembersInRoom(spaceId);
        //if displayName not found then just save the name as it is
        return users.getOrDefault(displayName, displayName);
    }

    public String checkRemindMessageFormat() {
        splitMsg = new ArrayList<>(Arrays.asList(request.getMessage().getText().split("\'")));

        removingBotName();

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

    private void removingBotName() {
        whoPart = new ArrayList<>(Arrays.asList(splitMsg.get(0).split("\\s+")));
        if (whoPart.get(0).equals("@" + botName)) {
            whoPart.remove(0);
        }
    }

}
