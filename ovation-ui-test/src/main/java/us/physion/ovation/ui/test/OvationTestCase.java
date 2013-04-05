/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.File;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.*;
import static org.junit.Assert.*;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.api.Ovation;
import us.physion.ovation.api.OvationApiModule;
import us.physion.ovation.domain.User;
/**
 *
 * @author huecotanks
 */
public class OvationTestCase {

    public OvationTestCase() { }
    public DataStoreCoordinator dsc;

    public static UUID USER_UUID;
    public final static String EMAIL = "me@bobslawblog.com";
    public final static String USER_NAME = "jackie";
    public final static char[] PASSWORD = "poorPasswordChoice".toCharArray();
    
    @BeforeClass
    public static void setUpClass() {}
    
    @Before
    public void setUp() {
        OvationApiModule m = new OvationApiModule();
        Injector injector = Guice.createInjector(m);
        injector.getInstance(DataStoreCoordinator.class);
        dsc = Ovation.newDataStoreCoordinator();
        createUser();
    }
    
    @After
    public void tearDown() throws InterruptedException
    {
        dsc.deleteDB();
        //queryService.reset();
        Thread.sleep(1000);
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {}
    
    public static void setUpDatabase() {}
    
    public void createUser()
    {
        DataContext ctx = dsc.getContext();
        User u = ctx.addUser(USER_NAME, EMAIL, PASSWORD);
        USER_UUID = u.getUuid();
        dsc.authenticateUser(USER_UUID, PASSWORD);
    }
}
