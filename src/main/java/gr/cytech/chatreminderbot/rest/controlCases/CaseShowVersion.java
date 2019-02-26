package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class CaseShowVersion {
    private static final Logger logger = LoggerFactory.getLogger(CaseShowReminders.class);

    private Request request;

    public Request getRequest() {
        return request;
    }

    public String showVersion(Request request) throws Exception {
        this.request = request;
        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));

        return "Hi my version right now is: " + properties.getProperty("version");

    }
}


