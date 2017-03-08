package com.github.ibole.microservice.discovery.zookeeper.test;

import com.github.ibole.microservice.common.ServerIdentifier;

import com.google.common.net.HostAndPort;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;

public class CuratorClientTest {
  /** Zookeeper info */
  // private static final String ZK_ADDRESS = "RD:2181";
  private static final String ZK_PATH = "/zktest";

  public static void main(String[] args) throws Exception {

    HostAndPort hostAndPort1 = HostAndPort.fromString("localhost:2181");
    HostAndPort hostAndPort2 = HostAndPort.fromString("localhost:2182");
    HostAndPort hostAndPort3 = HostAndPort.fromString("localhost:2183");
    ArrayList<HostAndPort> list = new ArrayList<HostAndPort>();
    list.add(hostAndPort1);
    list.add(hostAndPort2);
    list.add(hostAndPort3);
    ServerIdentifier identifier = new ServerIdentifier(list);
    // 1.Connect to zk
    CuratorFramework client =
        CuratorFrameworkFactory.newClient(identifier.getConnectionString(), new RetryNTimes(10,
            5000));
    client.start();

    client.delete().forPath(ZK_PATH);

    System.out.println("zk client start successfully!");
    // 2.Client API test
    // 2.1 Create node
    String data1 = "hello";
    print("create", ZK_PATH, data1);
    client.create().withMode(CreateMode.PERSISTENT).forPath(ZK_PATH, data1.getBytes());

    // client.close();
    // // 2.2 Get node and data
    //
    // print("ls", "/");
    // print(client.getChildren().forPath("/"));
    // print("get", ZK_PATH);
    // print(client.getData().forPath(ZK_PATH));
    // // 2.3 Modify data
    // String data2 = "world";
    // print("set", ZK_PATH, data2);
    // client.setData().forPath(ZK_PATH, data2.getBytes());
    // print("get", ZK_PATH);
    // print(client.getData().forPath(ZK_PATH));
    // // 2.4 Remove node
    // print("delete", ZK_PATH);
    // client.delete().forPath(ZK_PATH);
    // print("ls", "/");
    // print(client.getChildren().forPath("/"));
  }

  private static void print(String... cmds) {
    StringBuilder text = new StringBuilder("$ ");
    for (String cmd : cmds) {
      text.append(cmd).append(" ");
    }
    System.out.println(text.toString());
  }

  private static void print(Object result) {
    System.out.println(result instanceof byte[] ? new String((byte[]) result) : result);
  }

}
