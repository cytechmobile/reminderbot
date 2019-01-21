package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.controlCases.Client;
import gr.cytech.chatreminderbot.rest.controlCases.Reminder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ClientIT {
    private static final Logger logger = LoggerFactory.getLogger(ClientIT.class);

    @Test
    void manualSendTest() {
        Client client = new Client();
        String who = "users/102853879309256732507";
        String what = " use  character  `'` only two times. One before and one after what, type Help for example.";
        ZonedDateTime when = ZonedDateTime.now(ZoneId.of("Europe/Athens")).plusMinutes(1);
        String spaceID = "AAAADvB8eGY";
        String threadID = "wk-fzcPcktM";

        Reminder reminder = new Reminder(what, when, who, spaceID, threadID);
       // client.sendAsyncResponse(reminder);
    }
}
