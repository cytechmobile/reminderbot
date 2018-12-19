package gr.cytech.chatreminderbot.rest;


import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminder")
@NamedQueries({
        @NamedQuery(name = "reminder.findNextReminder",
                query = "SELECT r FROM Reminder r order by r.when"),
        @NamedQuery(name = "reminder.findAll",
                query = "SELECT r from Reminder r")

})
public class Reminder implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reminder_id", nullable = false)
    private int reminderId;

    @Column(name = "what", nullable = false)
    private String what;

    @Column(name = "whenTo", nullable = false)
    private LocalDateTime when;


    @Column(name = "sender_displayName", nullable = false)
    private String senderDisplayName;

    @Column(name = "space_id", nullable = false)
    private String spaceId;

    @Column(name = "thread_id", nullable = false)
    private String threadId;


    public Reminder() {
    }

    public Reminder(String what, LocalDateTime when, String senderDisplayName, String spaceId, String threadId) {
        this.what = what;
        this.when = when;
        this.senderDisplayName = senderDisplayName;
        this.spaceId = spaceId;
        this.threadId = threadId;
    }


    public int getReminderId() {
        return reminderId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String space_id) {
        this.spaceId = space_id;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String thread_id) {
        this.threadId = thread_id;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public void setSenderDisplayName(String sender_id) {
        this.senderDisplayName = sender_id;
    }


    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public LocalDateTime getWhen() {
        return when;
    }

    public void setWhen(LocalDateTime when) {
        this.when = when;
    }
}
