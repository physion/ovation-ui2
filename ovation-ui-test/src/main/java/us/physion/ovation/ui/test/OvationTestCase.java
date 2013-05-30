/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.test;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.*;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.*;
import static org.junit.Assert.*;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.FileService;
import us.physion.ovation.OvationWebApi;
import us.physion.ovation.api.*;
import us.physion.ovation.couch.CouchServiceManager;
import us.physion.ovation.couch.OvationCouchModule;
import us.physion.ovation.domain.Group;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.dao.EntityDao;
import us.physion.ovation.domain.dto.EntityBase;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.exceptions.UserAccessException;
import us.physion.ovation.validation.ValidationResult;
import us.physion.ovation.api.OvationApiModule;
import us.physion.ovation.test.util.*;
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
    
    @BeforeClass
    public static void setUpClass() throws InterruptedException, ExecutionException {
        
         local_stack = new TestUtils().makeLocalStack(new OvationApiModule(),
                                          EMAIL.replace("@", "-").replace(".", "-"),
                                          EMAIL,
                                          PASSWORD);
         
         dsc = local_stack.getAuthenticatedDataStoreCoordinator();
    }
    
    public Injector getInjector()
    {
        return local_stack.getInjector();
    }

    @AfterClass
    public static void tearDownClass() throws InterruptedException {
        if (local_stack != null) {
            local_stack.cleanUp();
        }
    }
}