package tas.mape.system.entity;

import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.composite.CompositeService;
import tas.mape.analyzer.Analyzer;
import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.executer.Executer;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Knowledge;
import tas.mape.monitor.Monitor;
import tas.mape.planner.Planner;
import tas.mape.planner.RatingType;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Class representing a component that can execute a MAPEK-loop.
 * This is a managing system for the managed system, the workflow executer.
 */
public class MAPEKComponent {
	
	/**
	 * Private class used as a builder to build MAPE-K component objects.
	 * This class is used to avoid a big constructor for the MAPE-K component.
	 */
	public static class Builder {
		
		// Components
		private Monitor monitor;
		private Analyzer analyzer;
		private Planner planner;
		private Executer executer;
		private Knowledge knowledge;
		
		/**
		 * Create a new builder with an initialized executer
		 */
		public Builder() {
			executer = new Executer();
		}
		
		/**
		 * Creates a new builder where the knowledge component with its given parameters has been initialized
		 * @param loadFailureDelta the given load failure delta 
		 * @param currentQoSRequirement the given current QoS requirement
		 * @param serviceRegistryEndpoints the given registry endpoints used by the workflow
		 * @param usableServicesAndChance map containing usable service information with additional usage chance
		 * @return the new Builder object with initialized knowledge
		 */
		public Builder initializeKnowledge(int loadFailureDelta, String currentQoSRequirement, List<String> serviceRegistryEndpoints, Map<Description, 
				Pair<List<ServiceDescription>, Double>> usableServicesAndChance) {
			
			knowledge = new Knowledge(loadFailureDelta, currentQoSRequirement, serviceRegistryEndpoints, usableServicesAndChance);
			return this;
		}
		
		/**
		 * Creates a new builder where the planner component with its given parameters has been initialized.
		 * @param plannerEndpoint the given planner end point
		 * @return the new Builder object with initialized planner
		 * @throws InstantiationException throw when the executer field is null
		 */
		public Builder initializePlanner(String plannerEndpoint) throws InstantiationException {
			
			if (executer == null) {
				throw new InstantiationException("Executer field is null!");
			}
			
			planner = new Planner(plannerEndpoint, executer);
			return this;
		}
		
		/**
		 * Creates a new builder where the analyzer component with its given parameters has been initialized.
		 * @param combinationLimit the given combination limit that will decide how much service combinations 
		 *        will be chosen in the execute step
		 * @param ratingType the given type of the rating for service combinations 
		 * @param QoSStrategies a map containing the strategy number for each QoS requirement
		 * @return the new Builder object with initialized analyzer
		 * @throws InstantiationException throw when the knowledge field is null
		 * @throws InstantiationException throw when the planner field is null
		 */
		public Builder initializeAnalyzer(int combinationLimit, RatingType ratingType, Map<String, Integer> QoSStrategies) 
				throws InstantiationException {
			
			if (knowledge == null) {
				throw new InstantiationException("Knowledge field is null!");
			}
			
			if (planner == null) {
				throw new InstantiationException("Planner field is null!");
			}
			
			analyzer = new Analyzer(knowledge, planner, combinationLimit, ratingType, QoSStrategies);
			return this;
		}
		
		/**
		 * Creates a new builder where the monitor component with its given parameters has been initialized.
		 * @param minFailureDelta the given minimum failure delta
		 * @param failureChange the given failure change
		 * @return the new Builder object with initialized monitor
		 * @throws InstantiationException throw when the knowledge field is null
		 * @throws InstantiationException throw when the analyzer field is null
		 */
		public Builder initializeMonitor(double minFailureDelta, double failureChange) throws InstantiationException {
			
			if (knowledge == null) {
				throw new InstantiationException("Knowledge field is null!");
			}
			
			if (analyzer == null) {
				throw new InstantiationException("Analyzer field is null!");
			}
			
			monitor = new Monitor(knowledge, analyzer, minFailureDelta, failureChange);
			return this;
		}
		
		/**
		 * Creates and returns a MAPE-K component built with the initialized data.
		 * @return the built MAPE-K component
		 * @throws IllegalStateException throw when some components haven't been initialized.
		 */
		public MAPEKComponent build() throws IllegalStateException {
			
			MAPEKComponent component = new MAPEKComponent();
			
			if (monitor == null) {
				throw new IllegalStateException("The build process can't be started, some components haven't been initialized!");
			}
			
			component.monitor = monitor;
			component.analyzer = analyzer;
			component.planner = planner;
			component.executer = executer;
			component.knowledge = knowledge;
			
			return component;
		}	
	}
	
	// MAPE-K components
	private Monitor monitor;
	private Analyzer analyzer;
	private Planner planner;
	private Executer executer;
	private Knowledge knowledge;
	
	/**
	 * Create a new MAPE-K component.
	 * @note The constructor is private because the only way to create this component is through the builder class.
	 */
	private MAPEKComponent() {}
	
	// The methods below are exact same methods found in the components. These are
	// implemented so the user can change component parameters without misusing the components.
	
	/**
	 * Initialize the executer components with the given composite service.
	 * @param compositeService the given composite service
	 */
	public void initializeExecuterEffectors(CompositeService compositeService) {
		executer.initializeEffectors(compositeService);
	}
	
	/**
	 * Connect all monitor probes with the probes of the given composite service
	 * @param compositeService the given composite service
	 */
	public void connectMonitorProbes(CompositeService compositeService) {
		monitor.connectProbes(compositeService);
	}
	
	/**
	 * Set minimum failure delta to the given value
	 * @param minFailureDelta the new minimum failure delta
	 */
	public void setMinFailureDelta(double minFailureDelta) {
		monitor.setMinFailureDelta(minFailureDelta);
	}
	
	/**
	 * Return the minimum failure delta
	 * @return the minimum failure delta
	 */
	public double getMinFailureDelta() {
		return monitor.getMinFailureDelta();
	}
	
	/**
	 * Set failure change to the given value
	 * @param failureChange the new failure change
	 */
	public void setFailureChange(double failureChange) {
		monitor.setFailureChange(failureChange);
	}
	
	/**
	 * Return the failure change
	 * @return the failure change
	 */
	public double getFailureChange() {
		return monitor.getFailureChange();
	}
	
	/**
	 * Return the strategy number for a given QoS requirement name
	 * @param requirementName the given QoS requirement name
	 * @return the strategy number
	 */
	public Integer getQoSStrategy(String requirementName) {
		return analyzer.getQoSStrategy(requirementName);
	}
	
	/**
	 * Update or add a certain QoS strategy number using a given QoS requirement name and strategy number
	 * @param requirementName the given QoS requirement name
	 * @param strategy the given strategy number
	 */
	public void setQoSStrategy(String requirementName, Integer strategy) {
		analyzer.setQoSStrategy(requirementName, strategy);
	}
	
	/**
	 * Remove the QoS strategy with the given QoS requirement name key
	 * @param requirementName the given QoS requirement
	 */
	public void removeQoSStrategy(String requirementName) {
		analyzer.removeQoSStrategy(requirementName);
	}
	
	/**
	 * Clear the QoS strategies map
	 */
	public void clearQoSStrategies() {
		analyzer.clearQoSStrategies();
	}
	
	/**
	 * Set the currently used protocol to a given protocol and add this planner
	 * to the protocol components
	 * @param protocol the given protocol
	 */
	public void setProtocol(AbstractProtocol<PlannerMessage, Planner> protocol) {
		planner.setProtocol(protocol);
	}
	
	/**
	 * Return the currently used protocol
	 * @return the currently used protocol
	 */
	public AbstractProtocol<PlannerMessage, Planner> getProtocol() {
		return planner.getProtocol();
	}
	
	/**
	 * Return the system goals
	 * @return the system goals
	 */
	public List<Goal> getGoals() {
		return knowledge.getGoals();
	}
	
	/**
	 * Add a given goal to the list of goals
	 * @param goal the given goal
	 */
	public void addGoal(Goal goal) {
		knowledge.addGoal(goal);
	}
	
	/**
	 * Remove a given goal from the list of goals
	 * @param goal the given goal
	 */
	public void removeGoal(Goal goal) {
		knowledge.removeGoal(goal);
	}
	
	/**
	 * Change the list of goals to a given list of goals
	 * @param goals the given list of goals
	 */
	public void changeGoals(List<Goal> goals) {
		knowledge.changeGoals(goals);
	}
	
	/**
	 * Reset the goals
	 */
	public void resetGoals() {
		knowledge.resetGoals();
	}
	
	// The methods below are used to simulate the mape loop for multiple workflow entities 'concurrently'
	// instead of using multiple threads. Using multiple threads is possible, but can introduce race conditions.
	
	/**
	 * Execute the monitor component
	 */
	public void executeMonitor() {
		monitor.execute();
	}
	
	/**
	 * Execute the analyzer component
	 */
	public void executeAnalyzer() {
		monitor.triggerAnalyzer();
	}
	
	/**
	 * Execute the planner component
	 */
	public void executePlanner() {
		analyzer.triggerPlanner();
	}
	
	/**
	 * Execute the executer component
	 */
	public void executeExecuter() {
		planner.triggerExecuter();
	}
}