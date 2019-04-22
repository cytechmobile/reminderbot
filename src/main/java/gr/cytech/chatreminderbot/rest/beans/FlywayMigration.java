package gr.cytech.chatreminderbot.rest.beans;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class FlywayMigration {
    private static final Logger logger = LoggerFactory.getLogger(FlywayMigration.class);
    private boolean migrated = false;

    @ConfigProperty(name = "quarkus.datasource.url")
    String dbUrl;
    @ConfigProperty(name = "quarkus.datasource.username")
    String dbUser;
    @ConfigProperty(name = "quarkus.datasource.password")
    String dbPassword;

    public void migrate(@Observes StartupEvent se) {
        logger.info("logger with url:{}  user:{} pwd:{}", dbUrl, dbUser, dbPassword);
        Flyway flyway = Flyway.configure()
                .dataSource(dbUrl, dbUser, dbPassword)
                .load();

        flyway.migrate();
        migrated = true;
    }

    public boolean migrationCompleted() {
        return migrated;
    }
}
