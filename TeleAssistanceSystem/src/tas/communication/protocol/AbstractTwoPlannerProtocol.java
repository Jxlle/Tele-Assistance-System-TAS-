package tas.communication.protocol;

import java.util.ArrayList;
import java.util.List;

import tas.communication.message.PlannerMessage;
import tas.communication.message.PlannerMessageContent;
import tas.mape.planner.Planner;
import tas.mape.planner.RatingType;
import tas.mape.planner.ServiceCombination;

/**
 * Abstract class representing the structure of a two-component planner protocol.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public abstract class AbstractTwoPlannerProtocol extends AbstractPlannerProtocol {

	/**
	 * Create a new planner two-component protocol
	 */
	public AbstractTwoPlannerProtocol() {
		super(2);
	}

	// Shared registry endpoints between both planners
	// This is data that should be exchanged at the start of the system, before the protocol
	protected List<String> sharedRegistryEndpoints;
		
	/**
	 * Initialize local protocol properties
	 * @param startIndex the given index of the starting component
	 * @param receiverIndices the given indices of the receivers of the first message
	 */
	@Override
	protected void InitializeProtocol(int startIndex, List<Integer> receiverIndices) {
		sharedRegistryEndpoints = participatingComponents.get(0).getRegistryEndpoints();
		List<String> registryEndpointsOther = participatingComponents.get(1).getRegistryEndpoints();
		
		// Calculate shared registry endpoints
		sharedRegistryEndpoints.retainAll(registryEndpointsOther);
	}
	
	/**
	 * Let a starting communication component send 
	 * its first message to a given receiver to start the protocol.
	 * @param startIndex the given index of the starting component
	 * @param receiverIndices the given list of receiver indices
	 */
	@Override
	protected void sendFirstMessage(int startIndex, List<Integer> receiverIndices) {
	
		ServiceCombination chosenCombination;
		Planner sender = participatingComponents.get(startIndex);
		Planner receiver = participatingComponents.get(receiverIndices.get(0));
		
		if (sender.getAvailableServiceCombinations().get(0).getRatingType() == RatingType.CLASS) {
			// Choose random best combination
			List<ServiceCombination> bestCombinations = new ArrayList<>();
			
			for (ServiceCombination s : sender.getAvailableServiceCombinations()) {
				if (s.getRating().equals(sender.getAvailableServiceCombinations().get(0).getRating())) {
					bestCombinations.add(s);
				}
			}
			
			chosenCombination = bestCombinations.get(AbstractProtocol.random.nextInt(bestCombinations.size()));
		}
		else {
			chosenCombination = sender.getAvailableServiceCombinations().get(0);
		}
		
		// Set chosen combination
		sender.setCurrentServiceCombination(chosenCombination);
		
		// Make message
		PlannerMessageContent content = sender.generateMessageContent(chosenCombination, sharedRegistryEndpoints, usedMessageContentPercentage);
		PlannerMessage message = new PlannerMessage(messageID, receiver.getEndpoint(), sender.getEndpoint(), "FIRST_OFFER", content);
		
		System.out.println("PROTOCOL STARTED");
		
		// Send message
		sender.sendMessage(message);
	}
}
