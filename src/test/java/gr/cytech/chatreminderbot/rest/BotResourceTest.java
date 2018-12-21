package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.message.Message;
import gr.cytech.chatreminderbot.rest.message.Request;
import gr.cytech.chatreminderbot.rest.message.Sender;
import gr.cytech.chatreminderbot.rest.message.ThreadM;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class BotResourceTest {

    @Mocked
    EntityManager entityManager;

    @Mocked
    TimerSessionBean timerSessionBean;

    BotResource botResource;

    @Mocked
    Client client;

    Reminder reminder;

    @BeforeEach
    public final void beforeEach() throws Exception {
        botResource = new BotResource();

        timerSessionBean = new TimerSessionBean();

        reminder = new Reminder("'what'", LocalDateTime.now().plusMinutes(10),
                "DisplayName", "uPWJ7AAAAAE", "1E_d3mjJGyM");


        timerSessionBean.nextReminderDate = reminder.getWhen();

        botResource.timerSessionBean = timerSessionBean;
        botResource.entityManager = entityManager;
    }

    @Test
    public void parseReminderDate() throws Exception {
        LocalDateTime curr = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String inOneHour = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(curr);
        LocalDateTime result = botResource.dateForm(inOneHour);

        assertThat(result).as("parsed date is not the expected").isEqualTo(curr);
    }


    @Test
    public void setNextReminderTest() throws Exception {
        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);
        final String expectedDate = "12/12/2019 12:00";
        mes.setText("reminder me 'something to do' at " + expectedDate);
        req.setMessage(mes);


        // Already set in mock a nextReminder that is to be in 10 mins from now()
        //So this should not be set
        botResource.handleReq(req);

        //Verifies that setNextReminder is called 0 times because Input reminderDate is AFTER the current
        new Verifications() {{
            timerSessionBean.setNextReminder((Reminder) any, (LocalDateTime) any);
            times = 0;
        }};
    }


    @Test
    public void persistReminder() throws Exception {
        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);
        final String expectedDate = "12/12/2019 12:00";
        mes.setText("reminder me 'something to do' at " + expectedDate);
        req.setMessage(mes);

        botResource.handleReq(req);


        List<Reminder> capturedReminders = new ArrayList<>();

        new Verifications() {{
            entityManager.persist(withCapture(capturedReminders));
            times = 1;
        }};

        assertThat(capturedReminders).as("no reminders persisted").hasSize(1);
        Reminder r = capturedReminders.get(0);

        // make sure persisted reminder matches our expectations
        Locale locale = new Locale("en", "gr");
        String format = "dd/MM/yyyy HH:mm";
        DateTimeFormatter fomatter = DateTimeFormatter.ofPattern(format, locale);

        assertThat(r.getWhen()).isEqualTo(LocalDateTime.parse("12/12/2019 12:00", fomatter));
    }

    @Test
    public void extractWhenFromRequest() throws Exception {
        Request req = new Request();
        Message mes = new Message();


        Sender sender = new Sender();

        sender.setName("MyName");
        mes.setSender(sender);
        final String expectedDate = "12/12/2018 12:00";
        mes.setText("reminder me 'something to do' at " + expectedDate);
        req.setMessage(mes);

        assertThat(botResource.extractReminderDate(req)).as("Unexpected extracted reminder date").isEqualTo(expectedDate);


    }


    @Test
    public void extractWhatFromRequest() throws Exception {
        Request req = new Request();
        Message mes = new Message();


        Sender sender = new Sender();

        sender.setName("MyName");
        mes.setSender(sender);
        String what = "something to do";
        final String expectedDate = "12/12/2018 12:00";
        mes.setText("reminder me '" + what + "' at " + expectedDate);
        req.setMessage(mes);

        assertThat(botResource.extractWhat(req)).as("Unexpected extracted reminder date").isEqualTo(what);

    }

    @Test
    public void extractWhoFromRequest() throws Exception {
        Request req = new Request();
        Message mes = new Message();
        ThreadM thread = new ThreadM();
        //SpaceId from testBot room
        final String spaceId = "AAAADvB8eGY";
        thread.setName("spaces/" + spaceId + "/thread/wk-fzcPcktM");


        Sender sender = new Sender();

        sender.setName("MyName");
        mes.setSender(sender);
        mes.setThread(thread);
        String what = "something to do";
        String who = "@Ntina trol";
        final String expectedDate = "12/12/2018 12:00";
        mes.setText("reminder " + who + " '" + what + "' at " + expectedDate);
        req.setMessage(mes);

        Map<String, String> expectedUsers = new HashMap<>();
        new Expectations() {{
            client.getListOfMembersInRoom(spaceId); result = expectedUsers;
        }};

        assertThat(botResource.extractWho(req)).as("Unexpected extracted reminder date").isEqualTo(who.substring(1));
    }

    @Test
    public void findIdUserNameTest() {


      //In the future
        }

    @Test
    public void isValidDateTest() {
        String when = "16/12/2018 21:40";

        //Check dates
        String when1 = "56/12/2018 21:40";
        //Check month
        String when2 = "16/32/2018 21:40";
        //Check year
        String when3 = "16/12/2017 21:40";
        //Check hour
        String when4 = "16/12/2018 45:40";
        //Check mins
        String when5 = "16/12/2018 21:66";
        //Check format
        String when6 = "1612/2018 21:66";
        String when7 = "16/12/2018 2166";
        String when8 = "16.12.2018 21.66";

/*
        assertEquals(true, botResource.isValidDate(when));

        assertEquals(false, botResource.isValidDate(when1));
        assertEquals(false, botResource.isValidDate(when2));
        assertEquals(false, botResource.isValidDate(when3));
        assertEquals(false, botResource.isValidDate(when4));
        assertEquals(false, botResource.isValidDate(when5));

        assertEquals(false, botResource.isValidDate(when6));
        assertEquals(false, botResource.isValidDate(when7));
        assertEquals(false, botResource.isValidDate(when8));
*/
    }


}
