package gr.cytech.chatreminderbot.rest.controlCases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class CaseShowVersion {
    private static final Logger logger = LoggerFactory.getLogger(CaseShowReminders.class);

    public String showVersion() throws Exception {

        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));

        return "Hi my version right now is: " + properties.getProperty("version");

    }
}


