package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.db.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

public class CaseSetConfigurations {
    private static final Logger logger = LoggerFactory.getLogger(Control.class);

    @Inject
    Dao dao;

    private List<String> splitMsg;

    @Transactional
    public String configurationController(List<String> splitMsg) {
        this.splitMsg = splitMsg;
        if (splitMsg.size() == 1) {
            return configCommand();
        }
        if (splitMsg.get(1).equals("set")) {
            return caseSetConfiguration();
        }
        return errorMessage();
    }

    public String caseSetConfiguration() {
        StringBuilder valueForConfigurations = new StringBuilder();
        for (int i = 0; i < splitMsg.size(); i++) {
            if (i >= 3) {
                valueForConfigurations.append(splitMsg.get(i)).append(" ");
            }
        }
        valueForConfigurations.deleteCharAt(valueForConfigurations.length() - 1);
        logger.info("the updated String is {}", valueForConfigurations);
        Configurations newConfiguration = new Configurations(splitMsg.get(2), valueForConfigurations.toString());
        newConfiguration = dao.merge(newConfiguration);
        return "Updated configuration to " + newConfiguration.getValue() + " with key " + newConfiguration.getKey();
    }

    public String configCommand() {
        List<Configurations> configs = dao.getAllConfigurations();
        // adding multiply whitespaces instead of just pressing space in the string
        String multiplyWhiteSpaces = String.format("%-24s", " ");
        StringBuilder allConfigs = new StringBuilder("key" + multiplyWhiteSpaces + "value \n");
        for (Configurations k : configs) {
            String val = k.getValue();
            if (k.getKey().toLowerCase().contains("key")) {
                val = "***";
            }
            allConfigs.append("<b>").append(k.getKey())
                    .append("</b> ").append(" --> ").append(val).append(" \n");
        }
        return "the configurations right now are: \n " + allConfigs;
    }

    public String errorMessage() {
        return "use `config set key value` for adding a configuration in database "
                + "or `config` to list the configurations";
    }
}
