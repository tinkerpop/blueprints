package com.tinkerpop.blueprints.odm.impls.tinkerdoc;

import com.tinkerpop.blueprints.odm.StoreTestSuite;
import com.tinkerpop.blueprints.odm.SuiteConfiguration;
import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerStoreTest extends TestCase {

    private static final SuiteConfiguration config = new SuiteConfiguration();

    static {
        config.id = "_id";
    }

    public void testStoreTestSuite() throws Exception {
        doSuiteTest(new StoreTestSuite(config));
    }


    private void doSuiteTest(final StoreTestSuite suite) throws Exception {
        String doTest = System.getProperty("testTinkerDoc");
        if (doTest == null || doTest.equals("true")) {
            for (Method method : suite.getClass().getDeclaredMethods()) {
                if (method.getName().startsWith("test")) {
                    System.out.println("Testing " + method.getName() + "...");
                    method.invoke(suite, new TinkerStore());
                }
            }
        }
    }

}
