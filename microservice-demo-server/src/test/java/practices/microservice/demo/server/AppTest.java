package practices.microservice.demo.server;

import practices.microservice.rpc.server.ServerBootstrap;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
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

		String[] args1 = new String[]{"--port=8444","--use_tls=true","--reg_servers=localhost:2181,localhost:2182,localhost:2183"};
		
		ServerBootstrap.main(args1);
    	assertTrue( true );
    }
}
