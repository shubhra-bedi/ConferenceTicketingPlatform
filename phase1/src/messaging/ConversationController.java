package messaging;

import contact.ContactManager;
import exception.MessageDeniedException;

import java.util.Set;
import java.util.UUID;

public class ConversationController {
    private ConversationManager convoManager;
    private ContactManager contactManager;

    public ConversationController(ContactManager contactManager, ConversationManager conversationManager) {
        this.contactManager = contactManager;
        this.convoManager = conversationManager;
    }

    /**
     * Checks if the User have access to send message to another User
     *
     * @param sender   UUID of the person who wants to send the message
     * @param receiver UUID of the person to whom the message is to be sent
     * @return true iff the receiver is in the friend list of sender
     */
    private boolean checkAccess(UUID sender, UUID receiver) {
        return contactManager.getContacts(sender).contains(receiver);
    }

    /**
     * Sends a particular message to a specific chat
     *
     * @param message the message to be sent
     * @param convId  the conversation Id of the conversation to which this message has to be added
     */
    public void sendMessage(Message message, UUID convId) {
        convoManager.sendMessage(message, convId);
    }

    /**
     * Initiates a new Chat(Conversation) between 2 or more users.
     *
     * @param convName     the name of the Chat to be initiated
     * @param executorUUID the UUID of the user running this operation
     * @param otherUsers   the set of other users in this conversation
     * @param convMessages the message that initiated the need for this Chat to be created
     */
    public UUID initiateConversation(String convName, UUID executorUUID, Set<UUID> otherUsers, Message convMessages) {

        for (UUID otherUserUUID : otherUsers) {
            if (!checkAccess(executorUUID, otherUserUUID)) {
                throw new MessageDeniedException(executorUUID, otherUserUUID);
            }
        }

        // Let the other users read and write
        return convoManager.createConversation(convName, otherUsers, otherUsers, convMessages);
    }

    /**
     * Adds user to the a specific chat
     *
     * @param userUUID         The userId of the user to be added to the Chat
     * @param conversationUUID The UUID of the conversation/chat to which the user needs to be added
     */
    public boolean addUser(UUID userUUID, UUID conversationUUID) {
        return convoManager.addUser(userUUID, conversationUUID);
    }

    /**
     * Adds user to the a specific chat
     *
     * @param userUUID         The userId of the user to be added to the Chat
     * @param conversationUUID The UUID of the conversation/chat to which the user needs to be added
     */
    public boolean removeUser(UUID userUUID, UUID conversationUUID) {
        return convoManager.removeUser(userUUID, conversationUUID);
    }

}
