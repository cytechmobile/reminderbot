package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Path("/services")
public class BotResource {

    private final static Logger logger = LoggerFactory.getLogger(BotResource.class.getName());
    public static final String BOT_NAME_ENV = "BOT_NAME";
    public static final String BOT_NAME = System.getProperty(BOT_NAME_ENV,
            System.getenv().
                    getOrDefault(BOT_NAME_ENV, "reminder"));


    @PersistenceContext(name = "wa")
    protected EntityManager entityManager;

    //Provides entityManager to other classes(TimerSession)
    public EntityManager getEntityManager() {
        return entityManager;
    }

    String timeZone;

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    //Gets the class timerSessionBean in order to set reminders
    @Inject
    TimerSessionBean timerSessionBean;

    //------------Check request from google chat -------------------

    @POST
    @Path("/handleReq")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public String handleReq(Request req) {
        //Pass json as class request

        //Gets space_id and thread_id of the message,
        //they needed in order to response in the same space/thread
        String space_id = req.getMessage().getThread().getName().split("/")[1];
        String thread_id = req.getMessage().getThread().getName().split("/")[3];
        logger.debug("Text:{}", req.getMessage().getText());

        String help = "----- Instructions using the reminder bot -----  \n " +
                "1) Set a reminder:   \n " +
                " reminder me 'what' at 16/3/2018 16:00 \n" +
                " *Instead of me, in a room you can mention any other user. \n" +
                "2) Set a reminder in another room(the bot must be invited first) and notify all\n " +
                " reminder #roomName 'what' at 16/3/2018 16:00 \n" +
                "3) Set your team timezone by typing for example: \n" +
                " timezone Athens  by default is GMT \n" +
                "4) Set your timezone by typing for example: \n" +
                " mytimezone Athens default is GMT \n" +
                "5) Show your reminders by typing: \n" +
                " myreminders \n" +
                "6) Delete your reminder by typing: \n" +
                " delete (reminder id) \n" +
                " **If you are in a room you must mention the bot using @, then talk to it**\n";

        //---- Retrieve e message
        String[] splitedMsg = req.getMessage().getText().split("\\s+");
        //----- Response with instructions
        if (splitedMsg[0].equals("help") ||
                (splitedMsg[0].equals("@" + BOT_NAME) && splitedMsg[1].equals("help"))) {
            return responseBuild(help, space_id);
        }
        ///
        ///
        ///
        ///-------Case create Reminder -------------------/////////
        ///
        ///
        ///
        //Checks if message is long enough and if starts with reminder
        if (req.getMessage().getText().length() > 10 &&
                splitedMsg[0].equals("reminder") ||
                (splitedMsg[0].equals("@" + BOT_NAME) && splitedMsg[1].equals("reminder"))) {

            //Get what to remind
            String what = extractWhat(req);
            logger.debug("what: {}", what);

            //Gets who
            String who = extractWho(req);

            //find this DisplayName ID
            //Give who that id
            logger.debug("WHO : {}", who);

            //Gets when to remind
            String when = extractReminderDate(req);
            logger.debug("When: {}", when);

            //Checks if when is correct form
            if (!(isValidDate(when))) {
                return responseBuild("Wrong date or Timezone format.\n" +
                        " DateFormat must be: dd/MM/yyyy HH:mm . \n" +
                        " For a timezone format you can also use GMT", space_id);
            }
            //pass from string to date for this users timezone
            ZonedDateTime inputDate = dateForm(when);

            //Check if date is in past who this user Timezone
            if (!(isValidDate(inputDate))) {
                return responseBuild("This date has passed " + inputDate +
                        ". Check your timezone or insert in the current reminder", space_id);
            }

            logger.debug("timezone is set:{}", getTimeZone());
            //Saves the reminder what - when - who - which timezone -  from where
            Reminder reminder = new Reminder(what, inputDate, who, getTimeZone(), space_id, thread_id);
            entityManager.persist(reminder);


            //if there is zero reminders sets the  Reminder
            if (timerSessionBean.getNextReminderDate() == null) {
                logger.info("set NEW reminder to : {}", reminder.getWhen());
                timerSessionBean.setNextReminder(reminder, inputDate);
            } else
                //ELSE  if the new reminder is before the nextReminder,
                // changes as next reminder this
                if (checkIfTimerNeedsUpdate(inputDate)) {
                    logger.info("CHANGE next reminder to: {}", reminder.getWhen());
                    timerSessionBean.setNextReminder(reminder, inputDate);
                }

            //Response that was creating success and the time to notify the user
            String successMsg = "Reminder: <<" + what +
                    ">> saved succesfully and will notify you in: " +
                    calculateRemainingTime(inputDate);

            return responseBuild(successMsg, space_id);

        } else
            ///
            ///
            ///
            ///------------Case Set Team timezone -------------------/////////
            ///
            ///
            ///
            ///
            if (req.getMessage().getText().length() > 7 &&
                    splitedMsg[0].equals("timezone") ||
                    (splitedMsg[0].equals("@" + BOT_NAME) && splitedMsg[1].equals("timezone"))) {

                //Checks given timezone
                if (extractTimeZone(req) == null) {
                    String wrongTimeZone = "Given timezone is wrong, try again.";
                    return responseBuild(wrongTimeZone, space_id);
                }

                TimeZone defaultTimeZone = new TimeZone(extractTimeZone(req), "default");

                if (getGivenTimeZone("default").equals("")) {
                    entityManager.persist(defaultTimeZone);
                } else {
                    Query query = entityManager.createNamedQuery("set.timezone")
                            .setParameter("timezone", defaultTimeZone.getTimezone())
                            .setParameter("userid", "default");
                    query.executeUpdate();
                }
                String resp = "You successfully set the default timezone at:" + getGivenTimeZone("default");
                return responseBuild(resp, space_id);
            } else if (req.getMessage().getText().length() > 8 &&
                    splitedMsg[0].equals("mytimezone") ||
                    (splitedMsg[0].equals("@" + BOT_NAME) && splitedMsg[1].equals("mytimezone"))) {


                String who = req.getMessage().getSender().getName();

                //Checks given timezone
                if (extractTimeZone(req) == null) {
                    String wrongTimeZone = "Given timezone is wrong, try again.";
                    return responseBuild(wrongTimeZone, space_id);
                }

                TimeZone timeZone = new TimeZone(extractTimeZone(req), who);


                if (getGivenTimeZone(who).equals("")) {
                    entityManager.persist(timeZone);
                } else {
                    Query query = entityManager.createNamedQuery("set.timezone")
                            .setParameter("timezone", timeZone.getTimezone())
                            .setParameter("userid", who);
                    query.executeUpdate();
                }
                String resp = " <" + who + "> successfully set your timezone at:" + timeZone.getTimezone();
                return responseBuild(resp, space_id);
            } else
                ///
                ///
                ///
                ///------------Case Show my reminders -------------------/////////
                ///
                ///
                ///
                ///
                if (req.getMessage().getText().length() > 8 &&
                        splitedMsg[0].equals("myreminders") ||
                        (splitedMsg[0].equals("@" + BOT_NAME) && splitedMsg[1].equals("myreminders"))) {

                    String who = req.getMessage().getSender().getName();
                    String resp = showReminders(who);
                    return responseBuild(resp, space_id);
                } else
                    ///
                    ///
                    ///
                    ///------------Case Delete reminder -------------------/////////
                    ///
                    ///
                    ///
                    ///
                    if (req.getMessage().getText().length() > 8 &&
                            splitedMsg[0].equals("delete") ||
                            (splitedMsg[0].equals("@" + BOT_NAME) && splitedMsg[1].equals("delete"))) {
                        String reminderId;
                        if (splitedMsg.length == 2 && splitedMsg[1].matches("[0-9]+") ) {
                            reminderId = splitedMsg[1];
                        } else if (splitedMsg.length == 3 && splitedMsg[2].matches("[0-9]+")) {
                            reminderId = splitedMsg[2];
                        } else {
                            return responseBuild("Wrong command format type help to see the right", space_id);
                        }
                        String who = req.getMessage().getSender().getName();
                        String resp = deleteReminder(reminderId, who);
                        return responseBuild(resp, space_id);
                    } else
                    ///
                    ///
                    ///
                    ///------------Case Default -------------------/////////
                    ///
                    ///
                    ///
                    ///
                    {
                        logger.info("Default ");
                        String responseDefault = "I didnt understand you, type help for instructions \n";
                        return responseBuild(responseDefault, space_id);
                    }
    }

    //True if inputDate is before the next reminderDate so it needs to change
    public boolean checkIfTimerNeedsUpdate(ZonedDateTime inputDate) {
        if (inputDate.isBefore(timerSessionBean.getNextReminderDate())) {
            return true;
        } else {
            return false;
        }
    }

    //Builds a directly response
    private String responseBuild(String message, String space_id) {
        return "{ \"text\": \"" + message + "\" ,  \"thread\": { \"name\": \"spaces/" + space_id + "\" }}";
    }

    //Returns date from string, based on dd/MM/yyyy HH:mm format,
    //Is called after we ensure this is the current format
    ZonedDateTime dateForm(String when) {
        String format = "dd/MM/yyyy HH:mm";
        DateTimeFormatter fomatter = DateTimeFormatter.ofPattern(format);
        return ZonedDateTime.parse(when, fomatter.withZone(ZoneId.of(getTimeZone())));
    }

    //Check if given date in string is in valid format
    boolean isValidDate(String value) {
        String format = "dd/MM/yyyy HH:mm";
        LocalDateTime ldt;
        DateTimeFormatter fomatter = DateTimeFormatter.ofPattern(format);
        //Checks format
        try {
            ldt = LocalDateTime.parse(value, fomatter);
            String result = ldt.format(fomatter);
            return result.equals(value);
        } catch (DateTimeParseException e) {
            try {
                LocalDate ld = LocalDate.parse(value, fomatter);
                String result = ld.format(fomatter);
                return result.equals(value);
            } catch (DateTimeParseException exp) {
                try {
                    LocalTime lt = LocalTime.parse(value, fomatter);
                    String result = lt.format(fomatter);
                    return result.equals(value);
                } catch (DateTimeParseException e2) {
                    // logger.error("Error Parse LocalDateTime:{}", value, e);

                }
            }
        }


        return false;
    }

    //Check if given date is not in the past
    boolean isValidDate(ZonedDateTime inputDate) {
        if (inputDate.isBefore(ZonedDateTime.now(ZoneId.of(getTimeZone())))) {
            return false;
        } else {
            return true;
        }
    }

    //Calculates the reminding time after setting a reminder
    String calculateRemainingTime(ZonedDateTime inputDate) {
        //TODO use of timezone #2
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
            return months + " Months,  " +
                    days + " Days, " +
                    hours + " Hours, " +
                    minutes + " Minutes";
        }
        if (days > 0) {
            return days + " Days, " +
                    hours + " Hours, " +
                    minutes + " Minutes";
        }
        if (hours > 0) {
            return hours + " Hours, " +
                    minutes + " Minutes";
        }

        return minutes + " Minutes";
    }

    //Extracts Date, also checks if timezone is applied
    //if no, checks if user have set timezone
    //if no uses the default timezone
    String extractReminderDate(Request request) {
        String partOfDate = request.getMessage().getText().split("'")[2];
        String message[] = partOfDate.split("\\s+");
        String when = "";

        for (int i = 0; i < message.length; i++) {
            if (message[i].equals("at")) {
                when += message[i + 1];
                when += " " + message[i + 2];
                if (message.length == 5) {
                    String zone = findTimeZones(message[i + 3]);
                    if (zone != null) {
                        setTimeZone(zone);
                        logger.info("SET timeZone became: {}", zone);
                    } else {
                        when += "Make fail date format.";
                    }
                } else {
                    String userTimezone = getGivenTimeZone(request.getMessage().getSender().getName());
                    if (!(userTimezone.equals(""))) {
                        logger.info("SET timeZone of the user default found: {}", userTimezone);
                        setTimeZone(userTimezone);
                    } else {
                        logger.info("SET timeZone  Default");
                        setTimeZone(getGivenTimeZone("default"));
                    }

                }
            }
        }
        return when;
    }

    //Extracts what to remind from request
    String extractWhat(Request request) {
        String what = request.getMessage().getText().split("'")[1];
        logger.debug("what: {}", what);
        return what;
    }

    //Extract timezone from request -
    String extractTimeZone(Request request) {
        String message[] = request.getMessage().getText().split("\\s+");
        String timeZone = null;

        for (int i = 0; i < message.length; i++) {
            if (message[i].equals("timezone") || message[i].equals("mytimezone")) {
                timeZone = findTimeZones(message[i + 1]);
            }
        }
        return timeZone;
    }

    //Retrieves from db users timezone if not found returns ""
    @Transactional
    String getGivenTimeZone(String user) {
        List<TimeZone> timeZones = entityManager.
                createNamedQuery("get.Alltimezone", TimeZone.class).getResultList();
        if (timeZones.isEmpty()) {
            logger.debug("timezones not found return - ");
            return "";
        } else {
            for (TimeZone z : timeZones) {
                if (z.getUserid().equals(user)) {
                    logger.debug("found zone: {}", z.getTimezone());
                    return z.getTimezone();
                }
            }
            logger.debug("Didnt find zone for this user");
            return "";
        }
    }

    //Cases checks who to notify:
    // 1) me - returns <user/id>
    // 2)#RoomName (with @all) - returns roomsName
    // 3) @Firstname Lastname - returns <user/id>
    // 4) @all - returns <user/all>
    String extractWho(Request request) {
        String[] splited = request.getMessage().getText().split("\\s+");
        String who = "";
        String displayName = "";
        String spaceId = request.getMessage().getThread().getName().split("/")[1];
        if (splited[0].equals("reminder")) {
            // 1) me
            if (splited[1].equals("me")) {
                //  ---- takes the ID of the sender ---
                who = request.getMessage().getSender().getName();
            } else {
                if (splited[1].startsWith("#")) {
                    // 2)#RoomName
                    displayName = splited[1];
                } else {
                    // 3) @Firstname Lastname
                    displayName = splited[1].substring(1) + " " + splited[2];
                }
                who = findIdUserName(displayName, spaceId);
            }
        } else {
            if (splited[0].equals("@" + BOT_NAME)) {
                // 1) me
                if (splited[2].equals("me")) {
                    //  ---- takes the ID of the sender ---
                    who = request.getMessage().getSender().getName();
                } else if (splited[2].equals("@all")) {
                    //  ---- takes the ID of the sender ---
                    who = "users/all";
                } else {
                    if (splited[2].startsWith("#")) {
                        // 2)#RoomName
                        displayName = splited[2];
                    } else {
                        // 3) @Firstname Lastname
                        displayName = splited[2].substring(1) + " " + splited[3];
                    }
                    who = findIdUserName(displayName, spaceId);
                }
            }
        }

        return who;
    }

    //Finds timezone full format, if not found return null
    String findTimeZones(String inputTimeZone) {
        List<String> zones = new ArrayList<>();
        zones.addAll(ZoneId.getAvailableZoneIds());
        for (String z : zones) {
            String[] splitted=z.split("/");
            if (z.equalsIgnoreCase(inputTimeZone) ||
                    splitted[splitted.length-1].equalsIgnoreCase(inputTimeZone)) {
                logger.info("Found: {} ", z);
                return z;
            }
        }
        return null;
    }

    //Returns a list of WHO reminders, if not found returns not found message
    @Transactional
    String showReminders(String who) {

        List<Reminder> reminders = entityManager.
                createNamedQuery("reminder.showReminders", Reminder.class)
                .setParameter("userid", who)
                .getResultList();

        String remindersShow = "---- Reminders that will notify you ---- \n";
        if (reminders.isEmpty()) {
            logger.debug("timezones not found return - ");
            return "---- Reminders not found ---";
        } else {
            for (int i = 0; i < reminders.size(); i++) {
                remindersShow += i + 1 + ") ID:" + reminders.get(i).getReminderId() + " what:' " + reminders.get(i).getWhat() + " ' When: " +
                        reminders.get(i).getWhen().withZoneSameLocal(ZoneId.of(reminders.get(i).getReminderTimezone())).getDayOfMonth() + " of " +
                        reminders.get(i).getWhen().withZoneSameLocal(ZoneId.of(reminders.get(i).getReminderTimezone())).getMonth() + " at " +
                        reminders.get(i).getWhen().withZoneSameLocal(ZoneId.of(reminders.get(i).getReminderTimezone())).getHour() + ":" +
                        String.format("%02d",  reminders.get(i).getWhen().withZoneSameLocal(ZoneId.of(reminders.get(i).getReminderTimezone())).getMinute())+ " "+
                        reminders.get(i).getReminderTimezone() + "\n";
            }
            return remindersShow;
        }
    }

    @Transactional
    String deleteReminder(String reminderId, String who) {

        if ((entityManager.find(Reminder.class, Integer.parseInt(reminderId)) != null)) {
            List<Reminder> reminders = entityManager.
                    createNamedQuery("reminder.findByUserAndReminderId", Reminder.class)
                    .setParameter("userId", who)
                    .setParameter("reminderId", Integer.parseInt(reminderId))
                    .getResultList();
            if (reminders.isEmpty()) {
                return "Couldn't find reminder with id: " + reminderId;
            }
            Reminder oldReminder = entityManager.find(Reminder.class, Integer.parseInt(reminderId));
            logger.debug("Deleted reminder with ID: {}", oldReminder.getReminderId());
            entityManager.remove(oldReminder);
            return "Reminder with ID: " + oldReminder.getReminderId() + " successfully deleted!";
        } else {
            return "Couldn't find reminder with id: " + reminderId;
        }
    }

    //Gets usersID by his displayName in order to notify him properly
    String findIdUserName(String displayName, String spaceId) {
        Client client = new Client();
        Map<String, String> users = client.getListOfMembersInRoom(spaceId);
        //if displayName not found then just save the name as it is
        return users.getOrDefault(displayName, displayName);
    }
}
