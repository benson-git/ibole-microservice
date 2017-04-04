package com.github.ibole.microservice.remoting.curator.lock;

import com.github.ibole.microservice.discovery.zookeeper.test.AbstractZkServerStarter;
import com.github.ibole.microservice.remoting.DistributedLockService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class IntegrationTest extends AbstractZkServerStarter {

	private CuratorDistributedLockServiceProvider provider;
	
	@BeforeClass
	public static void startZKserver() throws Exception{
	  startZKServer();
	}
	
	@AfterClass
    public static void closeZKserver() throws Exception{
	      closeZKServer();
	}
	   
	@Before
	public void initialize() {
		
		provider = new CuratorDistributedLockServiceProvider("localhost:2181", "1000", "1", "/test");
	}
	
	@Test
	public void lock() {
		final String lockName = UUID.randomUUID().toString();
		
		DistributedLockService lock = provider.getDistributedLock(1000);
		Assert.assertTrue(lock.tryLock(lockName));
		Assert.assertTrue(lock.tryLock(lockName));
		DistributedLockService lock2 = provider.getDistributedLock(1000);
		Assert.assertFalse(lock2.tryLock(lockName));
	}

}
