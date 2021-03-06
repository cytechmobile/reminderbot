package gr.cytech.chatreminderbot.rest.controlCases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "time_zone")
@NamedQueries({
        @NamedQuery(name = "get.Alltimezone",
                query = "SELECT t from TimeZone t"),
        @NamedQuery(name = "get.spesificTimezone",
                query = "SELECT t from TimeZone t where t.userid = :userid"),
        @NamedQuery(name = "show.timezones",
                query = "SELECT t from TimeZone t where t.userid = :id ")
})
public class TimeZone {
    private static final Logger logger = LoggerFactory.getLogger(TimeZone.class);

    @Column(name = "timezone")
    private String timezone;

    @Id
    @Column(name = "userid")
    private String userid;

    public TimeZone() {
    }

    public TimeZone(String timezone, String userid) {
        this.timezone = timezone;
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public String getTimezone() {
        return timezone;
    }

    public String findTimeZones(String inputTimeZone) {
        List<String> worldTimeZones = new ArrayList<>(ZoneId.getAvailableZoneIds());
        for (String timeZone : worldTimeZones) {
//            z is like Europe/Athens so,
//            we either get it at full form or as the city name.
            String[] slitted = timeZone.split("/");
            if (timeZone.equalsIgnoreCase(inputTimeZone)
                    || slitted[slitted.length - 1].equalsIgnoreCase(inputTimeZone)) {
                return timeZone;
            }
        }
        logger.info("Wrong timezone");
        return null;
    }

    @Override
    public String toString() {
        return "'" + timezone + '\'';
    }
}
