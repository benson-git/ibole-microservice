package practices.microservice.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.ServerIdentifier;

public abstract class AbstractServiceDiscovery implements ServiceDiscovery<InstanceMetadata> {
	
	protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private ServerIdentifier identifier;

	protected AbstractServiceDiscovery (ServerIdentifier identifier){
		this.identifier = identifier;
	}

	@Override
	public ServerIdentifier getIdentifier() {
		return this.identifier;
	}

	protected String buildBasePath()
	{
		return '/'+this.getIdentifier().getServiceType().getValue()+'/'+ServerIdentifier.BASE_KEY;
	}
}
