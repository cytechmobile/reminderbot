package gr.cytech.chatreminderbot.rest.controlCases;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class CaseSetConfigurations {
    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    private ArrayList<String> splitMsg;

    public void setSplitMsg(ArrayList<String> splitMsg) {
        this.splitMsg = splitMsg;
    }

    @Transactional
    public String configurationController() {
        if (splitMsg.size() == 1) {
            return helpCommand();
        }

        if (splitMsg.get(1).equals("buttonUrl") && splitMsg.size() == 3) {
            return caseSetBotUrl();
        }
        return errorMessage();
    }

    public String caseSetBotUrl() {
        Query query = entityManager.createNamedQuery("set.buttonUrl")
                .setParameter("urlKey", "buttonUrl")
                .setParameter("urlValue", splitMsg.get(2));
        if (doesUrlExist()) {
            query.executeUpdate();

        } else {
            Configurations newButtonUrl = new Configurations("buttonUrl", splitMsg.get(2));
            entityManager.persist(newButtonUrl);
        }

        return "Updated url to " + splitMsg.get(2);
    }

    public boolean doesUrlExist() {
        return entityManager.createNamedQuery("get.buttonUrl", Configurations.class)
                .getResultList().size() == 1;
    }

    public String helpCommand() {
        List<Configurations> test = entityManager
                .createNamedQuery("get.allConfigurations", Configurations.class).getResultList();
        // adding multiply whitespaces instead of just pressing space in the string
        String multiplyWhiteSpaces = String.format("%-24s", " ");
        StringBuilder allConfigs = new StringBuilder("key" + multiplyWhiteSpaces + "value \n");
        for (Configurations k : test) {
            allConfigs.append(k.getKey()).append(" --> ").append(k.getValue()).append(" \n");
        }
        return "the configurations right now are: \n " + allConfigs;
    }

    public String errorMessage() {
        return "no configuration for the specific name check config to see whats available";
    }
}
