package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.db.Dao;
import gr.cytech.chatreminderbot.rest.message.Message;
import gr.cytech.chatreminderbot.rest.message.Request;
import gr.cytech.chatreminderbot.rest.message.Sender;
import gr.cytech.chatreminderbot.rest.message.ThreadM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CaseSetTimezoneTest {
    private static final Logger logger = LoggerFactory.getLogger(CaseSetTimezoneTest.class);

    private CaseSetTimezone caseSetTimezone;
    private Control control;
    private Dao dao;

    @BeforeEach
    public void beforeEach() throws Exception {

        control = new Control();
        dao = mock(Dao.class);
        control.dao = mock(Dao.class);
        caseSetTimezone = new CaseSetTimezone();
        caseSetTimezone.setKeyWordGlobal("global");
        caseSetTimezone.setKeyWordMy("my");
        caseSetTimezone.dao = dao;

        when(dao.getConfigurationValue("BOT_NAME")).thenReturn("TimezoneTest");
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
        when(dao.merge(any(TimeZone.class))).thenAnswer(inv -> inv.getArguments()[0]);

        caseSetTimezone.setRequest(req);
        control.setRequest(req);
        caseSetTimezone.setSplitMsg(control.getSplitMsg());
        caseSetTimezone.setTimezone();

        ArgumentCaptor<TimeZone> argumentCaptor = ArgumentCaptor.forClass(TimeZone.class);
        verify(dao, times(1)).merge(argumentCaptor.capture());
        List<TimeZone> captureTimezone = argumentCaptor.getAllValues();

        assertThat(captureTimezone.get(0).getTimezone()).isEqualTo("Europe/Athens");
    }
}
