package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.controlCases.CaseSetConfigurations;
import gr.cytech.chatreminderbot.rest.controlCases.Configurations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CaseSetConfigurationsTest {

    public CaseSetConfigurations caseSetConfigurations;
    private List<String> splitMsg;

    @BeforeEach
    public void beforeEach() throws Exception {
        caseSetConfigurations = new CaseSetConfigurations();
        caseSetConfigurations.entityManager = mock(EntityManager.class);
        TypedQuery query = mock(TypedQuery.class);
        when(caseSetConfigurations.entityManager
                .createNamedQuery("get.allConfigurations", Configurations.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(new Configurations("buttonUrl", "localhost")));
        when(query.getSingleResult()).thenReturn(new Configurations("buttonUrl", "localhost"));
    }

    @Test
    public void setConfigurationsTest() throws Exception {
        String message = "config set buttonUrl localhost";

        String expectedResponse = "Updated configuration to localhost with key buttonUrl";
        splitMsg = new ArrayList<>(Arrays.asList(message.split("\\s+")));

        assertThat(caseSetConfigurations.configurationController(splitMsg)).isEqualTo(expectedResponse);
        ArgumentCaptor<Configurations> argumentCaptor = ArgumentCaptor.forClass(Configurations.class);
        verify(caseSetConfigurations.entityManager, times(1)).merge(argumentCaptor.capture());
        List<Configurations> captureConfigurations = argumentCaptor.getAllValues();

        assertThat(captureConfigurations.get(0).getValue()).isEqualTo("localhost");
        assertThat(captureConfigurations.get(0).getKey()).isEqualTo("buttonUrl");

    }

    @Test
    public void listOfConfigurations() throws Exception {

        String message = "config";

        // adding multiply whitespaces instead of just pressing space in the string
        String multiplyWhiteSpaces = String.format("%-24s", " ");
        String expectedResponse = "the configurations right now are: \n";
        Configurations resultList = caseSetConfigurations.entityManager
                .createNamedQuery("get.allConfigurations", Configurations.class).getSingleResult();

        splitMsg = new ArrayList<>(Arrays.asList(message.split("\\s+")));

        assertThat(caseSetConfigurations.configurationController(splitMsg))
                .isEqualTo(expectedResponse + " key" + multiplyWhiteSpaces
                        + "value \n" + "<b>" + resultList.getKey()
                        + "</b> " + " --> " + resultList.getValue() + " \n");
    }
}
