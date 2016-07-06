/**
 * 
 */
package practices.microservice.registry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.discovery.InstanceMetadata;
import practices.microservice.discovery.RegisterEntry;

/**
 * 
 * @author bwang
 *
 */
public abstract class AbstractServiceRegistry implements ServiceRegistry<InstanceMetadata> {

	
	protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private ServerIdentifier identifier;

	protected AbstractServiceRegistry(ServerIdentifier identifier) {
		this.identifier = identifier;
	}


	@Override
	public ServerIdentifier getIdentifier() {
		return this.identifier;
	}

	@SuppressWarnings("null")
	protected void validateFileds(RegisterEntry entry) {
		checkArgument(entry == null, "Param cannot be null!");
		checkNotNull(entry.getServiceType(), "Property 'serviceType' cannot be null!");
		checkNotNull(entry.getServiceName(), "Property 'serviceName' cannot be null!");
		checkNotNull(entry.getServiceContract(), "Property 'serviceContract' cannot be null!");
		checkNotNull(entry.getInstanceMetadata(), "Property 'instanceMetadatum' cannot be nulls!");
	}

	protected String buildBasePath()
	{
		return '/'+this.getIdentifier().getServiceType().getValue()+'/'+ServerIdentifier.BASE_KEY;
	}	
}
