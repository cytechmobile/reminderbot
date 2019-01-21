package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.controlCases.CaseShowReminders;
import gr.cytech.chatreminderbot.rest.controlCases.Reminder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseShowRemindersTest {
    private static final Logger logger = LoggerFactory.getLogger(CaseShowRemindersTest.class);

    private Reminder reminder;
    private CaseShowReminders caseShowReminders;

    @BeforeEach
    final void beforeEach() throws Exception {
        caseShowReminders = new CaseShowReminders();
        reminder = new Reminder("Do Something", ZonedDateTime.now(ZoneId.of("Europe/Athens")).plusMinutes(10),
                "DisplayName", "Europe/Athens", "uPWJ7AAAAAE", "1E_d3mjJGyM");

        reminder.setReminderId(1);
    }


    @Test
    void reminderListToStringTest() {
        ZonedDateTime nowPlusTen = ZonedDateTime.now(ZoneId.of("Europe/Athens")).plusMinutes(10);
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(reminder);
        logger.info("{}", caseShowReminders.reminderListToString(reminders));
        String expected = "1) ID:1 what:' Do Something ' When: " +
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(nowPlusTen) +
                " Europe/Athens\n";
        assertThat(caseShowReminders.reminderListToString(reminders)).isEqualTo(expected);
    }

}
