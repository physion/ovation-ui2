/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.io.File;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import org.junit.*;
import ovation.IAuthenticatedDataStoreCoordinator;
import ovation.database.DatabaseManager;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;
import ovation.*;
import us.physion.ovation.ui.TableTreeKey;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;
import us.physion.ovation.ui.test.TestManager;
/**
 *
 * @author huecotanks
 */
public class TagsViewTopComponentTest extends OvationTestCase{
    private TestEntityWrapper project;
    private TestEntityWrapper project2;
    
    static TestManager mgr = new SelectionViewTestManager();
    public TagsViewTopComponentTest() {
        setTestManager(mgr); //this is because there are static and non-static methods that need to use the test manager
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        OvationTestCase.setUpDatabase(mgr, 6);
    }
    
    @Before
    public void setUp() {
        dsc = setUpTest();

        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        
        DataContext c = dsc.getContext();
        Project p = c.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);
        project = new TestEntityWrapper(dsc, p);
        p.addTag("tag1");
        p.addTag("tag2");
        
        Project p2 = c.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);
        project2 = project = new TestEntityWrapper(dsc, p);
        p2.addTag("tag1");
        p2.addTag("another tag");
        
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
    public void testUpdateSetsMySelectedTagsProperly()
    {
        TagsViewTopComponent t = new TagsViewTopComponent();
        Set entitySet = new HashSet();
        entitySet.add(project);
        entitySet.add(project2);
        
        //both projects are selected
        List<TableTreeKey> tagSets = t.update(entitySet, dsc);
        List<String> myTagsFromUserNode = ((TagsSet)tagSets.get(0)).getTags();
        Set<String> mytags = new HashSet();
        for (String tag : ((ITaggableEntityBase)project.getEntity()).getMyTags())
        {
            mytags.add(tag);
        }
        for (String tag : ((ITaggableEntityBase)project2.getEntity()).getMyTags())
        {
            mytags.add(tag);
        }
        assertEquals(myTagsFromUserNode.size(), mytags.size());
        
        for (String s : myTagsFromUserNode)
        {
            assertTrue(mytags.contains(s));
        }
        
        //a single project is selected
        entitySet = new HashSet();
        entitySet.add(project);
        myTagsFromUserNode = ((TagsSet)t.update(entitySet, dsc).get(0)).getTags();
        mytags = new HashSet<String>();
        for (String tag : ((ITaggableEntityBase)project.getEntity()).getTags())
        {
            mytags.add(tag);
        }
        assertEquals(myTagsFromUserNode.size(), mytags.size());
        
        for (String s : myTagsFromUserNode)
        {
            assertTrue(mytags.contains(s));
        }
    }
    
     @Test
    public void testUpdateSetsOtherUsersSelectedTagsProperly()
    {
    }

     @Test
     public void testOnlyMyTagsAreEditable()
     {
        TagsViewTopComponent t = new TagsViewTopComponent();
        Set entitySet = new HashSet();
        entitySet.add(project);
        entitySet.add(project2);
        
        //both projects are selected
        List<TableTreeKey> tagSets = t.update(entitySet, dsc);
        List<String> myTagsFromUserNode = ((TagsSet)tagSets.get(0)).getTags();
        Set<String> mytags = new HashSet();
        for (String tag : ((ITaggableEntityBase)project.getEntity()).getMyTags())
        {
            mytags.add(tag);
        }
        for (String tag : ((ITaggableEntityBase)project2.getEntity()).getMyTags())
        {
            mytags.add(tag);
        }
        assertEquals(myTagsFromUserNode.size(), mytags.size());
        
        for (String s : myTagsFromUserNode)
        {
            assertTrue(mytags.contains(s));
        }
        
     }
}
