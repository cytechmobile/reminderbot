package gr.cytech.chatreminderbot.rest.controlCases;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;

public class CaseSetConfigurations {
    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    private ArrayList<String> splitMsg;

    public void setSplitMsg(ArrayList<String> splitMsg) {
        this.splitMsg = splitMsg;
    }

    @Transactional
    public String configurationController() {
        if (splitMsg.get(2).equals("button") && splitMsg.get(3).equals("url")
                && splitMsg.size() == 5) {
            return caseSetBotUrl();
        }
        return errorMessage();
    }

    public String caseSetBotUrl() {
        Query query = entityManager.createNamedQuery("set.buttonUrl")
                .setParameter("urlKey", "buttonUrl")
                .setParameter("urlValue", splitMsg.get(4));
        query.executeUpdate();
        return "Updated url to " + splitMsg.get(4);
    }

    public String errorMessage() {
        return "no configuration for the specific name";
    }
}
