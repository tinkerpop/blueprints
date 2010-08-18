package com.tinkerpop.blueprints.odm.impls.orientdb;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import com.tinkerpop.blueprints.odm.StoreTestSuite;
import com.tinkerpop.blueprints.odm.SuiteConfiguration;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientStoreTest extends TestCase {
	private static final SuiteConfiguration	config	= new SuiteConfiguration();

	static {
		config.id = "@rid";
	}

	public void testStoreTestSuite() throws Exception {
		doSuiteTest(new StoreTestSuite(config));
	}

	private void doSuiteTest(final StoreTestSuite suite) throws Exception {
		String doTest = System.getProperty("testOrientDB");
		if (doTest == null || doTest.equals("true")) {
			String url = System.getProperty("orientURL");

			final OrientStore store = new OrientStore(url);
			if (store.exists()) {
				store.open("admin", "admin");
			} else
				store.create();

			for (Method method : suite.getClass().getDeclaredMethods()) {
				if (method.getName().startsWith("test")) {
					System.out.println("Testing " + method.getName() + "...");
					method.invoke(suite, store);
				}
			}
		}
	}
}
