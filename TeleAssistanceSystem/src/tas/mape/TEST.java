package tas.mape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import service.adaptation.effectors.WorkflowEffector;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import service.registry.ServiceRegistry;
import tas.mape.analyzer.MinCostReq;
import tas.mape.communication.ComponentMessageHost;
import tas.mape.communication.PlannerMessage;
import tas.mape.executer.Executer;
import tas.mape.knowledge.WorkflowAnalyzer;
import tas.mape.planner.PlanComponent;
import tas.mape.planner.PlanComponentType;
import tas.mape.planner.Planner;
import tas.mape.planner.ServiceCombination;
import tas.mape.planner.RatingType;
import tas.services.alarm.AlarmService;
import tas.services.assistance.AssistanceService;
import tas.services.assistance.AssistanceServiceCostProbe;
import tas.services.drug.DrugService;
import tas.services.medical.MedicalAnalysisService;
import tas.services.qos.MinCostQoS;
import tas.services.qos.PreferencesQoS;
import tas.services.qos.ReliabilityQoS;

public class TEST {
	
    protected static ServiceRegistry serviceRegistry, serviceRegistry2;
    protected static AssistanceService assistanceService;
    protected static AssistanceServiceCostProbe monitor;
    protected static WorkflowEffector workflowEffector;

    protected static AlarmService alarm1;
    protected static AlarmService alarm2;
    protected static AlarmService alarm3;
    
    protected static DrugService drugService;

    protected static MedicalAnalysisService medicalAnalysis1;
    protected static MedicalAnalysisService medicalAnalysis2;
    protected static MedicalAnalysisService medicalAnalysis3;
    
    private static List<ServiceRegistry> serviceRegistries;
	
	public static void main(String[] args) {	
		init();
	}
	
	public static void init() {
		serviceRegistry = new ServiceRegistry("ServiceRegistry", "se.lnu.service.registry");
		serviceRegistry.startService();
		
		serviceRegistry2 = new ServiceRegistry("ServiceRegistry2", "se.lnu.service.registry2");
		serviceRegistry2.startService();
		
		serviceRegistries = new ArrayList<ServiceRegistry>();
		serviceRegistries.add(serviceRegistry);
		serviceRegistries.add(serviceRegistry2);

		// Alarm Services
		alarm1 = new AlarmService("AlarmService1", "service.alarmService1");
		alarm1.getServiceDescription().getCustomProperties().put("Cost", 4.0);
		alarm1.getServiceDescription().setOperationCost("triggerAlarm", 4.0);
		alarm1.getServiceDescription().getCustomProperties().put("FailureRate", 0.11);
		alarm1.startService();
		alarm1.register(serviceRegistry2);

		alarm2 = new AlarmService("AlarmService2", "service.alarmService2");
		alarm2.getServiceDescription().getCustomProperties().put("Cost", 12.0);
		alarm2.getServiceDescription().getCustomProperties().put("preferred", true);
		alarm2.getServiceDescription().setOperationCost("triggerAlarm", 12.0);
		alarm2.getServiceDescription().getCustomProperties().put("FailureRate", 0.04);
		//alarm2.addServiceProfile(new ServiceFailureProfile(0.04));
		alarm2.startService();
		alarm2.register(serviceRegistry);

		alarm3 = new AlarmService("AlarmService3", "service.alarmService3");
		alarm3.getServiceDescription().getCustomProperties().put("Cost", 2.0);
		alarm3.getServiceDescription().setOperationCost("triggerAlarm", 2.0);
		alarm3.getServiceDescription().getCustomProperties().put("FailureRate", 0.18);
		//alarm3.addServiceProfile(new ServiceFailureProfile(0.18));
		alarm3.startService();
		alarm3.register(serviceRegistry);

		// Medical Analysis Services
		medicalAnalysis1 = new MedicalAnalysisService("MedicalService1", "service.medical1");
		medicalAnalysis1.getServiceDescription().getCustomProperties().put("preferred", false);
		medicalAnalysis1.getServiceDescription().getCustomProperties().put("Cost", 4.0);
		medicalAnalysis1.getServiceDescription().setOperationCost("analyzeData", 4.0);
		medicalAnalysis1.getServiceDescription().getCustomProperties().put("FailureRate", 0.12);
		//medicalAnalysis1.addServiceProfile(new ServiceFailureProfile(0.12));
		//medicalAnalysis1.addServiceProfile(new ServiceFailureProfile(0.12));
		medicalAnalysis1.startService();
		medicalAnalysis1.register(serviceRegistry);

		medicalAnalysis2 = new MedicalAnalysisService("MedicalService2", "service.medical2");
		medicalAnalysis2.getServiceDescription().getCustomProperties().put("preferred", true);
		medicalAnalysis2.getServiceDescription().getCustomProperties().put("Cost", 14.0);
		medicalAnalysis2.getServiceDescription().setOperationCost("analyzeData", 14.0);
		medicalAnalysis2.getServiceDescription().getCustomProperties().put("FailureRate", 0.07);
		medicalAnalysis2.startService();
		medicalAnalysis2.register(serviceRegistry);

		medicalAnalysis3 = new MedicalAnalysisService("MedicalService3", "service.medical3");
		medicalAnalysis3.getServiceDescription().getCustomProperties().put("preferred", false);
		medicalAnalysis3.getServiceDescription().getCustomProperties().put("Cost", 2.15);
		medicalAnalysis3.getServiceDescription().setOperationCost("analyzeData", 2.15);
		medicalAnalysis3.getServiceDescription().getCustomProperties().put("FailureRate", 0.18);
		medicalAnalysis3.startService();
		medicalAnalysis3.register(serviceRegistry);

		// Drug Services
		drugService = new DrugService("DrugService", "service.drug");
		drugService.getServiceDescription().getCustomProperties().put("preferred", true);
		drugService.getServiceDescription().getCustomProperties().put("Cost", 2.0);
		drugService.getServiceDescription().setOperationCost("changeDoses", 5.0);
		drugService.getServiceDescription().setOperationCost("changeDrug", 5.0);
		//drugService.getServiceDescription().getCustomProperties().put("FailureRate", 0.01);
		drugService.startService();
		drugService.register(serviceRegistry);

		// Assistance Service. Workflow is provided by TAS_gui through executeWorkflow method
		assistanceService = new AssistanceService("TeleAssistanceService", "service.assistance", "resources/TeleAssistanceWorkflow.txt", Arrays.asList(serviceRegistry, serviceRegistry2));
		assistanceService.startService();
		//assistanceService.register(serviceRegistry);
		monitor = new AssistanceServiceCostProbe();
		assistanceService.getCostProbe().register(monitor);
		assistanceService.getWorkflowProbe().register(monitor);
		//assistanceService.getWorkflowProbe().register(new AssistanceServiceDelayProbe());
		// assistanceService.getServiceInvocationProbe().register(monitor);
		assistanceService.addQosRequirement("ReliabilityQoS", new ReliabilityQoS());
		assistanceService.addQosRequirement("PreferencesQoS", new PreferencesQoS());
		assistanceService.addQosRequirement("CostQoS", new MinCostQoS());
		
		workflowEffector = new WorkflowEffector(assistanceService);
		
		
		// TESTING CODE
		assistanceService.updateCache();
		
		for (String service : assistanceService.getCache().getServiceDescriptions().keySet()) {
			System.out.print(service + " \n");
		}
		
		//System.out.print(assistanceService.getCache().getServiceWithEndpoint("service.medical3").getServiceName() + " \n");
		
		
		List<PlanComponent> plan = new ArrayList<PlanComponent>();
		PlanComponentType planType1 = PlanComponentType.INCREASE_LOAD;
		PlanComponent planComp1 = new PlanComponent(planType1, "service.drug", 2);
		plan.add(planComp1);
		
		Executer executer = new Executer();
		executer.initializeEffectors(assistanceService);
		executer.execute(plan);
		
		
		WeightedCollection<String> items = new WeightedCollection<String>();
		items.add("50% chance", 50);
		items.add("25% chance", 25);
		items.add("25% chance 2", 25);
		
		Map<String, Integer> kansenTest = new HashMap<String, Integer>();
		
		
		for (int i = 0; i < 10000; i++) {
			String itemName = items.next();
			int newValue = 0;
			
			if (kansenTest.get(itemName) != null) {
				newValue = kansenTest.get(itemName) + 1;
			}
			
			kansenTest.put(itemName, newValue);
		}
		
		for (String s : kansenTest.keySet()) {
			System.err.print(s + " " + kansenTest.get(s) + " \n");
		}
		
		items.remove("50% chance");
		System.err.print("----------------------------------------------\n");
		
		items.increaseWeight("25% chance", 1);
		for (Integer s : items.getWeights()) {
			System.err.print(s + "\n");
		}
		
		System.err.print("----------------------------------------------\n");
		System.err.print(items.getChance("25% chance") + "\n");
		
		
		@SuppressWarnings("unused")
		MinCostReq mcr = new MinCostReq();
		
		// ANALYZER TEST
		assistanceService.getCache().getServiceWithEndpoint("service.alarmService3");
		List<ServiceCombination> services = mcr.applyStrategy(new Pair<Integer, Integer>(1, 1), 10, RatingType.NUMBER, null, assistanceService.getCache().caches);
		//List<Map<Description, ServiceDescription>> services2 = mcr.getAllServiceCombinations(assistanceService.getCache().caches);
		
		for (ServiceCombination comb : services) {
			System.err.print("-----------------------------------------------------------------------------------------\n");
			
			comb.getDescriptions();
			
			for (Description d : comb.getDescriptions()) {
				
				System.err.print("["+ d.toString() + "] : " + comb.getAllServices(d).getItems().get(0).getServiceEndpoint() + "\n");
				
			}
			
			System.err.print("-------------------------------------------------------------------------------------------\n");
			System.err.print("cost: " + comb.getRating() + " \n");
		}
		
		/*for (Map<Description, ServiceDescription> map : services2) {
			System.err.print("-----------------------------------------------------------------------------------------\n");
			
			for (Description d : map.keySet()) {
				
				System.err.print("["+ d.toString() + "] : " + map.get(d).getServiceEndpoint() + "\n");
				
			}
			
			System.err.print("-------------------------------------------------------------------------------------------\n");
		}*/
		
		/*Planner p1 = new Planner("mape.planner1", executer);
		Planner p2 = new Planner("mape.planner2", executer);
		ComponentMessageHost host = new ComponentMessageHost();
		host.register(p1);
		host.register(p2);
		System.err.print("host size " + host.getListeners().size() + " \n");
		//p2.setEndpoint("mape.planner2new");
		p1.sendMessage(new PlannerMessage(1, p2.getEndpoint(), p1.getEndpoint(), "test", null, null));
		
		
		ServiceCombination combination = new ServiceCombination(null, RatingType.NUMBER, 1.0);

		
		System.err.print("------------------------------------------------------------ \n");
		System.err.print("test: " + combination.getRating().getClass() + "\n");
		
		
		WorkflowAnalyzer wpi = new WorkflowAnalyzer();
		Map<Description, Pair<List<ServiceDescription>, Double>> test = wpi.analyzeWorkflow(5000, "resources/TeleAssistanceWorkflow.txt", "resources/files/PreferredQoS.xml", assistanceService);
		
		for (Description d : test.keySet()) {
			System.err.print("------------------------------------------------------------ \n");
			
			for (ServiceDescription sd : test.get(d).getKey()) {
				System.err.print("service name: " + sd.getServiceName() + " \n");
			}
			
			System.err.print("=== \n");
			System.err.print("description usage chance :" + test.get(d).getValue() + " \n");
			System.err.print("------------------------------------------------------------ \n");
		}*/
	}
}
