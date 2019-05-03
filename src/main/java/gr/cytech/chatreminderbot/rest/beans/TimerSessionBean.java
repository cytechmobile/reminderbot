package gr.cytech.chatreminderbot.rest.beans;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import gr.cytech.chatreminderbot.rest.controlCases.Client;
import gr.cytech.chatreminderbot.rest.controlCases.Reminder;
import gr.cytech.chatreminderbot.rest.db.Dao;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.nlp.parse.DateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class TimerSessionBean {
    private static final Logger logger = LoggerFactory.getLogger(TimerSessionBean.class);

    @Inject
    FlywayMigration flywayMigration;

    @Inject
    public Dao dao;

    protected ScheduledExecutorService timerService;
    protected Client client;
    protected Future nextReminderFuture;

    void startup(@Observes StartupEvent ev) {
        logger.info("Run from start set next reminder");
        timerService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(false).setNameFormat("reminder-timer-%d").build());
        timerService.submit(this::programmaticTimeout);
        logger.info("Reminder Timer started");
    }

    void shutdown(@Observes ShutdownEvent e) {
        this.stopTimer();
        if (timerService != null) {
            timerService.shutdownNow();
        }
    }

    public void setTimerForReminder(Reminder reminder) {
        timerService.submit(this::programmaticTimeout);
    }

    /*
     * This should *ONLY* be called by a single thread
     * The one in the single-threaded scheduled executor
     */
    @Transactional
    protected void programmaticTimeout() {
        if (!flywayMigration.migrationCompleted()) {
            setTimer(Instant.now().plusSeconds(1));
            return;
        }

        stopTimer();

        try {
            while (true) {
                Optional<Reminder> orem = dao.findNextPendingReminder();
                if (orem.isEmpty()) {
                    break;
                }
                Reminder r = orem.get();
                try {
                    //Sends message
                    if (client == null) {
                        client = Client.newClient(dao);
                    }
                    client.sendAsyncResponse(r);
                    logger.info("Sent reminder {}", r);
                } catch (Exception e) {
                    logger.error("error sending reminder. consider changing the buttonUrl and the googlePrivateKey", e);
                }

                //Removes the reminder
                dao.remove(r);
                logger.info("Deleted reminder at: {}", r.getWhen());
                if (r.isRecuring()) {
                    String timezone = dao.getUserTimezone(r.getSenderDisplayName());
                    ZoneId zoneId = ZoneId.of(timezone);
                    TimeZone setTimeZone = TimeZone.getTimeZone(timezone);

                    PrettyTimeParser prettyTimeParser = new PrettyTimeParser(setTimeZone);
                    List<DateGroup> parse = prettyTimeParser.parseSyntax(r.getFullText());

                    Instant when = Instant.ofEpochMilli(parse.get(0).getDates().get(0).getTime());
                    r.setWhen(when.atZone(zoneId));
                    logger.info("Saving again recurring reminder");
                    dao.persist(r);

                }
            }
        } catch (Exception e) {
            logger.warn("exception caught during reminder check timeout", e);
        }

        try {
            Optional<Reminder> or = dao.findNextReminder();
            if (or.isPresent()) {
                Reminder r = or.get();
                this.setTimer(r.getWhen().toInstant());
            } else {
                logger.info("No reminders found, not scheduling anything");
            }
        } catch (Exception e) {
            logger.warn("error finding next reminder to set up schedule", e);
            setTimer(Instant.now().plusSeconds(1));
        }
    }

    //Stops if any timer is running in background
    protected void stopTimer() {
        if (this.nextReminderFuture != null) {
            logger.info("Stopping existing scheduled task");
            this.nextReminderFuture.cancel(false);
            this.nextReminderFuture = null;
        }
    }

    protected void setTimer(Instant time) {
        this.stopTimer();

        Instant now = Instant.now();
        if (time.isBefore(now)) {
            time = now.plusSeconds(1);
        }
        Duration d = Duration.between(now, time);
        this.nextReminderFuture = timerService.schedule(this::programmaticTimeout,
                d.toMillis(), TimeUnit.MILLISECONDS);
        logger.info("scheduled reminder to run in {}", d.toString());
    }
}
