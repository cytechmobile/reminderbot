package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.db.Dao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CaseSetConfigurationsTest {

    CaseSetConfigurations caseSetConfigurations;
    Dao dao;

    @BeforeEach
    public void beforeEach() throws Exception {
        dao = mock(Dao.class);
        caseSetConfigurations = new CaseSetConfigurations();
        caseSetConfigurations.dao = dao;
    }

    @Test
    public void setConfigurationsTest() throws Exception {
        String message = "config set buttonUrl localhost";

        String expectedResponse = "Updated configuration to localhost with key buttonUrl";
        List<String> splitMsg = List.of(message.split("\\s+"));

        when(dao.merge(any(Configurations.class))).thenAnswer(inv -> inv.getArguments()[0]);

        assertThat(caseSetConfigurations.configurationController(splitMsg)).isEqualTo(expectedResponse);
        ArgumentCaptor<Configurations> argumentCaptor = ArgumentCaptor.forClass(Configurations.class);
        //verify that merge has executed exactly 1 time
        verify(dao, times(1)).merge(argumentCaptor.capture());
        List<Configurations> captureConfigurations = argumentCaptor.getAllValues();

        assertThat(captureConfigurations.get(0).getValue()).isEqualTo("localhost");
        assertThat(captureConfigurations.get(0).getKey()).isEqualTo("buttonUrl");
    }

    @Test
    public void listOfConfigurations() throws Exception {
        String message = "config";

        // adding multiply whitespaces instead of just pressing space in the string
        String multiplyWhiteSpaces = String.format("%-24s", " ");

        //mocked query to get key/value
        List<Configurations> resultList = List.of((new Configurations("test", "tost")));
        when(dao.getAllConfigurations()).thenReturn(resultList);

        List<String> splitMsg = new ArrayList<>(Arrays.asList(message.split("\\s+")));

        String expectedResponse = "the configurations right now are: \n" + " key" + multiplyWhiteSpaces
                + "value \n" + "<b>" + resultList.get(0).getKey()
                + "</b> " + " --> " + resultList.get(0).getValue() + " \n";

        assertThat(caseSetConfigurations.configurationController(splitMsg))
                .isEqualTo(expectedResponse);
    }
}
