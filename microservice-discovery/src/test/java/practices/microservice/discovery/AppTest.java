package practices.microservice.discovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.registry.InstanceMetadata;
import practices.microservice.registry.RegisterEntry;
import practices.microservice.registry.RegisterEntry.ServiceType;
import practices.microservice.registry.RegistryFactory;
import practices.microservice.registry.ServiceRegistry;
import practices.microservice.registry.zookeeper.ZkRegistryFactory;

import com.google.common.net.HostAndPort;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
	private static Logger log = LoggerFactory.getLogger(AppTest.class);
    private static final String     PATH = "/discovery/example";

	ServerIdentifier identifier = null;
	CuratorFramework client = null;
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
    	
    	
    	HostAndPort hostAndPort = HostAndPort.fromString("127.0.0.1:2181");
    	ArrayList<HostAndPort> list = new ArrayList<HostAndPort>();
    	list.add(hostAndPort);
    	
    	identifier = new ServerIdentifier("RPC-Test", "/"+ServiceType.RPC.getValue()+"/Greeter", list);
    	
		client = CuratorFrameworkFactory.newClient(identifier
				.getConnectionString(), new ExponentialBackoffRetry(1000, 3));
		client.start();
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
    	CloseableUtils.closeQuietly(client);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     * @throws Exception 
     */
    public void testApp() throws Exception
    {
    	RegistryFactory<InstanceMetadata> factory = new ZkRegistryFactory();

    
		ServiceRegistry<InstanceMetadata> registry = factory.getServiceRegistry(identifier);
		registry.start();
		
		InstanceMetadata metaData = new InstanceMetadata(UUID.randomUUID(), "RD", 2184, true);
		RegisterEntry entry = new RegisterEntry();
		entry.setServiceType(ServiceType.RPC);
		entry.setServiceName("Greeter");
		entry.setServiceContract("com.test.practices.GreeterSub");
		entry.setInstanceMetadata(metaData);
		
		registry.register(entry);
		
		//List<InstanceMetadata> instanceList = registry.listAll("com.test.practices.GreeterSub");
		
		long times = System.currentTimeMillis();
		
		InstanceMetadata newmetadata = registry.getInstanceById("com.test.practices.GreeterSub", metaData.getId().toString());
		
		times = System.currentTimeMillis() - times;
		log.info("Get instance in {} ms", times);
		
		times = System.currentTimeMillis();
		
	    newmetadata = registry.getInstanceById("com.test.practices.GreeterSub", metaData.getId().toString());
		
		times = System.currentTimeMillis() - times;
		log.info("Get instance in {} ms", times);
		
		assertTrue(newmetadata.equals(metaData));
        
        registry.unregisterService(entry);
        
       // registry.close();
        //registry.destroy();
    }
    
    public void testDiscovery() throws Exception
    {
//    	ExampleServer server = new ExampleServer(client, PATH, "Greeter", "description");
//    	server.start();
//    	
//    	byte[] data = client.getData().forPath(PATH+"/Greeter");
//    	JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<InstanceDetails>(InstanceDetails.class);
//    	ServiceInstance<InstanceDetails> obj = serializer.deserialize(data);
//    	System.out.println("Data:"+obj.getPayload().getDescription());
//    	
//    	server.close();
    }
}
