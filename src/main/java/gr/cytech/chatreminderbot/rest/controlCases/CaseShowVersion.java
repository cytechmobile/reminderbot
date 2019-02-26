package gr.cytech.chatreminderbot.rest.controlCases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class CaseShowVersion {
    private static final Logger logger = LoggerFactory.getLogger(CaseShowReminders.class);

    public String showVersion() {

        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            return "Internal error while finding my current version";
        }

        return "Hi my version right now is: " + properties.getProperty("version");

    }
}


