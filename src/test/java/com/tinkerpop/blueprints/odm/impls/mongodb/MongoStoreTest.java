package com.tinkerpop.blueprints.odm.impls.mongodb;

import com.tinkerpop.blueprints.odm.StoreTestSuite;
import com.tinkerpop.blueprints.odm.SuiteConfiguration;
import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MongoStoreTest extends TestCase {

    private static final SuiteConfiguration config = new SuiteConfiguration();

    static {
        config.id = "_id";
    }

    public void testStoreTestSuite() throws Exception {
        doSuiteTest(new StoreTestSuite(config));
    }


    private void doSuiteTest(final StoreTestSuite suite) throws Exception {
        String doTest = System.getProperty("testMongoDB");
        if (doTest == null || doTest.equals("true")) {
            String host = System.getProperty("mongoDBHostname");
            if (host == null)
                host = "localhost";
            int portInt;
            String port = System.getProperty("mongoDBPort");
            if(null == port)
                portInt = 27017;
            else
                portInt = new Integer(port);
            String database = System.getProperty("mongoDBDatabase");
            if (null == database)
                database = "blueprints_test";
            for (Method method : suite.getClass().getDeclaredMethods()) {
                if (method.getName().startsWith("test")) {
                    System.out.println("Testing " + method.getName() + "...");
                    method.invoke(suite, new MongoStore(host, portInt, database, "collection"));
                }
            }
        }
    }

}
