package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.db.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Startup
@Singleton
public class TimerSessionBean {
    private static final Logger logger = LoggerFactory.getLogger(TimerSessionBean.class);

    private static final String CHECK_TIMER_MARKER = "check_timer_marker";

    @Resource
    TimerService timerService;

    public Client client;

    @Inject
    protected Dao dao;

    public ZonedDateTime nextReminderDate;

    @PostConstruct
    void reset() {
        logger.info("Run from start set next reminder");
        try {
            List<Reminder> reminders = dao.findNextReminder();
            if (!reminders.isEmpty()) {
                logger.info("Sets next reminder from db");
                this.setNextReminder(reminders.get(0), reminders.get(0).getWhen());
            }
        } catch (Exception e) {
            // this is most probably due to Flyway running AFTER EJB @Singleton with @Startup:
            // https://issues.jboss.org/browse/THORN-827
            logger.warn("error creating timer session bean. Perhaps due to Flyway not run yet?!", e);
            timerService.createSingleActionTimer(10_000, new TimerConfig(CHECK_TIMER_MARKER, false));
        }
    }

    public void setTimer(Reminder reminder) {
        TimerConfig timerConfig = new TimerConfig();
        //PERSISTENCE
        timerConfig.setPersistent(true);

        logger.info("Stop any timer before set new");
        this.stopTimer();

        //Gets when with timezone given by Db - user
        Date when = Date.from(reminder.getWhen().toInstant());
        Timer timer = timerService.createSingleActionTimer(when, timerConfig);
    }

    @Timeout
    public void programmaticTimeout(Timer timer) {
        List<Reminder> reminders = dao.findNextReminder();

        if (reminders.isEmpty()) {
            logger.info("Empty reminders no -----next reminder");
        } else {
            try {
                //Sends message
                client = Client.newClient(dao);
                client.sendAsyncResponse(reminders.get(0));
                logger.info("Send Message ");
            } catch (Exception e) {
                logger.error("consider change the buttonUrl and the googlePrivateKey",e);
            }
            //Removes old reminder

            Reminder oldReminder = dao.findOldReminder(reminders.get(0).getReminderId());
            logger.info("Deleted reminder at: {}", oldReminder.getWhen());
            dao.remove(oldReminder);

            //Gets next reminder if exists
            if (reminders.size() > 1) {
                this.setNextReminder(reminders.get(1), reminders.get(1).getWhen());
            } else {
                logger.info("Empty reminders no next reminder");
                this.setNextReminderDate(null);
            }
        }
    }

    //Stops if any timer is running in background
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
