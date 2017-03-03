/**
 * 
 */
package io.ibole.microservice.config.spring.annotation.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>
 * Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>
 * </p>
 *********************************************************************************************/


/**
 * @author bwang
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
// @ContextConfiguration(locations ={"classpath:/configuration/spring/beans.xml"})
//@Transactional
// @TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
public class RpcAnnotationTest {

  @Resource(name = "buzzServiceEdge")
  private BuzzServiceEdge buzzServiceEdge;

  @Test
  public void test() {

    //You can mock concrete classes, not just interfaces
    LinkedList mockedList = mock(LinkedList.class);
    
    mockedList.add("once");

    mockedList.add("twice");
    mockedList.add("twice");

    mockedList.add("three times");
    mockedList.add("three times");
    mockedList.add("three times");
  //following two verifications work exactly the same - times(1) is used by default
    verify(mockedList).add("once");
    verify(mockedList, times(1)).add("once");
    
  //exact number of invocations verification
    verify(mockedList, times(2)).add("twice");
    verify(mockedList, times(3)).add("three times");
    
  //verification using never(). never() is an alias to times(0)
    verify(mockedList, never()).add("never happened");
    
  //verification using atLeast()/atMost()
    verify(mockedList, atLeastOnce()).add("three times");
    //verify(mockedList, atLeast(2)).add("five times");
    //verify(mockedList, atMost(5)).add("three times");
    
   
    doThrow(new RuntimeException()).when(mockedList).clear();
    //mockedList.clear();
    
    
 // A. Single mock whose methods must be invoked in a particular order
    List singleMock = mock(List.class);

    //using a single mock
    singleMock.add("was added first");
    singleMock.add("was added second");

    //create an inOrder verifier for a single mock
    InOrder inOrder = inOrder(singleMock);

    //following will make sure that add is first called with "was added first, then with "was added second"
    inOrder.verify(singleMock).add("was added first");
    inOrder.verify(singleMock).add("was added second");
    
 // B. Multiple mocks that must be used in a particular order
    List firstMock = mock(List.class);
    List secondMock = mock(List.class);

    //using mocks
    firstMock.add("was called first");
    secondMock.add("was called second");

    //create inOrder object passing any mocks that need to be verified in order
    InOrder inOrder1 = inOrder(firstMock, secondMock);

    //following will make sure that firstMock was called before secondMock
    inOrder1.verify(firstMock).add("was called first");
    inOrder1.verify(secondMock).add("was called second");
    
    List mock = mock( List.class );  
    when( mock.get(0) ).thenReturn( 1 );  
    assertEquals( "预期返回1", 1, mock.get( 0 ) );// mock.get(0) 返回 1 
  }

}
