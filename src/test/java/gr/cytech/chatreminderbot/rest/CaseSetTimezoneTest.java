package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.controlCases.*;
import gr.cytech.chatreminderbot.rest.message.Message;
import gr.cytech.chatreminderbot.rest.message.Request;
import gr.cytech.chatreminderbot.rest.message.Sender;
import gr.cytech.chatreminderbot.rest.message.ThreadM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CaseSetTimezoneTest {

    private static final Logger logger = LoggerFactory.getLogger(CaseSetTimezoneTest.class);

    private CaseSetTimezone caseSetTimezone;
    private Control control;

    @BeforeEach
    public void beforeEach() throws Exception {
        String threadId = "THREAD_ID";
        String spaceId = "SPACE_ID";
        caseSetTimezone = new CaseSetTimezone();
        Reminder reminder = new Reminder("Do Something", ZonedDateTime.now(ZoneId.of("Europe/Athens")).plusMinutes(10),
                "DisplayName", spaceId, threadId);

        reminder.setReminderId(1);
        caseSetTimezone.entityManager = mock(EntityManager.class);

        control = new Control();
        control.dao = mock(Dao.class);
        control.dao.entityManager = mock(EntityManager.class);
        control.entityManager = mock(EntityManager.class);
        caseSetTimezone.setKeyWordGlobal("global");
        caseSetTimezone.setKeyWordMy("my");

        TypedQuery query = mock(TypedQuery.class);

        when(caseSetTimezone.entityManager.createNamedQuery("get.Alltimezone", TimeZone.class)).thenReturn(query);
        when(control.dao.entityManager.createNamedQuery("get.configurationByKey", Configurations.class))
                .thenReturn(query);
        when(query.setParameter("configKey", "BOT_NAME")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Configurations("BOT_NAME","TimezoneTest"));
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
    public void saveTimeZoneTest() {
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

        ArgumentCaptor<TimeZone> argumentCaptor = ArgumentCaptor.forClass(TimeZone.class);
        verify(caseSetTimezone.entityManager, times(1)).persist(argumentCaptor.capture());
        List<TimeZone> captureTimezone = argumentCaptor.getAllValues();

        assertThat(captureTimezone.get(0).getTimezone()).isEqualTo("Europe/Athens");
    }
}
