package io.ibole.microservice.rpc.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import io.grpc.stub.AbstractStub;

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
 * Base test for server service.
 * 
 * 一个JUnit 4 的单元测试用例执行顺序为： 
 * 
 * @BeforeClass –> @Before –> @Test –> @After –> @AfterClass 
 * 
 * @author bwang
 *
 */
@RunWith(JUnit4.class)
public class BaseServiceApiTest {
  
  private static ServiceTestResource testResource;
  /**
   * Get remoting service instance for client invocation.
   * 
   * @param type the type of expected service instance
   * @return T the instance of AbstractStub<T>.
   */
  public <T extends AbstractStub<T>> T getRemotingService(Class<T> type) {

    return ServiceTestResource.client.getRemotingService(type);
  }

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception { 
    if(!ServiceTestResource.testSuite){
      testResource = ServiceTestResource.newInstance(false);
      try {
        testResource.before();
      } catch (Throwable e) {
        testResource.after();
      }
    }
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    if(!ServiceTestResource.testSuite){
        testResource.after();
    }
  }

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}
}
