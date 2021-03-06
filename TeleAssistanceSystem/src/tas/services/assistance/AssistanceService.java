package tas.services.assistance;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import service.auxiliary.CompositeServiceConfiguration;
import service.auxiliary.LocalOperation;
import service.composite.CompositeService;
import service.registry.ServiceRegistry;

@CompositeServiceConfiguration(
			SDCacheMode = true
			//,Timeout = 1
			//,MaxRetryAttempts = 3
)

public class AssistanceService extends CompositeService {

	public AssistanceService(String serviceName, String serviceEndpoint,
			String workflowFilePath, List<ServiceRegistry> registries) {
		super(serviceName, serviceEndpoint, workflowFilePath, registries);
	}

	@LocalOperation
	public int pickTask() {
	    
		System.out.println("Pick (1) to measure vital parameters, (2) for emergency and (3) to stop service.");		
		Scanner in = new Scanner(System.in);
		
		do {
			
			String line = in.nextLine();
			Integer pick = Integer.parseInt(line);
			
			if (pick < 1 || pick > 3) {
				
				System.err.println("Wrong value:" + pick);
				
			} else {
				
				in.close();
				return pick;
				
			}
		} while (true);
	}
	
	@LocalOperation
	public HashMap<Object, Object> getVitalParameters(){
		return new HashMap<Object, Object>();
	}
}
