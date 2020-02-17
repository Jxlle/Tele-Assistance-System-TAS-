package tas.mape.knowledge;

import service.adaptation.probes.interfaces.WorkflowProbeInterface;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;

/**
 * Class representing the workflow probe for the workflow analyzer
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class WorkflowAnalyzerProbe implements WorkflowProbeInterface {

	// Workflow description data
	private WeightedCollection<Description> usedDescriptions = new WeightedCollection<Description>();
	
	/**
	 * Reset probe
	 */
	public void reset() {
		usedDescriptions = new WeightedCollection<Description>();
	}
	
	/**
	 * Return the used description collection
	 * @return the used description collection
	 */
	public WeightedCollection<Description> getUsedDescriptions() {
		return usedDescriptions;
	}

	/**
	 * Gets called when a workflow service is invoked.
	 * The description collection is changed accordingly.
	 */
	@Override
	public void serviceOperationInvoked(ServiceDescription description, String opName, Object[] params) {
		
		Description typeAndOperation = new Description(description.getServiceType(), opName);
		
		if (usedDescriptions.itemExists(typeAndOperation)) {
			usedDescriptions.increaseWeight(typeAndOperation, 1);
		}
		else {
			usedDescriptions.add(typeAndOperation, 1);
		}
	}
	
	/**
	 * Not used
	 */
	@Override
	public void serviceNotFound(String serviceType, String opName) throws IllegalStateException {
		// Should never be triggered here
		throw new IllegalStateException("Something went wrong! This probe function should never trigger!");
	}

	/**
	 * Not used
	 */
	@Override
	public void serviceOperationTimeout(ServiceDescription description, String opName, Object[] params) throws IllegalStateException {
		// Should never be triggered here
		throw new IllegalStateException("Something went wrong! This probe function should never trigger!");
	}
	
	/**
	 * Not used
	 */
	@Override
	public void serviceOperationReturned(ServiceDescription description, Object result, String opName,
			Object[] params) {
	}
	
	/**
	 * Not used
	 */
	@Override
	public void workflowStarted(Object[] params) {
	}

	/**
	 * Not used
	 */
	@Override
	public void workflowEnded(Object result, Object[] params) {
	}

}
