/**
 * 
 */
package practices.microservice.registry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.ServerIdentifier;

/**
 * @author bwang
 *
 */
public abstract class AbstractServiceRegistry<T> implements ServiceRegistry<T> {
	
	protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	protected static final String PATH = "/discovery/example";
	
	private ServerIdentifier identifier;
	
	protected AbstractServiceRegistry (ServerIdentifier identifier){
		this.identifier = identifier;
	}
	@Override
	public ServerIdentifier getIdentifier(){
		return this.identifier;
	}

	@SuppressWarnings("null")
	protected void validateFileds(RegisterEntry entry){
		checkArgument(entry == null, "Param cannot be null!");
		checkNotNull(entry.getServiceType(), "Property 'serviceType' cannot be null!");
		checkNotNull(entry.getServiceName(), "Property 'serviceName' cannot be null!");
		checkNotNull(entry.getServiceContract(), "Property 'serviceContract' cannot be null!");
		checkNotNull(entry.getInstanceMetadata(), "Property 'instanceMetadatum' cannot be nulls!");
	}

}
