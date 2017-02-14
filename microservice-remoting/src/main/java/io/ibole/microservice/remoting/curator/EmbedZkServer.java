package io.ibole.microservice.remoting.curator;

import com.google.common.io.Files;

import org.apache.curator.test.DirectoryUtils;
import org.apache.curator.test.TestingZooKeeperMain;
import org.apache.curator.test.ZooKeeperMainFace;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>
 * 版权所有：(c)2016， 深圳市拓润计算机软件开发有限公司
 * 
 * <p>
 * </p>
 *********************************************************************************************/


/**
 * @author bwang
 *
 */
public class EmbedZkServer {
  
  private static Logger logger = LoggerFactory.getLogger(EmbedZkServer.class.getName());
  
  public void boot(int port) throws Exception
  {
    logger.info("Booting Zookeeper Server...");
    ZooKeeperMainFace main = new TestingZooKeeperMain();
    Properties startupProperties = new Properties();
    File dataDirectory = Files.createTempDir();
    startupProperties.put("dataDir", dataDirectory.getAbsolutePath());
    startupProperties.put("clientPort", port);
    
    QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
    try {
      quorumConfiguration.parseProperties(startupProperties);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    Runtime.getRuntime().addShutdownHook(new Thread("SHUTDOWN-ZK-Server") {
      @Override
      public void run() {
        try {
          main.close();
          DirectoryUtils.deleteRecursively(dataDirectory);
        } catch (IOException ex) {
          logger.error("Shutdowning ZK Server error", ex);
        }
  
        logger.info("ZK Server has been shut down");
      }
    });
    
    new Thread(new Runnable()
      {
          public void run()
          {
              try
              {
                  main.runFromConfig(quorumConfiguration);
              }
              catch ( Exception e )
              {
                  logger.error("Starting ZK Server error", e);
              }
          }
      }).start();

      main.blockUntilStarted();
  }
  
}
