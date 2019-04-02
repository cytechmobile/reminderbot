package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.controlCases.CaseSetConfigurations;
import gr.cytech.chatreminderbot.rest.controlCases.Configurations;
import gr.cytech.chatreminderbot.rest.message.Message;
import gr.cytech.chatreminderbot.rest.message.Request;
import gr.cytech.chatreminderbot.rest.message.Sender;
import gr.cytech.chatreminderbot.rest.message.ThreadM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CaseSetConfigurationsTest {

    public CaseSetConfigurations caseSetConfigurations;
    private ArrayList<String> splitMsg;

    @BeforeEach
    public void beforeEach() throws Exception {
        caseSetConfigurations = new CaseSetConfigurations();
        caseSetConfigurations.entityManager = mock(EntityManager.class);
        TypedQuery query = mock(TypedQuery.class);
        when(caseSetConfigurations.entityManager.createNamedQuery("update.configuration")).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(caseSetConfigurations.entityManager
                .createNamedQuery("get.configurationByKey", Configurations.class)).thenReturn(query);
        when(caseSetConfigurations.entityManager
                .createNamedQuery("get.allConfigurations", Configurations.class)).thenReturn(query);
    }

    @Test
    public void changeButtonUrlTest() throws Exception {
        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);
        mes.setText("config buttonUrl localhost");

        req.setMessage(mes);
        String expectedResponse = "Updated url to localhost";
        splitMsg = new ArrayList<>(Arrays.asList(req.getMessage().getText().split("\\s+")));
        caseSetConfigurations.setSplitMsg(splitMsg);

        assertThat(caseSetConfigurations.configurationController()).isEqualTo(expectedResponse);

    }

    @Test
    public void listOfConfigurations() throws Exception {
        Request req = new Request();
        Message mes = new Message();
        Sender sender = new Sender();
        ThreadM threadM = new ThreadM();

        threadM.setName("space/SPACE_ID/thread/THREAD_ID");
        sender.setName("MyName");

        mes.setThread(threadM);
        mes.setSender(sender);
        mes.setText("config");

        req.setMessage(mes);
        // adding multiply whitespaces instead of just pressing space in the string
        String multiplyWhiteSpaces = String.format("%-24s", " ");
        String expectedResponse = "the configurations right now are: \n key" + multiplyWhiteSpaces + "value \n";
        splitMsg = new ArrayList<>(Arrays.asList(req.getMessage().getText().split("\\s+")));
        caseSetConfigurations.setSplitMsg(splitMsg);

        assertThat(caseSetConfigurations.configurationController()).isEqualTo(expectedResponse);

    }
}
