package practices.microservice.discovery;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.common.ServerIdentifier.ServiceType;
import practices.microservice.discovery.zookeeper.ZkDiscoveryFactory;
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
 //   private static final String     PATH = "/discovery/example";

	ServerIdentifier identifier = null;
//	CuratorFramework client = null;
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
    	
    	
    	HostAndPort hostAndPort1 = HostAndPort.fromString("localhost:2181");
    	HostAndPort hostAndPort2 = HostAndPort.fromString("localhost:2182");
    	HostAndPort hostAndPort3 = HostAndPort.fromString("localhost:2183");
    	ArrayList<HostAndPort> list = new ArrayList<HostAndPort>();
    	list.add(hostAndPort1);
    	list.add(hostAndPort2);
    	list.add(hostAndPort3);
    	identifier = new ServerIdentifier(ServiceType.RPC,list);
    	
//		client = CuratorFrameworkFactory.newClient(identifierr
//				.getConnectionString(), new ExponentialBackoffRetry(1000, 3));
//		client.start();
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
    	//CloseableUtils.closeQuietly(client);
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
    	RegistryFactory<ServiceRegistry<InstanceMetadata>> factory = new ZkRegistryFactory();
		ServiceRegistry<InstanceMetadata> registry = factory.getServiceRegistry(identifier);
		registry.start();
		
		DiscoveryFactory<ServiceDiscovery<InstanceMetadata>> factory2 = new ZkDiscoveryFactory();
		ServiceDiscovery<InstanceMetadata> discovery = factory2.getServiceDiscovery(identifier);
		discovery.start();
		
		
		InstanceMetadata metaData = new InstanceMetadata(UUID.randomUUID(), "localhost", 2184, true);
		RegisterEntry entry = new RegisterEntry();
		entry.setServiceType(ServiceType.RPC);
		entry.setServiceName(ServerIdentifier.BASE_KEY);
		entry.setServiceContract("com.test.practices.GreeterSub");
		entry.setInstanceMetadata(metaData);
		
		registry.register(entry);
		long times = System.currentTimeMillis();	
		InstanceMetadata instance1 = discovery.getInstanceById("com.test.practices.GreeterSub", metaData.getId().toString());
		times = System.currentTimeMillis() - times;
		log.info("Get instance at the fisrt time in {} ms", times);
	    times = System.currentTimeMillis();	
		InstanceMetadata instance2 = discovery.getInstanceById("com.test.practices.GreeterSub", metaData.getId().toString());
		times = System.currentTimeMillis() - times;
		log.info("Get instance at the second time in {} ms", times);
		
		registry.unregisterService(entry);
		
		assertTrue(instance1.getPort() == instance2.getPort());
		//		
//		InstanceMetadata newmetadata = registry.getInstanceById(ServiceType.RPC, "Greeter" , "com.test.practices.GreeterSub", metaData.getId().toString());
//        assertTrue(newmetadata.equals(metaData));
//        
        registry.destroy();
    }
    
    public void testDiscovery() throws Exception
    {
    	//ExampleServer server = new ExampleServer(client, PATH, "Greeter", "description");
    	//server.start();
//    	
//    	byte[] data = client.getData().forPath("/rpc/Greeter");
//    	JsonInstanceSerializer<InstanceMetadata> serializer = new JsonInstanceSerializer<InstanceMetadata>(InstanceMetadata.class);
//    	ServiceInstance<InstanceMetadata> obj = serializer.deserialize(data);
//    	System.out.println("Data:"+obj.getPayload().toString());
    	
    	//server.close();
    	Set<String> ips = getIp();
    	ips.iterator();
    }
    public Set<String> getIp(){
        Set<String> set = new HashSet<String>();
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
                        set.add(inetAddress.getHostAddress().toString());
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return set;
    }
}
