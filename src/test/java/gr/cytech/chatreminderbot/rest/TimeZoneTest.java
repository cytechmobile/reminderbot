package gr.cytech.chatreminderbot.rest;

import gr.cytech.chatreminderbot.rest.controlCases.TimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeZoneTest {
    private TimeZone timeZone;

    @BeforeEach
    final void beforeEach() throws Exception {
        timeZone = new TimeZone();
    }

    @Test
    void findTimeZonesTest() {
        String timezone1 = "athens";
        String timezone2 = "thens";
        String timezone3 = "PARIS";
        String timezone4 = "RIS";

        assertThat(timeZone.findTimeZones(timezone1)).isEqualTo("Europe/Athens");
        assertThat(timeZone.findTimeZones(timezone2)).isEqualTo(null);
        assertThat(timeZone.findTimeZones(timezone3)).isEqualTo("Europe/Paris");
        assertThat(timeZone.findTimeZones(timezone4)).isEqualTo(null);
    }
}
