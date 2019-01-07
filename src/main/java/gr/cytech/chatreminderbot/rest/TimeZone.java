package gr.cytech.chatreminderbot.rest;


import javax.persistence.*;

@Entity
@Table(name = "time_zone")
@NamedQueries({
        @NamedQuery(name = "get.Alltimezone",
                query = "SELECT t from TimeZone t"),
        @NamedQuery(name = "set.timezone",
                query = "UPDATE TimeZone t set t.timezone = :timezone WHERE t.userid LIKE :userid ")
})
public class TimeZone {


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

    public void setUserid(String user) {
        this.userid = user;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
