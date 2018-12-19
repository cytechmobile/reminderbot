package gr.cytech.chatreminderbot.rest;

import org.wildfly.swarm.runner.Runner;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractIT {
    private static final AtomicBoolean INIT = new AtomicBoolean(false);


    public static void abstractBeforeAll() throws Exception {
        if (INIT.compareAndSet(false, true)) {
            Runner.main(new String[0]);
        }
    }


}
