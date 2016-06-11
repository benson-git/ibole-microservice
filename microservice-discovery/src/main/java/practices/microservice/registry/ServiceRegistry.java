package practices.microservice.registry;

import java.util.List;
/**
 * 
 * @author bwang
 *
 */
public interface ServiceRegistry {

	/**
	 * Services are loaded by Lazy load manner.
	 */
	public void loadService();
	
	public void addService(RegisterEntry entry);
	
	public List<RegisterEntry> getServiceList();
}
