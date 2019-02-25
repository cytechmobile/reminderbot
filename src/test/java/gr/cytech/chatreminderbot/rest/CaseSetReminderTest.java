package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.controlCases.CaseSetReminder;
import gr.cytech.chatreminderbot.rest.controlCases.Client;
import gr.cytech.chatreminderbot.rest.controlCases.Reminder;
import gr.cytech.chatreminderbot.rest.controlCases.TimerSessionBean;
import gr.cytech.chatreminderbot.rest.message.Message;
import gr.cytech.chatreminderbot.rest.message.Request;
import gr.cytech.chatreminderbot.rest.message.Sender;
import gr.cytech.chatreminderbot.rest.message.ThreadM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.persistence.EntityManager;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CaseSetReminderTest {

    public CaseSetReminder caseSetReminder;

    private Request request;
    private Message message;
    private Client client;

    @BeforeEach
    final void beforeEach() {
        request = new Request();
        message = new Message();

        String spaceId = "SPACE_ID";
        String threadId = "THREAD_ID";

        TimerSessionBean timerSessionBean = mock(TimerSessionBean.class);
        Client client = mock(Client.class);
        caseSetReminder = new CaseSetReminder();
        caseSetReminder.entityManager = mock(EntityManager.class);
        caseSetReminder.timerSessionBean = timerSessionBean;
        caseSetReminder.client = client;
        ThreadM thread = new ThreadM();

        thread.setName("spaces/" + spaceId + "/thread/" + threadId + "");
        Sender sender = new Sender();
        sender.setName("MyName");
        message.setSender(sender);
        message.setThread(thread);

        Reminder reminder = new Reminder("Do Something", ZonedDateTime.now(ZoneId.of("Europe/Athens"))
                .plusMinutes(10), "DisplayName", "Europe/Athens", spaceId, threadId);

        reminder.setReminderId(1);
        when(timerSessionBean.getNextReminderDate()).thenReturn(reminder.getWhen());
    }

    @Test
    void dateFormTest() throws Exception {
        ZonedDateTime curr = ZonedDateTime.now(ZoneId.of("Europe/Athens")).truncatedTo(ChronoUnit.MINUTES);
        String inOneHour = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(curr);
        caseSetReminder.setTimeZone("Europe/Athens");
        caseSetReminder.setWhen(inOneHour);
        ZonedDateTime result = caseSetReminder.dateForm();

        assertThat(result).as("parsed date is not the expected").isEqualTo(curr);
    }

    @Test
    void setNextReminderTest() throws Exception {
        String expectedDate = "12/12/2019 12:00 athens";
        message.setText("remind me ' set next reminder Test' at " + expectedDate);
        request.setMessage(message);
        // Already set in mock a nextReminder that is to be in 10 minutes from now()
        //So this should not be set
        caseSetReminder.setRequest(request);
        caseSetReminder.setReminder();
        //Verifies that setNextReminder is called 0 times because Input reminderDate is AFTER the current
        verify(caseSetReminder.timerSessionBean, times(0))
                .setNextReminder(any(Reminder.class), any(ZonedDateTime.class));

    }

    @Test
    void persistReminder() throws Exception {

        String expectedDate = "12/12/2019 12:00 GMT-2";
        message.setText("remind me 'persist Reminder Test' at " + expectedDate);
        request.setMessage(message);
        caseSetReminder.setRequest(request);
        caseSetReminder.setReminder();

        ArgumentCaptor<Reminder> argumentCaptor = ArgumentCaptor.forClass(Reminder.class);

        verify(caseSetReminder.entityManager, times(1)).persist(argumentCaptor.capture());

        List<Reminder> capturedReminders = argumentCaptor.getAllValues();
        assertThat(capturedReminders).as("no reminders persisted").hasSize(1);
        Reminder reminder = capturedReminders.get(0);

        // make sure persisted reminder matches our expectations
        String format = "dd/MM/yyyy HH:mm";
        DateTimeFormatter fomatter = DateTimeFormatter.ofPattern(format);

        assertThat(reminder.getWhen())
                .isEqualTo(ZonedDateTime.parse("12/12/2019 12:00", fomatter.withZone(ZoneId.of("Europe/Athens"))));
    }

    @Test
    void setInfosTest() {
        String what = "something to do";
        String who = "@Ntina trol";
        final String expectedDate = "12/12/2018 12:00";
        message.setText("remind " + who + " '" + what + "' at " + expectedDate + " athens");
        request.setMessage(message);
        caseSetReminder.setRequest(request);
        caseSetReminder.checkRemindMessageFormat();
        caseSetReminder.setInfosForRemind();

        assertThat(caseSetReminder.getWho()).as("Unexpected extracted reminder date").isEqualTo(who.substring(1));
        assertThat(caseSetReminder.getWhat()).as("Unexpected extracted reminder date").isEqualTo(what);
        assertThat(caseSetReminder.getWhen()).as("Unexpected extracted reminder date").isEqualTo(expectedDate);
    }

    @Test
    void isValidDateTest() {
        String when = "02/01/2019 15:47";

        //Check dates
        String when1 = "56/12/2018 21:40ery";
        //Check month
        String when2 = "16/32/2018 21:40";
        //Check year
        String when3 = "16/12/20178 21:40";
        //Check hour
        String when4 = "16/12/2018 45:40";
        //Check mins
        String when5 = "16/12/2018 21:66";
        //Check format
        String when6 = "1612/2018 21:66";
        String when7 = "16/12/2018 2166";
        String when8 = "16.12.2018 21.66";

        assertThat(caseSetReminder.isValidFormatDate(when)).as("not Valid  reminder date").isEqualTo(true);

        assertThat(caseSetReminder.isValidFormatDate(when1)).as("not Valid  reminder date").isEqualTo(false);

        assertThat(caseSetReminder.isValidFormatDate(when2)).as("not Valid  reminder date").isEqualTo(false);

        assertThat(caseSetReminder.isValidFormatDate(when3)).as("not Valid  reminder date").isEqualTo(false);

        assertThat(caseSetReminder.isValidFormatDate(when4)).as("not Valid  reminder date").isEqualTo(false);

        assertThat(caseSetReminder.isValidFormatDate(when5)).as("not Valid  reminder date").isEqualTo(false);

        assertThat(caseSetReminder.isValidFormatDate(when6)).as("not Valid  reminder date").isEqualTo(false);

        assertThat(caseSetReminder.isValidFormatDate(when7)).as("not Valid  reminder date").isEqualTo(false);

        assertThat(caseSetReminder.isValidFormatDate(when8)).as("not Valid  reminder date").isEqualTo(false);

    }
}
