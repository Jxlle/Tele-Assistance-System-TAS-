package tas.mape.system.structure;

import tas.data.serviceinfo.GlobalServiceInfo;
import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.communication.protocol.PlannerProtocolDoNothing;
import tas.mape.planner.Planner;
import tas.mape.system.entity.SystemEntity;

/**
 * Class representing a double-loop system containing two system entities.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class DoubleLoopSystem extends AbstractMultiLoopSystem<SystemEntity, PlannerMessage, Planner> {

	/**
	 * Create a new double-loop system with two given system entities
	 * @param systemEntities the given system entities
	 * @throws IllegalArgumentException throw when the given 
	 *         amount of entities is not supported by the system
	 */
	public DoubleLoopSystem(SystemEntity[] systemEntities) throws IllegalArgumentException {
		super(systemEntities);
	}
	
	/**
	 * Execute the double-loop system with a given amount of execution cycles and no protocol
	 * The system will execute both entities without ever communicating.
	 * @param executionCycles the given amount of execution cycles
	 */
	@Override
	public void executeSystem(int executionCycles) {
		executeSystem(executionCycles, new PlannerProtocolDoNothing(), 0);
	}
	
	/**
	 * Execute the double-loop system with a given amount of execution cycles following a certain protocol
	 * @param executionCycles the given amount of execution cycles
	 * @param protocol the given protocol
	 * @param maxIterations the given maximum amount of iterations
	 */
	@Override
	public void executeSystem(int executionCycles, AbstractProtocol<PlannerMessage, Planner> protocol, int maxIterations) {
		
		// System entities
		SystemEntity entity1 = getSystemEntity(0);
		SystemEntity entity2 = getSystemEntity(1);
		
		// Set system protocol
		entity1.getManagingSystem().setProtocol(protocol);
		entity2.getManagingSystem().setProtocol(protocol);
		
		for (int i = 0; i < executionCycles; i++) {
			
			// Execute MAPE-K loop until the communication step
			entity1.getManagingSystem().executeMonitor();
			entity2.getManagingSystem().executeMonitor();
			entity1.getManagingSystem().executeAnalyzer();
			entity2.getManagingSystem().executeAnalyzer();
			entity1.getManagingSystem().executePlanner();
			entity2.getManagingSystem().executePlanner();
			
			// Execute protocol, planners communicate in this step
			protocol.executeProtocol(maxIterations);
			
			// Execute executers
			entity1.getManagingSystem().executeExecutor();
			entity2.getManagingSystem().executeExecutor();
			
			// Execute workflow
			entity1.getManagedSystem().executeWorkflow();
			entity2.getManagedSystem().executeWorkflow();
			
			// Reset all service loads after each execution cycle
			GlobalServiceInfo.resetServiceLoads();
		}
		
		// Reset approximated failure rates table after the run
		entity1.getManagingSystem().resetApproximatedServiceFailureRates();
		entity2.getManagingSystem().resetApproximatedServiceFailureRates();
	}

	/**
	 * Return the amount of needed entities in the system
	 * @return the amount of needed entities in the system
	 */
	@Override
	public int getSystemEntityCount() {
		return 2;
	}
}
