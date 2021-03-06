package tas.communication.message;

/**
 * Class representing the message type for communication between planner components.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class PlannerMessage extends ComponentMessage<PlannerMessageContent> {

	/**
	 * Create a new planner message with a given id, receiver endpoint, sender endpoint, message type and message content.
	 * @param id the id of the message (identifier)
	 * @param receiverEndpoint the given receiver endpoint
	 * @param senderEndpoint the given sender endpoint
	 * @param msgType the given message type
	 * @param content the given message content
	 */
	public PlannerMessage(int id, String receiverEndpoint, String senderEndpoint, String msgType, PlannerMessageContent content) {
		super(id, receiverEndpoint, senderEndpoint, msgType, content);
	}
}
