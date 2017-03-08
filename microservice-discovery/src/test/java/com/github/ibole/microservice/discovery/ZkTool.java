package com.github.ibole.microservice.discovery;

import com.google.common.net.HostAndPort;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.discovery.zookeeper.ZkDiscoveryFactory;
import com.github.ibole.microservice.registry.RegistryFactory;
import com.github.ibole.microservice.registry.ServiceRegistry;
import com.github.ibole.microservice.registry.zookeeper.ZkRegistryFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * simple ZkTool.
 */
public class ZkTool {
  // private static Logger log = LoggerFactory.getLogger(AppTest.class);
  // private static final String PATH = "/discovery/example";
  static CuratorFramework client;
  static ServerIdentifier identifier = null;

  /**
   * Sets up the fixture, for example, open a network connection. This method is called before a
   * test is executed.
   */
  static {


    HostAndPort hostAndPort1 = HostAndPort.fromString("192.168.1.246:2181");
    //HostAndPort hostAndPort2 = HostAndPort.fromString("localhost:2182");
    //HostAndPort hostAndPort3 = HostAndPort.fromString("localhost:2183");
    ArrayList<HostAndPort> list = new ArrayList<HostAndPort>();
    list.add(hostAndPort1);
    //list.add(hostAndPort2);
    //list.add(hostAndPort3);
    identifier = new ServerIdentifier(list);

    //client = CuratorFrameworkFactory.newClient(identifier.getConnectionString(),
       // new ExponentialBackoffRetry(1000, 3));
    
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);  
    client = CuratorFrameworkFactory.builder()  
            .connectString(identifier.getConnectionString())  
            //.sessionTimeoutMs(5000)
            .connectionTimeoutMs(10000)
            .retryPolicy(retryPolicy)
            //.namespace("text")
            .build();
    
    
    client.start();
    try {
      
      if(client.getZookeeperClient().blockUntilConnectedOrTimedOut()){
        System.out.println("...");
      }
      
      
      
      
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  
    
  }

  /**
   * Main.
   * 
   * @param args param
   * @throws Exception exception
   */
  public static void main(String[] args) throws Exception {

    delete();
    
  }

  /**
   * Rigourous Test :-).
   * 
   * @throws Exception exception
   */
  public void testApp() throws Exception {
    // RegistryFactory<ServiceRegistry<InstanceMetadata>> factory = new ZkRegistryFactory();
    // ServiceRegistry<InstanceMetadata> registry = factory.getServiceRegistry(identifier);
    // registry.start();
    //
    // DiscoveryFactory<ServiceDiscovery<InstanceMetadata>> factory2 = new ZkDiscoveryFactory();
    // ServiceDiscovery<InstanceMetadata> discovery = factory2.getServiceDiscovery(identifier);
    // discovery.start();
    //
    //
    // InstanceMetadata metaData = new InstanceMetadata(UUID.randomUUID(), "localhost", 2184, true);
    // RegisterEntry entry = new RegisterEntry();
    // entry.setServiceType(ServiceType.RPC);
    // entry.setServiceName(ServerIdentifier.BASE_KEY_PREFIX);
    // entry.setServiceContract("com.test.practices.GreeterSub");
    // entry.setInstanceMetadata(metaData);
    //
    // registry.register(entry);
    // long times = System.currentTimeMillis();
    // InstanceMetadata instance1 =
    // discovery.getInstanceById("com.test.practices.GreeterSub", metaData.getId().toString());
    // times = System.currentTimeMillis() - times;
    // //log.info("Get instance at the fisrt time in {} ms", times);
    // times = System.currentTimeMillis();
    // InstanceMetadata instance2 =
    // discovery.getInstanceById("com.test.practices.GreeterSub", metaData.getId().toString());
    // times = System.currentTimeMillis() - times;
    // //log.info("Get instance at the second time in {} ms", times);
    //
    // registry.unregisterService(entry);
    //
    // assertTrue(instance1.getPort() == instance2.getPort());
    //
    // InstanceMetadata newmetadata = registry.getInstanceById(ServiceType.RPC, "Greeter" ,
    // "com.test.practices.GreeterSub", metaData.getId().toString());
    // assertTrue(newmetadata.equals(metaData));
    //
    // registry.destroy();
    // assertTrue(true);
  }

  /**
   * clean zk.
   * 
   * @throws Exception exception
   */
  public static void delete() throws Exception {
    RegistryFactory<ServiceRegistry<InstanceMetadata>> factory = new ZkRegistryFactory();
    ServiceRegistry<InstanceMetadata> registry = factory.getServiceRegistry(identifier);
    registry.start();

    DiscoveryFactory<ServiceDiscovery<InstanceMetadata>> factory2 = new ZkDiscoveryFactory();
    ServiceDiscovery<InstanceMetadata> discovery = factory2.getServiceDiscovery(identifier);
    discovery.start();
    String[] serviceNames = {"com.github.ibole.demo.protos.GreeterGrpc$GreeterSub",
        "com.github.ibole.demo.protos.GreeterGrpc$GreeterBlockingStub",
        "com.github.ibole.demo.protos.GreeterGrpc$GreeterFutureStub"};
    for (String name : serviceNames) {

      List<InstanceMetadata> services = discovery.listAll(name);
      for (InstanceMetadata service : services) {

        String path = identifier.getRootPath().getPath() + "/" + name + "/" + service.generateKey();
        System.out.println(path);
        client.delete().forPath(path);
      }
    }
    
    registry.destroy();
    discovery.destroy();
    client.close();
    
  }
}
