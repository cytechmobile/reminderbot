package gr.cytech.chatreminderbot.rest.db;

import gr.cytech.chatreminderbot.rest.controlCases.Configurations;
import gr.cytech.chatreminderbot.rest.controlCases.Reminder;
import gr.cytech.chatreminderbot.rest.controlCases.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class Dao {

    private static final Logger logger = LoggerFactory.getLogger(Dao.class);

    @PersistenceContext(name = "wa")
    EntityManager entityManager;

    public String getBotName() {
        String botName = "";
        try {
            botName = entityManager.createNamedQuery("get.configurationByKey", Configurations.class)
                    .setParameter("configKey", "BOT_NAME")
                    .getSingleResult().getValue();
        } catch (NoResultException e) {
            logger.warn("no result found with key BOT_NAME please consider change it");
            botName = "CHANGE-ME";
        }
        return botName;
    }

    @Transactional
    public String getUserTimezone(String userId) {
        String timezone = "";
        try {
            timezone = entityManager.createNamedQuery("get.spesificTimezone", TimeZone.class)
                    .setParameter("userid", userId)
                    .getSingleResult().getTimezone();
            logger.info("timezone found: {}", timezone);
        } catch (NoResultException e) {
            try {
                timezone = entityManager.createNamedQuery("get.spesificTimezone", TimeZone.class)
                        .setParameter("userid", "default")
                        .getSingleResult().getTimezone();
                logger.info("timezone NOT found use default: {}", timezone);
            } catch (NoResultException e1) {
                TimeZone persistedTimezone = new TimeZone("Europe/Athens", "default");
                timezone = persistedTimezone.getTimezone();
                entityManager.persist(persistedTimezone);
                logger.warn("timezone not found and default not found too save new default and save it to dabase");
            }
        }
        return timezone;
    }

    public String getConfigurationValue(String config) {
        String getConfigurationValue;
        try {
            getConfigurationValue = entityManager
                    .createNamedQuery("get.configurationByKey", Configurations.class)
                    .setParameter("configKey", config)
                    .getSingleResult().getValue();
        } catch (NoResultException e) {
            logger.error("the configuration you choose with value: {} doesnt not exist", config);
            getConfigurationValue = "NO RESULT FOUND";
        }
        return getConfigurationValue;

    }

    public void persist(Object entity) {
        entityManager.persist(entity);

    }

    public void remove(Object entity) {
        entityManager.remove(entity);
    }

    public <T> T merge(T entity) {
        return entityManager.merge(entity);
    }

    public List<Reminder> findReminders(String userId, int reminderId) {
        try {
            return entityManager
                    .createNamedQuery("reminder.findByUserAndReminderId", Reminder.class)
                    .setParameter("userId", userId)
                    .setParameter("reminderId", reminderId)
                    .getResultList();
        } catch (Exception e) {
            logger.warn("error finding reminder for user:{} with id: {}", userId, reminderId, e);
            return Collections.emptyList();
        }
    }

    public Reminder deleteReminder(int reminderId) {
        Reminder r = entityManager.find(Reminder.class, reminderId);
        entityManager.remove(r);
        return r;
    }

    public List<Configurations> getAllConfigurations() {
        try {
            return entityManager
                    .createNamedQuery("get.allConfigurations", Configurations.class).getResultList();
        } catch (Exception e) {
            logger.warn("error getting configurations", e);
            return Collections.emptyList();
        }
    }

    public List<Reminder> showReminder(String name) {
        return entityManager
                .createNamedQuery("reminder.showReminders", Reminder.class)
                .setParameter("userid", name)
                .getResultList();
    }

    public Reminder findOldReminder(int reminderId) {
        return entityManager.find(Reminder.class, reminderId);
    }

    public Optional<Reminder> findNextPendingReminder() {
        List<Reminder> rs = entityManager.createNamedQuery("reminder.findNextPendingReminder", Reminder.class)
                .setMaxResults(1)
                .getResultList();
        if (rs != null && !rs.isEmpty()) {
            return Optional.of(rs.get(0));
        }
        return Optional.empty();
    }

    public Optional<Reminder> findNextReminder() {
        List<Reminder> rs = entityManager.createNamedQuery("reminder.findNextReminder", Reminder.class)
                .setMaxResults(1)
                .getResultList();
        if (rs != null && !rs.isEmpty()) {
            return Optional.of(rs.get(0));
        }
        return Optional.empty();
    }

    public boolean defaultTimezoneExists() {
        return entityManager.createQuery("SELECT t from TimeZone t where t.userid = :default")
                .setParameter("default", "default")
                .getResultList().size() == 1;
    }
}
