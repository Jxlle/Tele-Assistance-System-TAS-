package tas.mape.communication.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.message.ComponentMessage;
import tas.mape.communication.message.ComponentMessageHost;

/**
 * Abstract class representing the structure of a protocol.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 *
 * @param <T> the message type
 * @param <E> the communication component type
 * @note Currently only supports communication between components of the same type, sending the same type of messages
 */
public abstract class AbstractProtocol<T extends ComponentMessage<?>, E extends CommunicationComponent<T>> {
	
	// Message ID of the last sent message
	protected int messageID;
	
	// Needed amount of components to start the protocol
	protected int neededAmountOfComponents;
	
	// Maximum amount of iterations before the protocol stops
	protected int maxIterations;
	
	// The list of components that are using this protocol
	protected List<E> components = new ArrayList<>();
	
	// The message host of the components using this protocol
	private ComponentMessageHost<T> messageHost = new ComponentMessageHost<T>();
	
	/**
	 * Execute the protocol with the currently used communication components that will communicate and a 
	 * given maximum amount of iterations
	 * @param maxIterations the given maximum amount of iterations
	 * @throws IllegalArgumentException throw when the used protocol doesn't support the given amount of components
	 */
	public void executeProtocol(int maxIterations) throws IllegalArgumentException {
		
		if (components.size() != getNeededAmountOfComponents()) {
			throw new IllegalArgumentException("The used protocol doesn't support the current amount of components: " + components.size());
		}
		
		this.maxIterations = maxIterations;
		startProtocol();
	}
	
	/**
	 * Handle a given message that was received by the given communication component
	 * @param message the given message
	 * @param receiver the given communication component (receiver)
	 */
	public abstract void receiveAndHandleMessage(T message, E receiver);
	
	/**
	 * Return the index of a randomly chosen component
	 * @return the index of the chosen component
	 */
	protected int getRandomComponentIndex() {
		return ThreadLocalRandom.current().nextInt(0, getNeededAmountOfComponents() + 1);
	}
	
	/**
	 * Reset protocol data
	 */
	protected void resetProtocol() {
		messageID = 0;
	}
	
	/**
	 * Add a given component to the components list and register the
	 * component to the protocol message host.
	 * @param component the given component
	 */
	public void addComponent(E component) {
		components.add(component);
		messageHost.register(component);
	}
	
	/**
	 * Remove a given component from the components list and unregister the
	 * component from the protocol message host.
	 * @param component the given component to be removed
	 */
	public void removeComponent(E component) {
		components.remove(component);
		messageHost.unRegister(component);
	}
	
	/**
	 * Clear protocol components
	 */
	public void resetComponents() {
		components.clear();
	}
	
	/**
	 * Return the exact amount of components needed before the protocol can start
	 * @return the exact amount of components needed
	 */
	public int getNeededAmountOfComponents() {
		return neededAmountOfComponents;
	}
	
	/**
	 * Start the protocol, choose starting and receiving components to begin communication
	 */
	protected abstract void startProtocol();
	
	/**
	 * Initialize local protocol properties and let a starting communication component send 
	 * its first message to given receiver(s) to start the protocol.
	 * @param startIndex the given index of the starting component
	 * @param receiverIndices the given indices of the receivers of the first message
	 */
	protected abstract void InitializeAndSendFirstMessage(int startIndex, int... receiverIndices);
}
