package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Message;
import gr.cytech.chatreminderbot.rest.message.Request;
import gr.cytech.chatreminderbot.rest.message.Sender;
import gr.cytech.chatreminderbot.rest.message.ThreadM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.nlp.parse.DateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CaseSetReminderTest {//todo: provide one more better test to test timezone
    Logger logger = LoggerFactory.getLogger(CaseSetReminderTest.class);
    public CaseSetReminder caseSetReminder;

    private Request request;
    private Message message;
    private Client client;
    private Reminder reminder;

    @BeforeEach
    final void beforeEach() {
        request = new Request();
        message = new Message();

        String spaceId = "SPACE_ID";
        String threadId = "THREAD_ID";

        TimerSessionBean timerSessionBean = mock(TimerSessionBean.class);
        client = mock(Client.class);
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

        reminder = new Reminder("Do Something", ZonedDateTime.now(ZoneId.of("Europe/Athens"))
                .plusMinutes(10), "DisplayName", spaceId, threadId);

        reminder.setReminderId(1);
        when(timerSessionBean.getNextReminderDate()).thenReturn(reminder.getWhen());

        TypedQuery query = mock(TypedQuery.class);
        TypedQuery query1 = mock(TypedQuery.class);

        when(caseSetReminder.entityManager.createNamedQuery("get.configurationByKey",
                Configurations.class)).thenReturn(query);
        when(query.setParameter("configKey","BOT_NAME")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Configurations("BOT_NAME", "botName"));

        when(caseSetReminder.entityManager.createNamedQuery("get.spesificTimezone",
                TimeZone.class)).thenReturn(query1);
        when(query1.setParameter("userid","MyName")).thenReturn(query1);
        when(query1.getSingleResult()).thenReturn(new TimeZone("Europe/Athens", "MyName"));

    }

    @Test
    void setNextReminderTest() throws Exception {
        String expectedDate = "12/12/2019 12:00 athens";
        message.setText("remind me ' set next reminder Test' at " + expectedDate);
        request.setMessage(message);
        // Already set in mock a nextReminder that is to be in 10 minutes from now()
        //So this should not be set
        caseSetReminder.buildReminder(request);
        //Verifies that setNextReminder is called 0 times because Input reminderDate is AFTER the current
        verify(caseSetReminder.timerSessionBean, times(0))
                .setNextReminder(any(Reminder.class), any(ZonedDateTime.class));

    }
    @Test
    void dateFormTest() throws Exception {
        ZonedDateTime curr = ZonedDateTime.now(ZoneId.of("Europe/Athens")).truncatedTo(ChronoUnit.MINUTES);
        String inOneHour = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(curr);
        ZonedDateTime result = caseSetReminder.dateForm(inOneHour, String.valueOf(ZoneId.of("Europe/Athens")));

        assertThat(result).as("parsed date is not the expected").isEqualTo(curr);
    }



    @Test
    void persistReminder() throws Exception {

        String expectedDate = "12/12/2019 12:00";
        message.setText("remind me 'persist Reminder Test' at " + expectedDate);
        request.setMessage(message);
        caseSetReminder.buildReminder(request);

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
    void setInfosTest() throws Exception {
        String what = " something to do";
        String who = "@Ntina trol";
        final String expectedDate = "12/12/2020 12:00";

        Map<String,String> hashMap = new HashMap<>();
        hashMap.put("Ntina trol" , "Ntina trol");
        when(caseSetReminder.client.getListOfMembersInRoom("SPACE_ID")).thenReturn(hashMap);

        message.setText("remind " + who + " " + what + " at " + expectedDate + " athens");
        request.setMessage(message);
        caseSetReminder.buildReminder(request);

        List<String> splitMsg = new ArrayList<>(List.of(request.getMessage().getText().split("\\s+")));

        caseSetReminder.client = client;
        Reminder reminder = caseSetReminder.setInfosForRemind(request, this.reminder, splitMsg);

        String reminderWhenFormated = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(reminder.getWhen());

        assertThat(reminder.getSenderDisplayName()).as("Unexpected extracted reminder date")
                .isEqualTo(who.substring(1));
        assertThat(reminder.getWhat()).as("Unexpected extracted reminder date").isEqualTo(what);
        assertThat(reminderWhenFormated).as("Unexpected extracted reminder date").isEqualTo(expectedDate);
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
    @Test
    public void testCenturiesAgo() throws Exception {
        String text = "remind me to eat fish every 3 days at 10:00";
        List<DateGroup> parse = new PrettyTimeParser().parseSyntax(text);
        DateGroup dg = parse.get(0);
        int pos = dg.getPosition();
        String upTo = text.substring(0, pos).trim();
        if (upTo.endsWith(" every")) {
            upTo = upTo.substring(0, upTo.length() - " every".length());
        }
//        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String s = format.format(dg.getDates().get(0));

        logger.info("FORMAT {}", s);//todo fix that
        reminder.setWhen(caseSetReminder.dateForm(s, "Europe/Athens"));
        logger.info("reminder {}",reminder.getWhen());
        logger.info("reminder what {} ",reminder.getWhat());

        logger.info("parse position is: {} up to there:{} text:{}", pos, upTo, dg.getText());

    }

}
