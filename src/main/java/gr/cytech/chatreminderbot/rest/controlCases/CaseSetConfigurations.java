package gr.cytech.chatreminderbot.rest.controlCases;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

public class CaseSetConfigurations {
    @PersistenceContext(name = "wa")
    public EntityManager entityManager;

    private List<String> splitMsg;

    @Transactional
    public String configurationController(List<String> splitMsg) {
        this.splitMsg = splitMsg;
        if (splitMsg.size() == 1) {
            return configCommand();
        }

        if (splitMsg.get(1).equals("set") && splitMsg.size() == 4) {
            return caseSetConfiguration();
        }
        return errorMessage();
    }

    public String caseSetConfiguration() {
        Configurations newConfiguration = new Configurations(splitMsg.get(2), splitMsg.get(3));
        entityManager.merge(newConfiguration);
        return "Updated configuration to " + newConfiguration.getValue() + " with key " + newConfiguration.getKey();
    }

    public String configCommand() {
        List<Configurations> getAllConfigurations = entityManager
                .createNamedQuery("get.allConfigurations", Configurations.class).getResultList();
        // adding multiply whitespaces instead of just pressing space in the string
        String multiplyWhiteSpaces = String.format("%-24s", " ");
        StringBuilder allConfigs = new StringBuilder("key" + multiplyWhiteSpaces + "value \n");
        for (Configurations k : getAllConfigurations) {
            allConfigs.append("<b>").append(k.getKey())
                    .append("</b> ").append(" --> ").append(k.getValue()).append(" \n");
        }
        return "the configurations right now are: \n " + allConfigs;
    }

    public String errorMessage() {
        return "use `config set key value` for adding a configuration in database "
                + "or `config` to list the configurations";
    }
}
