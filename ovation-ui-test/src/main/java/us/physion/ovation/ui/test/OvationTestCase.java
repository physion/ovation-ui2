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
    
     @Before
    public void setUp() {
        OvationApiModule m = new OvationApiModule();
        Injector injector = Guice.createInjector(m);
        dsc = injector.getInstance(DataStoreCoordinator.class);
        dsc = Ovation.newDataStoreCoordinator();
        createUser();
    }
    
    @After
    public void tearDown() throws InterruptedException
    {
        if (dsc != null)
        {
            dsc.deleteDB();
            //queryService.reset();
            Thread.sleep(1000);
        }
    }
    
    public void createUser()
    {
        DataContext ctx = dsc.getContext();
        ctx.addUser(USER_NAME, EMAIL, PASSWORD);
        dsc.authenticateUser(EMAIL, PASSWORD);
    }
}
