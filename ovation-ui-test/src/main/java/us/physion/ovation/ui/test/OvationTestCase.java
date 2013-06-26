/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.test;

import com.google.inject.Injector;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.api.OvationApiModule;
import us.physion.ovation.domain.User;
import us.physion.ovation.test.util.LocalStack;
import us.physion.ovation.test.util.TestUtils;

import java.util.concurrent.ExecutionException;
/**
 *
 * @author huecotanks
 */
public class OvationTestCase {

    public OvationTestCase() {

    }
    public static DataStoreCoordinator dsc;
    public final static String EMAIL = "email@email.com";
    public final static String PASSWORD = "password";
    public final static String UNUSED_KEY = "UNUSED KEY";

    static LocalStack local_stack;
    public DataContext ctx;

    @BeforeClass
    public static void setUpClass() throws InterruptedException, ExecutionException {
        java.lang.Thread.sleep(1000);
         local_stack = new TestUtils().makeLocalStack(new OvationApiModule(),
                                          EMAIL.replace("@", "-").replace(".", "-"),
                                          EMAIL,
                                          PASSWORD);

         dsc = local_stack.getAuthenticatedDataStoreCoordinator();
    }

    @Before
    public void setUp()
    {
        ctx = dsc.getContext();
    }

    public Injector getInjector()
    {
        return local_stack.getInjector();
    }

    public User createNewUser(String userName, String email, String password)
    {
        return local_stack.createUser(userName, email, password.toCharArray());
    }

    @AfterClass
    public static void tearDownClass() throws InterruptedException {
        if (local_stack != null) {
            local_stack.cleanUp();
        }
    }
}
