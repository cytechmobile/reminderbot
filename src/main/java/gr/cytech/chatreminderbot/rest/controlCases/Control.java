package gr.cytech.chatreminderbot.rest.controlCases;

import gr.cytech.chatreminderbot.rest.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;

@Stateless
public class Control {
    private final static Logger logger = LoggerFactory.getLogger(Control.class.getName());
    private static final String BOT_NAME_ENV = "BOT_NAME";
    private static final String BOT_NAME = System.getProperty(BOT_NAME_ENV,
            System.getenv().
                    getOrDefault(BOT_NAME_ENV, "reminder"));

    @Inject
    CaseDeleteReminder caseDeleteReminder;

    @Inject
    CaseShowReminders caseShowReminders;

    @Inject
    CaseSetTimezone caseSetTimezone;

    @Inject
    CaseSetReminder caseSetReminder;


    private Request request;
    private ArrayList<String> splitMsg;

    //--- Key Words
    private String keyWord_help = "help";

    private String keyWord_remind = "remind";

    //Current format;
    //set my timezone to athens
    private String[] keyWords_setTimezone = {"set", "timezone", "to"};
    private String keyWord_setTimezone_global = "global";
    private String keyWord_setTimezone_my = "my";


    private String keyWord_ShowReminders = "list";

    private String keyWord_deleteReminder = "delete";
    //-- End of keyWords

    //-- Responses
    private String responseCaseDefault = "I didnt understand you, type help for instructions \n";
    private String responseCaseEmptyRequest = "I didnt understand you, you send empty message";

    public Control() {
    }

    public Request getRequest() {
        return request;
    }

    public ArrayList<String> getSplitMsg() {
        return splitMsg;
    }

    public void setRequest(Request request) {

        this.request = request;
        splitMsg = new ArrayList<>(Arrays.asList(request.getMessage().getText().split("\\s+")));
        if (!splitMsg.isEmpty() && splitMsg.get(0).equals("@" + BOT_NAME)) {
            splitMsg.remove(0);
        }
    }

    /*
     * Chose case to response accordingly key words in request
     * @returns a string with the response
     * */
    public String controlResponse() {

        if (splitMsg.isEmpty()) {
            return responseCaseEmptyRequest;
        } else
            //----- Case Response with instructions - Help
            if (splitMsg.size() == 1 && splitMsg.get(0).equals(keyWord_help)) {
                logger.info("----Case Help----");
                return caseHelp();
            } else
                //-------Case create Reminder -----------------
                if (splitMsg.get(0).equals(keyWord_remind)) {
                    logger.info("---Case remind---");

                    return caseSetReminder();
                } else
                    //------------Case Set  timezones -------------------
                    if (splitMsg.size() == 5 && (splitMsg.get(0).equals(keyWords_setTimezone[0])

                            && splitMsg.get(2).equals(keyWords_setTimezone[1])
                            && splitMsg.get(3).equals(keyWords_setTimezone[2]))) {
                        logger.info("----Case set timezone----");

                        return caseSetTimezone();
                    } else
                        ///------------Case Show my reminders -------------------
                        if (splitMsg.size() == 1 && splitMsg.get(0).equals(keyWord_ShowReminders)) {
                            logger.info("----Case list reminders----");
                            return caseShowReminders();
                        } else
                            ///------------Case Delete reminder -------------------
                            if (splitMsg.size() == 2 && splitMsg.get(0).equals(keyWord_deleteReminder)) {
                                logger.info("----Case delete reminder----");
                                return caseDeleteReminder();
                            } else
                            ///------------Case Default -------------------
                            {
                                logger.info("----Case default ---- ");
                                return responseCaseDefault;
                            }

    }


    //--  Cases ----

    public String caseHelp() {
        return "\n ----- Instructions using the reminder bot -----  \n \n" +
                "1)  Set a reminder  \n \n" +
                "    a) For you   \n" +
                "     `@" + BOT_NAME + " remind me 'what' at 16/03/2019 16:33`  \n" +
                "    b) For anyone in the current room   \n" +
                "     `@" + BOT_NAME + " remind @George Papakis 'what' at 16/03/2019 16:33`  \n" +
                "    c) All in any the current room  \n" +
                "     `@" + BOT_NAME + " @all 'what' at 16/03/2019 16:33`  \n" +
                "    d) All in any other room that bot is invited    \n" +
                "     `@" + BOT_NAME + " remind #roomName 'what' at 16/03/2019 16:33` \n \n" +
                "2) Set timezone  \n \n" +
                "    a) For each reminder   \n" +
                "     `@" + BOT_NAME + " remind me 'what' at 16/03/2019 16:33 Athens `  \n" +
                "    b) If previews omitted set timezone for each user in every reminder he sets  \n" +
                "     `@" + BOT_NAME + " set my timezone to athens`  \n" +
                "    c) If previews omitted set timezone for every user in the current domain  \n" +
                "     `@" + BOT_NAME + " set global timezone to Paris`  \n" +
                "    d) By default it uses GMT \n \n" +
                "3) Show my reminders  \n \n" +
                "    a) For each user shows reminders that will notify him.  \n" +
                "     `@" + BOT_NAME + " list` \n" +
                "      Example:\n" +
                "     `1) ID:23 what:' Something to do ' When: 23/01/2019 18:20 Europe/Athens` \n \n  " +
                "4) Delete a reminder  \n \n" +
                "    a) For each user, using a reminders id.  \n" +
                "     `@" + BOT_NAME + " delete 323 ` \n";
    }

    public String caseSetReminder() {
        caseSetReminder.setRequest(request);
        caseSetReminder.setBOT_NAME(BOT_NAME);
        return caseSetReminder.setReminder();
    }

    public String caseSetTimezone() {

        caseSetTimezone.setRequest(request);
        caseSetTimezone.setSplitMsg(splitMsg);
        caseSetTimezone.setKeyWord_global(keyWord_setTimezone_global);
        caseSetTimezone.setKeyWord_my(keyWord_setTimezone_my);

        return caseSetTimezone.setTimezone();
    }

    public String caseShowReminders() {
        caseShowReminders.setRequest(request);
        return caseShowReminders.showReminders();
    }

    public String caseDeleteReminder() {
        caseDeleteReminder.setRequest(request);
        caseDeleteReminder.setSplitMsg(splitMsg);
        return caseDeleteReminder.deleteReminder();
    }

    //-- End of Cases


}
