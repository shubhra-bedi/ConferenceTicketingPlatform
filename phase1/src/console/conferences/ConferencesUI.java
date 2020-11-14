package console.conferences;

import console.ConsoleUtilities;
import convention.ConferenceController;
import convention.EventController;
import convention.RoomController;
import convention.calendar.TimeRange;
import convention.exception.InvalidNameException;
import convention.exception.InvalidTimeRangeException;
import user.UserController;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ConferencesUI {
    ConsoleUtilities consoleUtilities;

    UserController userController;
    RoomController roomController;
    EventController eventController;
    ConferenceController conferenceController;
    UUID signedInUserUUID;

    /**
     * Creates the ConferenceUI
     * @param userController
     * @param roomController
     * @param eventController
     * @param conferenceController
     */
    public ConferencesUI(UserController userController, RoomController roomController, EventController eventController, ConferenceController conferenceController) {
        this.userController = userController;
        this.roomController = roomController;
        this.eventController = eventController;
        this.conferenceController = conferenceController;
        this.consoleUtilities = new ConsoleUtilities(userController);
    }

    /**
     * Create a new conference
     */
    public void createConference() {
        String[] fieldIDs = {
                "conferenceName",
                "startTime",
                "endTime"
        };

        Map<String, String> labels = new HashMap<String, String>() {
            {
                put("conferenceName", "Conference Name");
                put("startTime", String.format("Start Time/Date [%s]", consoleUtilities.getDateTimeFormat()));
                put("endTime", String.format("End Time/Date [%s]", consoleUtilities.getDateTimeFormat()));
            }
        };

        try {
            Map<String, String> response = consoleUtilities.inputForm("Create New Conference", labels, fieldIDs);

            // Parses input
            String conferenceName = response.get("conferenceName");
            LocalDateTime start = consoleUtilities.stringToDateTime(response.get("startTime"));
            LocalDateTime end = consoleUtilities.stringToDateTime(response.get("endTime"));
            TimeRange timeRange = new TimeRange(start, end);

            UUID newConferenceUUID = conferenceController.createConference(conferenceName, timeRange, signedInUserUUID);

            consoleUtilities.confirmBoxClear("Successfully created new conference.");

            viewSpecificConference(newConferenceUUID);
        } catch (InvalidNameException e) {
            consoleUtilities.confirmBoxClear("Invalid name. Conference name must be non empty.");
        } catch (InvalidTimeRangeException e) {
            consoleUtilities.confirmBoxClear("Invalid date range. End time must be after start time.");
        } catch (DateTimeParseException e) {
            consoleUtilities.confirmBoxClear(String.format("Invalid date. Please follow the given format. [%s]", consoleUtilities.getDateTimeFormat()));
        }
    }

    /**
     * Join an existing conference (that you're not part of, of course)
     */
    public void joinConference() {
        Set<UUID> notMyConferences = conferenceController.getNotUserConferences(signedInUserUUID);

        if (notMyConferences.size() == 0) {
            consoleUtilities.confirmBoxClear("There aren't any conferences you can join.");
        } else {
            UUID selectedConferenceUUID = displayConferences("Select a conference to join", notMyConferences);

            if (selectedConferenceUUID != null) {

                conferenceController.addAttendee(selectedConferenceUUID, signedInUserUUID);

                consoleUtilities.confirmBoxClear("Successfully joined conference.");

                viewSpecificConference(selectedConferenceUUID);
            }
        }

    }

    /**
     * Displays menu with conferences the user is affiliated with.
     */
    public void viewMyConferences() {
        Set<UUID> myConferences = conferenceController.getUserConferences(signedInUserUUID);

        if (myConferences.size() == 0) {
            consoleUtilities.confirmBoxClear("You are currently not affiliated with any conferences.");
        } else {
            UUID selectedConferenceUUID = displayConferences("Select a conference to view", myConferences);

            if (selectedConferenceUUID != null) {
                viewSpecificConference(selectedConferenceUUID);
            }
        }
    }

    /**
     * Displays a list of conferences and fetches relevant metadata
     * <p>
     * Allows user to select conference to operate on.
     *
     * @param instructions string with instructions for this menu
     * @param conferences  set of conference UUIDs
     * @return UUID of the selected conference. Null if the user makes no selection.
     */
    private UUID displayConferences(String instructions, Set<UUID> conferences) {

        /**
         * TODO: Update this to the more detailed table to show more metadata
         */

        // Now we have the UUIDs in order
        List<UUID> conferenceUUIDs = new ArrayList<>(conferences);

        // Time to grab the conference names
        String[] conferenceNames = new String[conferenceUUIDs.size() + 1];

        // Back button
        conferenceNames[conferenceUUIDs.size()] = "Back";

        for (int i = 0; i < conferenceUUIDs.size(); i++) {
            UUID conferenceUUID = conferenceUUIDs.get(i);
            conferenceNames[i] = conferenceController.getConferenceName(conferenceUUID) + " " + conferenceController.getConferenceTimeRange(conferenceUUID);
        }

        // Arrays start a 0, so subtract
        int selectionIndex = consoleUtilities.singleSelectMenu(instructions, conferenceNames) - 1;

        if (selectionIndex < conferenceUUIDs.size()) {
            return conferenceUUIDs.get(selectionIndex);
        } else {
            return null; // Back button was pressed
        }
    }

    /**
     * Create a conversation with any number of users in this conference
     * @param conferenceUUID
     */
    public void messageUsers(UUID conferenceUUID) {

        Set<UUID> conferenceUserUUIDs = conferenceController.getUsers(conferenceUUID, signedInUserUUID);
        Set<UUID> conversationUserUUIDs = new HashSet<>();
        conversationUserUUIDs.add(signedInUserUUID);

        // Fetch the users to add tot he conversation
        boolean running = true;

        while (running) {
            // Gets a set of users who aren't currently set to be added to the conversation
            Set<UUID> availableUserUUIDs = new HashSet<>(conferenceUserUUIDs);
            availableUserUUIDs.removeAll(conversationUserUUIDs);

            // Now, we'll grab the user's names
            ArrayList<UUID> orderedAvailableUserUUIDs = new ArrayList<>(availableUserUUIDs);
            String[] options = new String[orderedAvailableUserUUIDs.size() + 2];

            for (int i = 0; i < availableUserUUIDs.size(); i++) {
                options[i] = userController.getUserFullName(orderedAvailableUserUUIDs.get(i));
            }

            // The last two options are the back button
            options[availableUserUUIDs.size()] = "Done";
            options[availableUserUUIDs.size() + 1] = "Cancel";

            // Arrays start at 0, so subtract 1
            int selection = consoleUtilities.singleSelectMenu("Add a user to the conversation", options) - 1;

            if (selection == availableUserUUIDs.size()) { // Finish creation
                boolean confirm = consoleUtilities.booleanSelectMenu(String.format("Are you sure you want to create a conversation with %d users?", conversationUserUUIDs.size()));

                if (confirm) {
                    running = false;
                }
            } else if (selection == availableUserUUIDs.size() + 1) { // Cancel
                boolean confirm = consoleUtilities.booleanSelectMenu("Are you sure you want to cancel creating this conversation?");

                if (confirm) {
                    return;
                }
            } else { // Add user to list
                conversationUserUUIDs.add(orderedAvailableUserUUIDs.get(selection));
            }
        }

        // Actually create the conversation
        UUID newConversationUUID = conferenceController.createConversationWithUsers(conferenceUUID, signedInUserUUID, conversationUserUUIDs);
        consoleUtilities.confirmBoxClear(String.format("New conversation created with %d users (including you)", conversationUserUUIDs.size()));

        /**
         * TODO: Open the new conversation?
         */
    }

    /**
     * Create a room in this conference
     * @param conferenceUUID
     */
    public void createRoom(UUID conferenceUUID) {

        /**
         * TODO: Remove test code
         */
        // Create two mock rooms
        roomController.createRoom(conferenceUUID, signedInUserUUID, "BA6969", 2);
        roomController.createRoom(conferenceUUID, signedInUserUUID, "BA6970", 2);

        consoleUtilities.confirmBoxClear("two test rooms created");
    }

    /**
     * UI Menu for specific conference
     * @param conferenceUUID UUID of conference to view
     */
    public void viewSpecificConference(UUID conferenceUUID) {

        /**
         * Need a way to hide options in menu
         *
         * Display general information at the top of the menu
         *  - View your events (Attendee)
         *  - View your events (Spepaker)
         *  - View all events
         *      - <this is the event menu now>
         *          - DISPLAY GENERAL EVENT INFORMATION
         *          - Register for event/Unregister from event
         *          - Create an event conversation/Open event conversation (Speaker)
         *          - Delete event
         *  - Create Event (Organizer)
         *
         *  - View rooms
         *      - View room schedule
         *      - Delete room
         *  - Create Room
         *
         *  - Organizer settings
         *    - Manage users
         *      - Create conversation
         *        - Delete conference
         */

        String conferenceName = conferenceController.getConferenceName(conferenceUUID);
        int numEvents;
        int numRooms;
        int numAttendees;

        // We store a mapping from the menu ID to the label
        // For each permission, we'll set out a different list of selection IDs, and then generate the selection menu using that
        HashMap<String, String> selectionIDToLabel = new HashMap<String, String>() {
            {
                put("yourAttendeeEvents", "View Registered Events");
                put("yourSpeakerEvents", "[Speaker] View Registered Events");
                put("viewEvents", "View all Events");
                put("createEvent", "[Organizer] Create Event");
                put("createRoom", "[Organizer] Create Room");
                put("messageUsers", "[Organizer] Message Users");
                put("back", "Back");
            }
        };

        String[] attendeeSelectionIDs = {
                "yourAttendeeEvents",
                "viewEvents",
                "back"
        };

        String[] speakerSelectionIDs = new String[] {
                "yourAttendeeEvents",
                "yourSpeakerEvents",
                "viewEvents",
                "back"
        };

        String[] organizerSelectionIDs = new String[] {
                "yourAttendeeEvents",
                "yourSpeakerEvents",
                "viewEvents",
                "createEvent",
                "createRoom",
                "messageUsers",
                "back"
        };

        // Displays the menu options based on permission
        boolean isSpeaker = conferenceController.isSpeaker(conferenceUUID, signedInUserUUID, signedInUserUUID);
        boolean isOrganizer = conferenceController.isOrganizer(conferenceUUID, signedInUserUUID, signedInUserUUID);

        String role;
        String[] selectionIDs;

        if (isOrganizer) {
            role = "Organizer";
            selectionIDs = organizerSelectionIDs;
        } else if (isSpeaker) {
            role = "Speaker";
            selectionIDs = speakerSelectionIDs;
        } else {
            role = "Attendee";
            selectionIDs = attendeeSelectionIDs;
        }

        /* Alright, lets get these labels ready */
        String[] options = new String[selectionIDs.length];

        for (int i = 0; i < selectionIDs.length; i++) {
            options[i] = selectionIDToLabel.get(selectionIDs[i]);
        }

        boolean running = true;

        while (running) {
            // These statistics may change every loop, so we'll update them here
            numEvents = eventController.getEvents(conferenceUUID, signedInUserUUID).size();
            numRooms = roomController.getRooms(conferenceUUID, signedInUserUUID).size();
            numAttendees = conferenceController.getAttendees(conferenceUUID, signedInUserUUID).size();

            // We use the selection ID here instead of just the option index, as it may change with more or less options
            int selection = consoleUtilities.singleSelectMenu(String.format("Conference: %s | Role: %s | # Events: %d | # Rooms: %d | # Attendees: %d", conferenceName, role, numEvents, numRooms, numAttendees), options);
            String selectionID = selectionIDs[selection - 1]; // Arrays start at 0

            switch (selectionID) {
                case "yourAttendeeEvents":
                    consoleUtilities.confirmBoxClear("Should be attendee events");
                    break;
                case "yourSpeakerEvents":
                    consoleUtilities.confirmBoxClear("Should be speaker events");
                    break;
                case "viewEvents":
                    consoleUtilities.confirmBoxClear("Should be all events");
                    break;
                case "createEvent":
                    consoleUtilities.confirmBoxClear("Create an event");
                    break;
                case "createRoom":
                    createRoom(conferenceUUID);
                    break;
                case "messageUsers":
                    messageUsers(conferenceUUID);
                    break;
                case "back":
                    running = false;
            }
        }
    }

    /**
     * Run conference UI
     */
    public void run() {
        // We fetch the user UUID here so we keep it up to date
        this.signedInUserUUID = userController.getCurrentUser();

        String[] options = new String[]{
                "Create a conference",
                "Join a conference",
                "View your conferences",
                "Back"
        };

        boolean running = true;

        while (running) {
            int selection = consoleUtilities.singleSelectMenu("Conference Options", options);

            switch (selection) {
                case 1:
                    createConference();
                    break;
                case 2:
                    joinConference();
                    break;
                case 3:
                    viewMyConferences();
                    break;
                case 4:
                    running = false;
            }
        }
    }
}
