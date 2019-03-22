package gr.cytech.chatreminderbot.rest.controlCases;

import javax.persistence.*;

@Entity
@Table(name = "buttonurl")
@NamedQueries({
        @NamedQuery(name = "get.default",
                query = "SELECT t from ButtonUrl t where t.baseUrl = 'default'")
})
public class ButtonUrl {

    @Column(name = "url")
    private String url;

    @Id
    @Column(name = "base_url")
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public ButtonUrl(String url) {
        this.url = url;
        this.baseUrl = "default";
    }

    public String getUrl() {
        return url;
    }

    public ButtonUrl() {
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "" + url;
    }
}
