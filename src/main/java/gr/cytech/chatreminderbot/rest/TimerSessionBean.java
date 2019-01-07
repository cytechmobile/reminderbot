package gr.cytech.chatreminderbot.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;


@Singleton
@Startup
public class TimerSessionBean {
    @Resource
    TimerService timerService;

    //Inject botResource in order to get EntityManager
    @Inject
    BotResource botResource;

    ZonedDateTime nextReminderDate;


    private final static Logger logger = LoggerFactory.getLogger(Client.class.getName());

    public void setTimer(Reminder reminder) {
        TimerConfig timerConfig = new TimerConfig();
        //PERSISTENCE
        timerConfig.setPersistent(true);

        logger.info("Stop any timer before set new");
        this.stopTimer();
        // Date when = Date.from(reminder.getWhen().atZone(ZoneId.systemDefault()).toInstant());
        //TODO use of timezone #1

        //Gets when with timezone given by Db - user
        Date when = Date.from(reminder.getWhen().toInstant());
        Timer timer = timerService.createSingleActionTimer(when, timerConfig);
    }


    @Timeout
    public void programmaticTimeout(Timer timer) {

        List<Reminder> reminders = botResource.getEntityManager().
                createNamedQuery("reminder.findNextReminder", Reminder.class).getResultList();

        if (reminders.isEmpty()) {
            logger.info("Empty reminders no -----next reminder");
        } else {
            //Sends message
            Client client = new Client();
            client.sendAsyncResponse(reminders.get(0));
            logger.info("Send Message ");


            //Removes old reminder
            Reminder oldReminder = botResource.getEntityManager().find(Reminder.class, reminders.get(0).getReminderId());
            logger.info("Deleted reminder at: {}", oldReminder.getWhen());
            botResource.getEntityManager().remove(oldReminder);

            //Gets next reminder if exists
            if (reminders.size() > 1) {
                this.setNextReminder(reminders.get(1), reminders.get(1).getWhen());
            } else {
                logger.info("Empty reminders no next reminder");
                this.setNextReminderDate(null);

            }
        }


    }

    //Stops if any timer is running in backround
    public void stopTimer() {
        for (Object obj : timerService.getTimers()) {
            Timer t = (Timer) obj;
            t.cancel();
        }
    }


    //Sets the nextTimer - and its Date
    public void setNextReminder(Reminder newNextReminder, ZonedDateTime dateTime) {
        this.setTimer(newNextReminder);
        this.setNextReminderDate(dateTime);

    }

    public ZonedDateTime getNextReminderDate() {
        return nextReminderDate;
    }

    public void setNextReminderDate(ZonedDateTime nextReminderDate) {
        this.nextReminderDate = nextReminderDate;
    }


}