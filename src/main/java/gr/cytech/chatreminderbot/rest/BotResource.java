package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;


@Path("/services")
public class BotResource {

    private final static Logger logger = LoggerFactory.getLogger(BotResource.class.getName());

    public static final String BOT_NAME_ENV = "BOT_NAME_ENV";
    public static final String BOT_NAME = System.getProperty(BOT_NAME_ENV,
            System.getenv().
                    getOrDefault(BOT_NAME_ENV, "reminder"));


    @PersistenceContext(name = "wa")
    protected EntityManager entityManager;

    //Provides entityManager to other classes(TimerSession)
    public EntityManager getEntityManager() {
        return entityManager;
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

        ///
        ///
        ///
        ///-------Case create Reminder -------------------/////////
        ///
        ///
        ///


        if (req.getMessage().getText().length() < 10) {
            String responseDefault = "bot use:reminder who 'what' at 16/3/2018 16:00 ";
            return responseBuild(responseDefault, space_id);
        }

        String[] splitedMsg = req.getMessage().getText().split("\\s+");
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


            //Checks if when is correct form or is it in past
            if (!(isValidDate(when))) {
                return responseBuild("Wrong date format -" +
                        " Or past Date. Format must be: dd/MM/yyyy HH:mm ", space_id);
            }

            //pass from string to date
            LocalDateTime inputDate = dateForm(when);

            //Saves the reminder what - when - who - from where
            Reminder reminder = new Reminder(what, inputDate, who, space_id, thread_id);
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
            ///------------Case get List of my Reminders -------------------/////////
            ///
            ///
            ///
            ///
            if (req.getMessage().getText().length() > 11 &&
                    req.getMessage().getText().substring(0, 12).equals("reminderlist") ||
                    req.getMessage().getText().substring(0, 29).equals("@" + BOT_NAME + " reminderlist ")) {
                logger.info("List of reminders: ");
                //Retrieve my reminders

                String resp = "List of reminders: ";

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
                String responseDefault = "bot use:reminder who 'what' at 16/3/2018 16:00 ";
                return responseBuild(responseDefault, space_id);
            }
    }

    //True if inputDate is before the next reminderDate so it needs to change
    public boolean checkIfTimerNeedsUpdate(LocalDateTime inputDate) {
        if (inputDate.isBefore(timerSessionBean.getNextReminderDate())) {
            return true;
        } else {
            return false;
        }
    }


    private String responseBuild(String message, String space_id) {
        return "{ \"text\": \"" + message + "\" ,  \"thread\": { \"name\": \"spaces/" + space_id + "\" }}";
    }

    //Returns date from string, based on dd/MM/yyyy HH:mm format,
    //Is called after we ensure this is the current format
    LocalDateTime dateForm(String when) {
        Locale locale = new Locale("en", "gr");
        String format = "dd/MM/yyyy HH:mm";
        DateTimeFormatter fomatter = DateTimeFormatter.ofPattern(format, locale);

        return LocalDateTime.parse(when, fomatter);
    }


    //Check if given date in string is in valid format OR valid date
    boolean isValidDate(String value) {
        Locale locale = new Locale("en", "gr");
        String format = "dd/MM/yyyy HH:mm";
        LocalDateTime ldt;
        DateTimeFormatter fomatter = DateTimeFormatter.ofPattern(format, locale);
        //Checks format
        try {
            if (LocalDateTime.parse(value, fomatter).isBefore(LocalDateTime.now())) {
                return false;
            }
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
                    logger.error("Error Parse LocalDateTime:{}", value, e);
                }
            }
        }


        return false;
    }

    String calculateRemainingTime(LocalDateTime inputDate) {
        LocalDateTime fromDateTime = LocalDateTime.now();

        LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);

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

    String extractReminderDate(Request request) {

        String message[] = request.getMessage().getText().split("\\s+");
        String when = "";

        for (int i = 0; i < message.length; i++) {
            if (message[i].equals("at")) {
                when += message[i + 1];
                when += " " + message[i + 2];
            }
        }
        return when;
    }

    String extractWhat(Request request) {
        String what = request.getMessage().getText().split("'")[1];
        logger.debug("what: {}", what);
        return what;
    }

    String extractWho(Request request) {
        String[] splited = request.getMessage().getText().split("\\s+");
        String who="";
        String displayName = "";
        String spaceId=request.getMessage().getThread().getName().split("/")[1];
        if (splited[0].equals("reminder")) {
            if (splited[1].equals("me")) {
                //  ---- takes the ID of the sender ---
                who = request.getMessage().getSender().getName();
            } else {
                //TODO Checks how many spaces have the Displayname -
                displayName = splited[1].substring(1) + " " + splited[2];
                who = findIdUserName(displayName,spaceId);
            }
        } else {
            if (splited[0].equals("@" + BOT_NAME)) {
                if (splited[2].equals("me")) {
                    //  ---- takes the ID of the sender ---
                    who = request.getMessage().getSender().getName();
                } else {
                    //TODO Checks how many spaces have the Displayname -
                    displayName = splited[2].substring(1) + " " + splited[3];
                    who = findIdUserName(displayName,spaceId);
                }
            }
        }

        return who;
    }

    //Gets usersID by his displayName in order to notify him properly
    String findIdUserName(String displayName,String spaceId){
        Client client = new Client();
      HashMap<String,String > users =  client.getListOfMembersInRoom(spaceId);
      //if displayName not found then just save the name as it is
        return users.getOrDefault(displayName,displayName);
    }
}
