/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ibole.microservice.discovery.zookeeper.test;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * @author bwang
 *
 */
public abstract class AbstractZkServerStarter {
  
  protected final static Logger logger = LoggerFactory.getLogger(AbstractZkServerStarter.class.getName());
  protected final static String PORT = "2181";
  private static ZooKeeperServerMain zooKeeperServer;
  
  
  protected static void initialize() {
      
      Properties startupProperties = new Properties();
      startupProperties.put("dataDir", System.getProperty("java.io.tmpdir"));
      startupProperties.put("clientPort", PORT);

      
      QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
      try {
          quorumConfiguration.parseProperties(startupProperties);
      } catch(Exception e) {
          throw new RuntimeException(e);
      }

      zooKeeperServer = new ZooKeeperServerMain();
      final ServerConfig configuration = new ServerConfig();
      configuration.readFrom(quorumConfiguration);

      new Thread() {
          public void run() {
              try {
                  zooKeeperServer.runFromConfig(configuration);
              } catch (IOException e) {
                logger.error("ZkServerStarter initialize error", e);
              }
          }
      }.start();   
  }
  
}
