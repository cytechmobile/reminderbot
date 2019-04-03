package gr.cytech.chatreminderbot.rest.controlCases;

import javax.persistence.*;

@Entity
@Table(name = "configurations")
@NamedQueries({
        @NamedQuery(name = "get.configurationByKey",
                query = "SELECT t from Configurations t where t.key = :configKey"),
        @NamedQuery(name = "get.allConfigurations",
                query = "SELECT t from Configurations t")
})
public class Configurations {
    @Id
    @Column(name = "key", unique = true, nullable = false)
    private String key;

    @Column(name = "value")
    private String value;

    public Configurations(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Configurations() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
