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

import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
  protected final static int PORT = 2181;
  private static TestingServer server;
  private static File tempDirectory;
  
  protected static void startZKServer() throws Exception {
    tempDirectory = new File(System.getProperty("java.io.tmpdir")+'/'+System.nanoTime());
    server = new TestingServer(PORT, tempDirectory);
  }
  
  protected static void closeZKServer() {
    CloseableUtils.closeQuietly(server);
    tempDirectory.deleteOnExit();
  }
  
}
