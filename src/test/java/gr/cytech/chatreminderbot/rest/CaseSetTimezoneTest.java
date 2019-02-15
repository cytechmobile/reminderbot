package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.controlCases.CaseSetTimezone;
import gr.cytech.chatreminderbot.rest.controlCases.Control;
import gr.cytech.chatreminderbot.rest.controlCases.Reminder;
import gr.cytech.chatreminderbot.rest.controlCases.TimeZone;
import gr.cytech.chatreminderbot.rest.message.Message;
import gr.cytech.chatreminderbot.rest.message.Request;
import gr.cytech.chatreminderbot.rest.message.Sender;
import gr.cytech.chatreminderbot.rest.message.ThreadM;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
public class CaseSetTimezoneTest {

    private Reminder reminder;
    private CaseSetTimezone caseSetTimezone;
    Control control;

    @Mocked
    private EntityManager entityManager;


    @BeforeEach
    final void beforeEach() throws Exception {
        String threadId = "THREAD_ID";
        String spaceId = "SPACE_ID";
        caseSetTimezone = new CaseSetTimezone();
        reminder = new Reminder("Do Something", ZonedDateTime.now(ZoneId.of("Europe/Athens")).plusMinutes(10),
                "DisplayName", "Europe/Athens", spaceId, threadId);

        reminder.setReminderId(1);

        caseSetTimezone.entityManager = entityManager;

         control = new Control();
        caseSetTimezone.setKeyWordGlobal("global");
        caseSetTimezone.setKeyWordMy("my");
    }

    @Test
    void extractTimeZoneTest() {
        String givenTimeZone1 = "GMTSASS";

        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);
        mes.setText("set global timezone to" + givenTimeZone1);

        req.setMessage(mes);
        caseSetTimezone.setRequest(req);
        assertThat(caseSetTimezone.extractTimeZone()).isEqualTo(null);

    }


    @Test
    void saveTimeZoneTest() {
        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);
        mes.setText("set my timezone to Athens");

        req.setMessage(mes);


        caseSetTimezone.setRequest(req);
        control.setRequest(req);
        caseSetTimezone.setSplitMsg(control.getSplitMsg());
        caseSetTimezone.setTimezone();


        List<TimeZone> captureTimezone = new ArrayList<>();

        new Verifications() {{
            entityManager.persist(withCapture(captureTimezone));
            times = 1;
        }};

        assertThat(captureTimezone.get(0).getTimezone()).isEqualTo("Europe/Athens");

    }

}
