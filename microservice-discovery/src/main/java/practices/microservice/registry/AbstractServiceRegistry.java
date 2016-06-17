/**
 * 
 */
package practices.microservice.registry;

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


}
