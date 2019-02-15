package gr.cytech.chatreminderbot.rest.controlCases;


import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "reminder")
@NamedQueries({
        @NamedQuery(name = "reminder.findNextReminder",
                query = "SELECT r FROM Reminder r order by r.when"),
        @NamedQuery(name = "reminder.findAll",
                query = "SELECT r from Reminder r"),
        @NamedQuery(name = "reminder.showReminders",
                query = "SELECT r from Reminder r where r.senderDisplayName like :userid order by r.when"),
        @NamedQuery(name = "reminder.findByUserAndReminderId",
                query = "SELECT r from Reminder r where r.senderDisplayName  like :userId AND r.reminderId = :reminderId")
})
public class Reminder {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reminder_id", nullable = false)
    private int reminderId;

    @Column(name = "what", nullable = false)
    private String what;

    @Column(name = "whenTo", nullable = false)
    private ZonedDateTime when;

    @Column(name = "sender_displayName", nullable = false)
    private String senderDisplayName;

    @Column(name = "space_id", nullable = false)
    private String spaceId;

    @Column(name = "thread_id", nullable = false)
    private String threadId;

    @Column(name = "reminder_timezone", nullable = false)
    private String reminderTimezone;


    public Reminder() {
    }

    public Reminder(String what, ZonedDateTime when, String senderDisplayName, String spaceId, String threadId) {
        this.what = what;
        this.when = when;
        this.senderDisplayName = senderDisplayName;
        this.spaceId = spaceId;
        this.threadId = threadId;
    }


    public Reminder(String what, ZonedDateTime when, String senderDisplayName, String reminderTimezone, String spaceId, String threadId) {
        this.what = what;
        this.when = when;
        this.senderDisplayName = senderDisplayName;
        this.reminderTimezone = reminderTimezone;
        this.spaceId = spaceId;
        this.threadId = threadId;

    }

    public int getReminderId() {
        return reminderId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public String getWhat() {
        return what;
    }

    public ZonedDateTime getWhen() {
        return when;
    }

    public String getReminderTimezone() {
        return reminderTimezone;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reminder reminder = (Reminder) o;
        return reminderId == reminder.reminderId
                && Objects.equals(what, reminder.what)
                && Objects.equals(when, reminder.when)
                && Objects.equals(senderDisplayName, reminder.senderDisplayName)
                && Objects.equals(spaceId, reminder.spaceId)
                && Objects.equals(threadId, reminder.threadId)
                && Objects.equals(reminderTimezone, reminder.reminderTimezone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reminderId, what, when, senderDisplayName, spaceId, threadId, reminderTimezone);
    }
}
