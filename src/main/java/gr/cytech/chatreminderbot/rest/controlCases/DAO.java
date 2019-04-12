package gr.cytech.chatreminderbot.rest.controlCases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;

public class DAO {
    private final static Logger logger = LoggerFactory.getLogger(DAO.class);

    public String getBotName(EntityManager entityManager){
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
    public String getUserTimezone(String userId, EntityManager entityManager) {
        String timezone = "";
        try {
            timezone = entityManager.createNamedQuery("get.spesificTimezone", TimeZone.class)
                    .setParameter("userid", userId)
                    .getSingleResult().getTimezone();
            logger.info("timezone found: {}",timezone);
        } catch (NoResultException e) {
            try{
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

    public Configurations getConfigurationValue(String config, EntityManager entityManager){
        Configurations getConfigurationValue = entityManager
                .createNamedQuery("get.configurationByKey", Configurations.class)
                .setParameter("configKey", config)
                .getSingleResult();
        return getConfigurationValue;
    }
}
