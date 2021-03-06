package console;

import console.conferences.ConferencesUI;
import console.contacts.ContactsUI;
import console.conversation.MessagingUI;
import contact.ContactController;
import convention.ConferenceController;
import convention.EventController;
import convention.RoomController;
import messaging.ConversationController;
import user.UserController;

/**
 * Main menu page (Post login)
 */
public class MainMenuUI {

    ConsoleUtilities consoleUtilities;

    MessagingUI messagingUI;
    ContactsUI contactsUI;
    ConferencesUI conferencesUI;

    // User controller
    UserController userController;

    /**
     * Constructs the main menu
     *
     * @param userController
     * @param contactController
     * @param conversationController
     * @param roomController
     * @param eventController
     * @param conferenceController
     */
    public MainMenuUI(UserController userController, ContactController contactController, ConversationController conversationController, RoomController roomController, EventController eventController, ConferenceController conferenceController) {
        this.userController = userController;
        this.consoleUtilities = new ConsoleUtilities(userController);

        this.messagingUI = new MessagingUI(userController, conversationController);
        this.contactsUI = new ContactsUI(userController, contactController);
        this.conferencesUI = new ConferencesUI(userController, roomController, eventController, conferenceController, conversationController);
    }

    /**
     * Run the MainMenuUI
     *
     * @return false iff the user wants to quit the program in the following UI loop
     */
    public boolean run() {

        String[] options = new String[]{
                "Messaging",
                //"Contacts",
                "Conferences",
                "Log Out",
                "Exit System"
        };

        while (true) {
            int selection = consoleUtilities.singleSelectMenu("Main Menu", options);

            switch (selection) {
                case 1:
                    messagingUI.run();
                    break;
                /*case 2:
                    contactsUI.run();
                    break;*/
                case 2:
                    conferencesUI.run();
                    break;
                case 3:
                    userController.logout();
                    return true; // Logout (i.e. return to parent menu without terminating program)
                case 4:
                    return false; // Terminate program
            }
        }
    }
}
