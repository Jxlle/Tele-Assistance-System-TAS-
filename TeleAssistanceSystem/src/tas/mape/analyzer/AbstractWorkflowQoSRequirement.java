package tas.mape.analyzer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.StaticTree;
import service.auxiliary.WeightedCollection;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Goal.GoalType;
import tas.mape.knowledge.Knowledge;
import tas.mape.planner.RatingType;
import tas.mape.planner.ServiceCombination;

/**
 * An abstract class for choosing service combinations in the analyzer step of the MAPE-K loop 
 * based on a QoS requirement and strategy.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public abstract class AbstractWorkflowQoSRequirement {
	
	// Local map used for storing failure rates
	private HashMap<Description, Double> failureRates;
	
	/**
	 * Generate usable service combinations with a given generation strategy, combination limit, rating type and knowledge 
	 * based on the knowledge requirement, goals, rating type and more.
	 * @param generationStrategy the given generation strategy number
	 * @param combinationLimit the given limit of returned service combinations
	 * @param ratingType the given rating type
	 * @param knowledge the given knowledge
	 * @return a list of the chosen service combinations
	 * @throws IllegalArgumentException throw when the combination limit is illegal
	 */
	@SuppressWarnings("unchecked")
	public List<ServiceCombination> getServiceCombinations(int generationStrategy, int combinationLimit, RatingType ratingType, Knowledge knowledge) 
					throws IllegalArgumentException {
		
		// Check if given combination limit is legal
		if (combinationLimit < 1)
			throw new IllegalArgumentException("Combination limit must be at least 1.");
		
		Method method = null;
		List<Map<Description, WeightedCollection<ServiceDescription>>> allServiceCombinations = null;
		
		// Search service combination generator 
		try {
			method = this.getClass().getMethod("getAllServiceCombinations" + generationStrategy, Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		// Invoke method
		try {
			allServiceCombinations = (List<Map<Description, WeightedCollection<ServiceDescription>>>) 
					method.invoke(this, knowledge.getUsableServices());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}	

		return getServiceCombinations(combinationLimit, ratingType, knowledge, allServiceCombinations);
	}
	
	/**
	 * Re-rank the given service combinations with a given map of service loads and a given knowledge
	 * @param serviceCombinations the given service combinations
	 * @param serviceLoads the given map of service loads
	 * @param knowledge knowledge
	 * @return the new service combinations
	 */
	public abstract List<ServiceCombination> getNewServiceCombinations(List<ServiceCombination> serviceCombinations, 
			Map<String, Integer> serviceLoads, Knowledge knowledge);
	
	/**
	 * STRATEGY 1: Only a single service per service description
	 * Generate and return all possible service combinations without rating or type
	 * @param usableServices a map of usable services where each key is a service type & operation name combination (description) 
	 *        and the value is a list of the usable services for that description 
	 * @return the generated service combinations without rating or type
	 */
	@CombinationStrategy(combinationInfo = "Selection of a single service instance per type")
	public List<Map<Description, WeightedCollection<ServiceDescription>>> getAllServiceCombinations1(Map<Description, 
			List<ServiceDescription>> usableServices) {
		
		// Return the service combinations
		return getAllServiceCombinationsGeneral(usableServices, 1);
	}
	
	/**
	 * STRATEGY 2: One or two services per service description, equal (50/50) usage chance
	 * Generate and return all possible service combinations without rating or type
	 * @param usableServices a map of usable services where each key is a service type & operation name combination (description) 
	 *        and the value is a list of the usable services for that description 
	 * @return the generated service combinations without rating or type
	 */
	@CombinationStrategy(combinationInfo = "Selection of one/two service instances per type (50/50)")
	public List<Map<Description, WeightedCollection<ServiceDescription>>> getAllServiceCombinations2(Map<Description, 
			List<ServiceDescription>> usableServices) {
		
		// Return the service combinations
		return getAllServiceCombinationsGeneral(usableServices, 2);
	}
	
	/**
	 * STRATEGY 3: One or three services per service description, equal (33/33/33) usage chance
	 * Generate and return all possible service combinations without rating or type
	 * @param usableServices a map of usable services where each key is a service type & operation name combination (description) 
	 *        and the value is a list of the usable services for that description 
	 * @return the generated service combinations without rating or type
	 */
	@CombinationStrategy(combinationInfo = "Selection of one/three service instances per type (33/33/33)")
	public List<Map<Description, WeightedCollection<ServiceDescription>>> getAllServiceCombinations3(Map<Description, 
			List<ServiceDescription>> usableServices) {
		
		// Return the service combinations
		return getAllServiceCombinationsGeneral(usableServices, 3);
	}

	/**
	 * A method used to generate return all possible service combinations without rating or type.
	 * It generates all one-service-per-description combinations plus all n-services-per-description 
	 * combinations with equal usage chances.
	 * @param usableServices a map of usable services where each key is a service type & operation name combination (description) 
	 *        and the value is a list of the usable services for that description 
	 * @param combinationSize the given amount of used services per description for additional service combinations
	 * @return the generated service combinations without rating or type
	 */
	private List<Map<Description, WeightedCollection<ServiceDescription>>> getAllServiceCombinationsGeneral(Map<Description, 
			List<ServiceDescription>> usableServices, int combinationSize) {
		
		List<List<WeightedCollection<ServiceDescription>>> usableServicesCollection = new ArrayList<>();
		List<WeightedCollection<ServiceDescription>> serviceCollectionList;
		WeightedCollection<ServiceDescription> serviceCollection;
		List<Description> descriptionList = new ArrayList<>();
		
		// Transform the given map into a suitable list
		for (Description description : usableServices.keySet()) {
			descriptionList.add(description);
			serviceCollectionList = new ArrayList<>();
			
			// Add every service for this service description
			for (ServiceDescription service : usableServices.get(description)) {
	        	serviceCollection = new WeightedCollection<ServiceDescription>();
	        	serviceCollection.add(service, 100);
	        	serviceCollectionList.add(serviceCollection);
			}
			
			// Add every other service collection
			if (combinationSize > 1) {
				for (WeightedCollection<ServiceDescription> collection : getAllMultiCollectionCombinationsEqualChances(usableServices.get(description), 
						combinationSize))
					serviceCollectionList.add(collection);
			}
		
			usableServicesCollection.add(serviceCollectionList);
		}
		
		// Return all possible collection combinations
		return getAllCollectionCombinations(usableServicesCollection, descriptionList);
	}
	
	/**
	 * Calculate the accumulated value of a given property for a given service combination.
	 * @param combination the given service combination without the rating and rating type
	 * @param property the given property
	 * @return the total value
	 */
	public double getTotalValue(Map<Description, WeightedCollection<ServiceDescription>> combination, String property) {
		
		double totalValue = 0;
		
		for (Description description : combination.keySet()) {
			
			for (ServiceDescription service : combination.get(description).getItems()) {
				
				if (service.getCustomProperties().containsKey(property)) {
					totalValue += (double) service.getCustomProperties().get(property) * combination.get(description).getChance(service);
				}
			}
		}
		
		return totalValue;
	}
	
	/**
	 * Calculate the accumulated value of a given property for a given service combination.
	 * @param combination the given service combination
	 * @param property the given property
	 * @return the total value
	 */
	public static double getTotalValue(ServiceCombination combination, String property) {
		
		double totalValue = 0;
		
		for (Description description : combination.getDescriptions()) {
			
			for (ServiceDescription service : combination.getAllServices(description).getItems()) {
				
				if (service.getCustomProperties().containsKey(property)) {
					totalValue += (double) service.getCustomProperties().get(property) * combination.getAllServices(description).getChance(service);
				}
			}
		}
		
		return totalValue;
	}
	
	/**
	 * Calculate the accumulated value of the approximated failure rate for a given service combination.
	 * @param combination the given service combination without the rating and rating type
	 * @param knowledge the given knowledge
	 * @return the total value
	 */
	public double getTotalApproximatedFailureRateValue(Map<Description, WeightedCollection<ServiceDescription>> combination, Knowledge knowledge) {
		
		failureRates = new HashMap<>();
		double totalValue = 0;
		
		// Get workflow service tree from knowledge
		StaticTree<Description> tree = knowledge.getWorkflowServiceTree();
		Map<String, Integer> serviceLoads = getServiceLoads(combination, knowledge);
		
		// Initialize simple failure rates per description
		for (Description description : combination.keySet()) {	
			setSimpleServiceFailureRate(combination, description, knowledge, serviceLoads);
		}
		
		// Calculate full failure rate
		for (Description description : combination.keySet()) {	
			double subValue = failureRates.get(description) * knowledge.getServiceUsageChance(description);
			List<Description> workflowServiceFlow = tree.findNodePath(description);
			
			for (Description desc : workflowServiceFlow) {
				
				if (!description.equals(desc)) {
					subValue *= 1 - failureRates.get(desc);
				}
			}
			
			totalValue += subValue;
		}
		
		return totalValue;
	}
	
	/**
	 * Calculate and set the simple service failure rate for a given description (= service type & operation name) with a given service combination,
	 * description and knowledge. This failure rate does not take the service flow of the workflow into account. The full accurate failure rate 
	 * is calculated above. 
	 * @param combination the given service combination
	 * @param description the given description
	 * @param Knowledge the given knowledge
	 * @param serviceLoads loads of the services
	 * @throws IllegalStateException throws when the failure rate map already contains description data
	 */
	private void setSimpleServiceFailureRate(Map<Description, WeightedCollection<ServiceDescription>> combination, Description description, 
			Knowledge knowledge, Map<String, Integer> serviceLoads) throws IllegalStateException {
		
		if (failureRates.containsKey(description)) {
			throw new IllegalStateException("The failure rate map already contains description data!");
		}
		
		double totalValue = 0;
		
		for (ServiceDescription service : combination.get(description).getItems()) {
			if (service.getCustomProperties().containsKey("FailureRate")) {
				
				// The failure rate (not taking services that activate this service into account) is calculated as follows:
				//
				// failure rate = chance that this service for this service type is used 
				//            * approximated fail rate
				double useChance = combination.get(description).getChance(service);	
				//System.out.println("KNOWLEDGE VALUE of " + service.getServiceEndpoint() + ", " + knowledge.getApproximatedServiceFailureRate(service.getServiceEndpoint(), serviceLoadsInt.get(service.getServiceEndpoint())) + " " + serviceLoadsInt.get(service.getServiceEndpoint()));
				totalValue += useChance * knowledge.getApproximatedServiceFailureRate(service.getServiceEndpoint(), serviceLoads.get(service.getServiceEndpoint()));
			}
		}
		
		failureRates.put(description, totalValue);
	}
	
	/**
	 * Calculate the service loads for each service in a given service combination with a given knowledge
	 * @param combination the given service combination
	 * @param Knowledge the given knowledge
	 * @return the calculated service loads map
	 */
	private Map<String, Integer> getServiceLoads(Map<Description, WeightedCollection<ServiceDescription>> combination, Knowledge knowledge) {
		
		Map<String, Double> serviceLoads = new HashMap<String, Double>();
		Map<String, Integer> serviceLoadsInt = new HashMap<String, Integer>();
		
		for (Description descr : combination.keySet()) {
			WeightedCollection<ServiceDescription> serviceUsage = combination.get(descr);
			
			for (ServiceDescription service : serviceUsage.getItems()) {
				double serviceLoad = knowledge.getServiceDescriptionLoad(descr, serviceUsage.getChance(service));
				serviceLoads.compute(service.getServiceEndpoint(), (k, v) -> (v == null) ? serviceLoad : v + serviceLoad);		
			}
		}
		
		for (Map.Entry<String, Double> entry : serviceLoads.entrySet()) {
			//System.err.println("LOAD " + entry.getKey() + ": " + (int) Math.ceil(entry.getValue()));
			serviceLoadsInt.put(entry.getKey(), (int) Math.ceil(entry.getValue()));
		}
		
		return serviceLoadsInt;
	}
	
	/**
	 * Calculate the accumulated value of the approximated failure rate for a given service combination and extra property values
	 * @param combination the given service combination
	 * @param values the given values map
	 * @param knowledge the given knowledge
	 * @return the total value
	 */
	public double getTotalApproximatedFailureRateValue(ServiceCombination combination, Map<String, Integer> values, Knowledge knowledge) {
		
		failureRates = new HashMap<>();
		double totalValue = 0;
		
		// Get workflow service tree from knowledge
		StaticTree<Description> tree = knowledge.getWorkflowServiceTree();
		Map<String, Integer> serviceLoads = getServiceLoads(combination, knowledge, values);
		
		// Initialize simple failure rates per description
		for (Description description : combination.getDescriptions()) {	
			setSimpleServiceFailureRate(combination, description, knowledge, values, serviceLoads);
		}
		
		// Calculate full failure rate
		for (Description description : combination.getDescriptions()) {	
			double subValue = failureRates.get(description) * knowledge.getServiceUsageChance(description);
			List<Description> workflowServiceFlow = tree.findNodePath(description);
			
			for (Description desc : workflowServiceFlow) {
				
				if (!description.equals(desc)) {
					subValue *= 1 - failureRates.get(desc);
				}
			}
			
			totalValue += subValue;
		}
		
		return totalValue;
	}
	
	/**
	 * Calculate and set the simple service failure rate for a given description (= service type & operation name) with a given service combination,
	 * description, knowledge and given service loads. This failure rate does not take the service flow of the workflow into account. 
	 * The full accurate failure rate is calculated above. 
	 * @param combination the given service combination
	 * @param description the given description
	 * @param Knowledge the given knowledge
	 * @param loads the given service loads
	 * @param serviceLoads loads of the services
	 * @throws IllegalStateException throws when the failure rate map already contains description data
	 */
	private void setSimpleServiceFailureRate(ServiceCombination combination, Description description, 
			Knowledge knowledge, Map<String, Integer> loads, Map<String, Integer> serviceLoads) throws IllegalStateException {
		
		if (failureRates.containsKey(description)) {
			throw new IllegalStateException("The failure rate map already contains description data!");
		}
		
		double totalValue = 0;
		
		for (ServiceDescription service : combination.getAllServices(description).getItems()) {
			
			if (service.getCustomProperties().containsKey("FailureRate")) {	
				// The failure rate (not taking services that activate this service in count) is calculated as follows:
				//
				// failure rate = chance that this service for this service type is used 
				//            * approximated fail rate
				double useChance = combination.getAllServices(description).getChance(service);
				totalValue += useChance * knowledge.getApproximatedServiceFailureRate(service.getServiceEndpoint(), serviceLoads.get(service.getServiceEndpoint()));
			}
		}
		
		failureRates.put(description, totalValue);
	}
	
	/**
	 * Calculate the service loads for each service in a given service combination with a given knowledge and additional service loads
	 * @param serviceCombination the given service combination 
	 * @param Knowledge the given knowledge
	 * @param loads the given service loads
	 * @return the calculated service loads map
	 */
	private Map<String, Integer> getServiceLoads(ServiceCombination serviceCombination, Knowledge knowledge, Map<String, Integer> loads) {
		
		Map<String, Double> serviceLoads = new HashMap<String, Double>();
		Map<String, Integer> serviceLoadsInt = new HashMap<String, Integer>();
		
		for (Description description : serviceCombination.getDescriptions()) {			
			WeightedCollection<ServiceDescription> serviceUsage = serviceCombination.getAllServices(description);
			
			for (ServiceDescription service : serviceUsage.getItems()) {
				double load = knowledge.getServiceDescriptionLoad(description, serviceUsage.getChance(service));
				
				if (loads.containsKey(service.getServiceEndpoint())) {
					load += loads.get(service.getServiceEndpoint());
				}
				
				double serviceLoad = load;
				
				serviceLoads.compute(service.getServiceEndpoint(), (k, v) -> (v == null) ? serviceLoad : v + serviceLoad);		
			}
		}
		
		for (Map.Entry<String, Double> entry : serviceLoads.entrySet()) {
			//System.err.println("LOAD " + entry.getKey() + ": " + (int) Math.ceil(entry.getValue()));
			serviceLoadsInt.put(entry.getKey(), (int) Math.ceil(entry.getValue()));
		}
		
		return serviceLoadsInt;
	}
	
	/**
	 * Choose the service combinations for a requirement with a given combination limit, 
	 * rating type, knowledge and service combinations without rating or type
	 * @param combinationLimit the given limit of returned service combinations
	 * @param ratingType the given rating type
	 * @param knowledge the given knowledge
	 * @param allServiceCombinations the given generated service combinations without rating or type
	 * @return a list of the chosen service combinations
	 */
	protected abstract List<ServiceCombination> getServiceCombinations(int combinationLimit, RatingType ratingType, 
			Knowledge knowledge, List<Map<Description, WeightedCollection<ServiceDescription>>> allServiceCombinations);
	
	/**
	 * Calculate the number rating of a service combination with a given value.
	 * The given value is an accumulation of property values where lower means better score.
	 * @param totalValue the given accumulated property value
	 * @return the service combination rating
	 */
	public static double GetNumberRatingDouble(double totalValue) {
		
		// Return score. +1 excludes special cases where the total value is between 0 and 1
		return 1 / (totalValue + 1) * 100;
	}
	
	/**
	 * Calculate the class rating of a service combination with a given list of goals, property and a value.
	 * The given value is an accumulation of property values where lower means better score.
	 * The rating is a number representing the class the service combination is in. The number represents the amount of met goals.
	 * This class rating is calculated by checking all goals, finding the relevant goal and checking that goal with the given value.
	 * @param totalValue the given accumulated property value
	 * @param goals the given system goals
	 * @param property the given name of the custom service property
	 * @return the service combination rating
	 */
	public static int getClassRating(List<Goal> goals, double totalValue, String property) throws IllegalStateException {
		
		for (Goal goal : goals) {
			
			if (goal.getType() == GoalType.fromString(property)) {
				
				if (goal.validValue(totalValue)) {
					//System.out.println("value " + totalValue);
					return 1;
				}
				else {
					//System.out.println("value " + totalValue);
					return 0;
				}
			}
		}
		
		// No goal set
		return 1;
	}
	
	/**
	 * Return a sorted service combination list with a maximum amount of service combinations and a given rating type 
	 * based on a list of combination scores and a list of all service combinations
	 * @param combinationLimit the given maximum amount of service combinations
	 * @param ratingType the given rating type
	 * @param combinationScores the given list of combination scores
	 * @param serviceCombinations the given list of all service combinations
	 * @param propertyValues list of the combination property values
	 * @return a list of sorted service combinations
	 */
	protected List<ServiceCombination> getSortedServiceCombinations(int combinationLimit, RatingType ratingType, 
			List<Comparable<?>> combinationScores, List<Map<Description, WeightedCollection<ServiceDescription>>> serviceCombinations, HashMap<String, List<Double>> propertyValues) {
		
		List<ServiceCombination> chosenServicesList = new ArrayList<>();
		ServiceCombination chosenServicesEntry;
		Map<Description, WeightedCollection<ServiceDescription>> chosenServicesCombination;
		
		for (int i = 0; i < serviceCombinations.size(); i++) {
			chosenServicesCombination = serviceCombinations.get(i);
			chosenServicesEntry = new ServiceCombination(chosenServicesCombination, ratingType, combinationScores.get(i));
			chosenServicesList.add(chosenServicesEntry);
			
			for (String property : propertyValues.keySet()) {
				chosenServicesEntry.addProperty(property, propertyValues.get(property).get(i));
			}
		}
		
		Collections.sort(chosenServicesList, Collections.reverseOrder());
		return chosenServicesList.subList(0, Math.min(combinationLimit, serviceCombinations.size()));
	}
	
	/**
	 * Return a sorted service combination list by generating a new list of service combinations and
	 * rating them using the given new combination scores. 
	 * @param serviceCombinations the given service combinations
	 * @param combinationScores the given list of combination scores
	 * @param propertyValues list of the combination property values
	 * @return a list of sorted service combinations
	 */
	protected List<ServiceCombination> getSortedServiceCombinations(List<ServiceCombination> serviceCombinations, 
			List<Comparable<?>> combinationScores, HashMap<String, List<Double>> propertyValues) {
		
		List<ServiceCombination> chosenServicesList = new ArrayList<>();
		
		for (int i = 0; i < serviceCombinations.size(); i++) {
			ServiceCombination newCombination = serviceCombinations.get(i).GetCloneNewRating(combinationScores.get(i));
			chosenServicesList.add(newCombination);
			
			for (String property : propertyValues.keySet()) {
				newCombination.addProperty(property, propertyValues.get(property).get(i));
			}
		}
		
		Collections.sort(chosenServicesList, Collections.reverseOrder());
		return chosenServicesList;
	}
	
	/**
	 * Return all possible service collection combinations from a given list of usable services and service descriptions.
	 * @param usableServicesCollection the given list of usable services
	 * @param descriptionList the given list of usable service descriptions
	 * @return a list of maps consisting of every possible service collection combination for each service description 
	 */
	private List<Map<Description, WeightedCollection<ServiceDescription>>> getAllCollectionCombinations(
			List<List<WeightedCollection<ServiceDescription>>> usableServicesCollection, List<Description> descriptionList) {
		
		List<Map<Description, WeightedCollection<ServiceDescription>>> combinations = new ArrayList<>();
		int listSize = usableServicesCollection.size();
		int[] indices = new int[listSize];
		
		while (true) {
			
	        // Add current combination 
			Map<Description, WeightedCollection<ServiceDescription>> currentCombination = new HashMap<>();
			
	        for (int i = 0; i < listSize; i++)
	        	currentCombination.put(descriptionList.get(i), usableServicesCollection.get(i).get(indices[i]));
	        
	        combinations.add(currentCombination);
	  
	        // find the rightmost array that has more 
	        // elements left after the current element  
	        // in that array 
	        int next = listSize - 1; 
	        
	        while (next >= 0 && (indices[next] + 1 >= usableServicesCollection.get(next).size()))
	            next--; 
	  
	        // No more combinations are left
	        if (next < 0) 
	            break; 
	  
	        // Move to next element in that array 
	        indices[next]++;
	  
	        // Reset other arrays to the right of current array
	        for (int i = next + 1; i < listSize; i++) 
	            indices[i] = 0;
		}
		
		return combinations;
	}
	
	/**
	 * Recursively generate and return a list of service description arrays representing all possible n-services-per-description 
	 * service combinations with equal usage chances.
	 * @param serviceDescriptions the given list of service descriptions
	 * @param combinationSize the given amount of used services per description
	 * @param startPos parameter used to hold the current starting position in the array
	 * @param currentCombination parameter used to hold the current combination
	 * @param result parameter used to hold the result
	 * @return the generated list of arrays representing the service combinations without usage chance
	 */
	private List<ServiceDescription[]> getAllMultiListCombinations(List<ServiceDescription> serviceDescriptions, int combinationSize, 
			int startPos, ServiceDescription[] currentCombination, List<ServiceDescription[]> result) {
		
		if (combinationSize == 0) {
			result.add(currentCombination.clone());
	        return null;
		}
		
        for (int i = startPos; i <= serviceDescriptions.size() - combinationSize; i++) {
            currentCombination[currentCombination.length - combinationSize] = serviceDescriptions.get(i);
            getAllMultiListCombinations(serviceDescriptions, combinationSize - 1, i + 1, currentCombination, result);
        }
        
        return result;
	} 
	
	/**
	 * Generate and return a list of weighted collections representing all possible n-services-per-description 
	 * service combinations with equal usage chances.
	 * @param serviceDescriptions the given list of service descriptions
	 * @param combinationSize the given amount of used services per description
	 * @return the generated weighted collections representing the service combinations with usage chance
	 */
	private List<WeightedCollection<ServiceDescription>> getAllMultiCollectionCombinationsEqualChances(List<ServiceDescription> serviceDescriptions,
			int combinationSize) {
		
		List<WeightedCollection<ServiceDescription>> collections = new ArrayList<>();
		WeightedCollection<ServiceDescription> collection;
		List<ServiceDescription[]> multiListCombinations = 
				getAllMultiListCombinations(serviceDescriptions, combinationSize, 0, new ServiceDescription[combinationSize], new ArrayList<>());
		
		
		for (ServiceDescription[] multiList : multiListCombinations) {
			collection = new WeightedCollection<ServiceDescription>();
			
			for (ServiceDescription service : multiList)
				collection.add(service, 100);
			
			collections.add(collection);
		}
		
		return collections;
	}
}
