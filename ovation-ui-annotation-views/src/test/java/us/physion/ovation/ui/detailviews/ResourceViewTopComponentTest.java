/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RMISecurityManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import ovation.*;
import ovation.database.DatabaseManager;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;
import us.physion.ovation.ui.test.TestManager;

/**
 *
 * @author huecotanks
 */
public class ResourceViewTopComponentTest extends OvationTestCase{
    private IResourceWrapper rw1;
    private IResourceWrapper rw2;
    
    private TestEntityWrapper project;
    private TestEntityWrapper source;
    
    static TestManager mgr = new SelectionViewTestManager();
    public ResourceViewTopComponentTest() {
	setTestManager(mgr); //this is because there are static and non-static methods that need to use the test manager
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        OvationTestCase.setUpDatabase(mgr, 5);
    }
    
    @Before
    public void setUp() {
        dsc = setUpTest();

        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        byte[] data = {1, 2, 3, 4, 5};
        String uti = "unknown-uti";
        
        DataContext c = dsc.getContext();
        project = new TestEntityWrapper(dsc, c.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START));
        source = new TestEntityWrapper(dsc, c.insertSource("source"));
        Resource r1 = project.getEntity().addResource(uti, "resource 1", data);
        rw1 = new DummyResourceWrapper(dsc, r1);
        Resource r2 = source.getEntity().addResource(uti, "resource 2", data);
        rw2 = new DummyResourceWrapper(dsc, r2);

        Ovation.enableLogging(LogLevel.DEBUG);
    }
    
    
    @After
    public void tearDown()
    {
        tearDownTest();
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        OvationTestCase.tearDownDatabase(mgr);
    }


    
    @Test
    public void testUpdateResourcesUpdatesTheEntitiesList()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
        entitySet.add(project);
        entitySet.add(source);
        ResourceViewTopComponent t = new ResourceViewTopComponent();
        assertTrue( t.getResources().isEmpty());
        t.updateResources(entitySet);
        
        Set<String> nameSet = new HashSet();
        for (IResourceWrapper rw :t.getResources()){
            nameSet.add(rw.getName());
        }
        assertTrue(nameSet.contains(rw1.getName()));
        assertTrue(nameSet.contains(rw2.getName()));
        
        entitySet = new HashSet();
        entitySet.add(source);
        t.updateResources(entitySet);
        List<IResourceWrapper> resources = t.getResources();
        assertTrue(resources.get(0).getName().equals(rw2.getName()));
        assertEquals(resources.size(), 1);

    }
    
     @Test
    public void testUpdateResourcesUpdatesTheEditedSet()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
        entitySet.add(project);
        entitySet.add(source);
        ResourceViewTopComponent t = new ResourceViewTopComponent();
        t.updateResources(entitySet);
        
        t.editResource(rw1);
        assertTrue(t.editedSet.contains(rw1));
    }
     
     @Test
     public void testAddNewResource() throws IOException
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
        entitySet.add(source);
        ResourceViewTopComponent t = new ResourceViewTopComponent();
        t.updateResources(entitySet);
        File tmp = null;
        try{
             tmp = File.createTempFile("file", ".ext");
             BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
             out.write("Stuff in the file!!");
             out.close();
             t.addResource(entitySet, tmp.getAbsolutePath());
             
             boolean foundResource = false;
             for (IResourceWrapper rw : t.getResources())
             {
                 if (rw.getName().equals(tmp.getName()))
                 {
                     foundResource = true;
                 }
             }
             
             assertNotNull(source.getEntity().getResource(tmp.getName()));
             assertTrue(foundResource);
         }
        finally{
            tmp.delete();
        }
     }
     
    @Test
    public void testDeactivateSyncButtonWhenTheresNothingToSync()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
        entitySet.add(project);
        entitySet.add(source);
        ResourceViewTopComponent t = new ResourceViewTopComponent();
        t.updateResources(entitySet);
        assertFalse(t.saveButtonIsEnabled());
        
        t.editResource(rw1);
        assertTrue(t.saveButtonIsEnabled());
        
        t.removeResources(new IResourceWrapper[]{rw1}, entitySet);
        assertFalse(t.saveButtonIsEnabled());    
        assertEquals(t.getResources().size(), 1);
    } 
    
    
}
