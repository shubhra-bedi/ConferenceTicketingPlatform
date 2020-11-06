import conference.Conference;
import conference.ConferenceController;
import util.PermissionException;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConferenceSystem {
    Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public void run() {
        // Setup logger
        Handler handlerObj = new ConsoleHandler();
        handlerObj.setLevel(Level.ALL);
        LOGGER.addHandler(handlerObj);
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);

        ConferenceController conferenceController = new ConferenceController();

        // Test stuff

        LocalDateTime start = LocalDateTime.of(2015,
                Month.JULY, 29, 19, 30, 40);
        LocalDateTime end = LocalDateTime.of(2018,
                Month.JULY, 29, 19, 30, 40);

        UUID ogUser = UUID.randomUUID();

        UUID conferenceUUID = conferenceController.createConference("bro", start, end, ogUser);

        conferenceController.removeOrganizer(conferenceUUID, UUID.randomUUID(), ogUser);


        try {
            conferenceController.deleteConference(conferenceUUID, UUID.randomUUID());
        } catch (PermissionException e) {

        }

        conferenceController.deleteConference(conferenceUUID, ogUser);


    }
}
