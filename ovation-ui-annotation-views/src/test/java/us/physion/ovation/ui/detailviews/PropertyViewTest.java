/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.util.*;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.joda.time.DateTime;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import ovation.*;
import us.physion.ovation.ui.detailviews.ScrollableTableTree.TableInTreeCellRenderer;
import us.physion.ovation.ui.interfaces.*;
import us.physion.ovation.ui.test.OvationTestCase;
import us.physion.ovation.ui.test.TestManager;

@ServiceProvider(service = Lookup.Provider.class)
/**
 *
 * @author huecotanks
 */
public class PropertyViewTest extends OvationTestCase implements Lookup.Provider, ConnectionProvider{
    
    private Lookup l;
    InstanceContent ic;
    private TestEntityWrapper project;
    private TestEntityWrapper source;
    private TestEntityWrapper user1;
    private TestEntityWrapper user2;
    private Set<String> userURIs;
    private PropertiesViewTopComponent tc;
    
    static TestManager mgr = new SelectionViewTestManager();
    public PropertyViewTest() {
	setTestManager(mgr); //this is because there are static and non-static methods that need to use the test manager
        ic = new InstanceContent();
        l = new AbstractLookup(ic);
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        OvationTestCase.setUpDatabase(mgr, 5);
    }
    
    @Before
    public void setUp() throws UserAuthenticationException {
        dsc = setUpTest();
        Ovation.enableLogging(LogLevel.DEBUG);

        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        byte[] data = {1, 2, 3, 4, 5};
        String uti = "unknown-uti";
        
        DataContext c = dsc.getContext();
        User newUser = c.addUser("newUser", "password");
        project = new TestEntityWrapper(dsc, c.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START));
        source = new TestEntityWrapper(dsc, c.insertSource("source"));
        Project p = (Project)project.getEntity();
        p.addProperty("color", "yellow");
        p.addProperty("size", 10.5);
        Source s = (Source)source.getEntity();
        s.addProperty("id", 4);
        s.addProperty("birthday", "6/23/1988");
        
        user1 = new TestEntityWrapper(dsc, c.currentAuthenticatedUser());
        user2 = new TestEntityWrapper(dsc, newUser);
        userURIs = new HashSet();
        userURIs.add(user1.getURI());
        userURIs.add(user2.getURI());
        
        c.authenticateUser("newUser", "password");
        p.addProperty("color", "chartreuse");
        p.addProperty("interesting", true);
        
        ic.add(this);

        tc = new PropertiesViewTopComponent();
        tc.setTableTree(new DummyTableTree());
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
    public void testGetsProperTreeNodeStructure()
    {
        /*Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
        entitySet.add(project);
        entitySet.add(source);
        assertTrue( tc.getEntities() == null ||tc.getEntities().isEmpty());
        tc.setEntities(entitySet, dsc);
        
         try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        JTree tree = tc.getTableTree().getTree();
        DefaultMutableTreeNode n = (DefaultMutableTreeNode)((DefaultTreeModel)tree.getModel()).getRoot();
        assertEquals(n.getChildCount(), 2);

        DefaultMutableTreeNode currentUserNode = (DefaultMutableTreeNode)n.getChildAt(0);
        DefaultMutableTreeNode otherUserNode = (DefaultMutableTreeNode)n.getChildAt(1);
        
        assertTrue(((DefaultMutableTreeNode)currentUserNode.getChildAt(0)) instanceof TableNode);
        assertEquals(currentUserNode.getChildCount(), 1);
        assertTrue(((DefaultMutableTreeNode)otherUserNode.getChildAt(0)) instanceof TableNode);
        assertEquals(otherUserNode.getChildCount(), 1);
        * 
        */
    }
    
    @Test
    public void testGetsPropertiesAppropriatelyForEachUser()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
       
        entitySet.add(project);
        entitySet.add(source);
        
        ArrayList<TableTreeKey> properties = tc.setEntities(entitySet, dsc);
        assertEquals(properties.size(), 2);
        
        //user1 properties
        Set<TestTuple> props = TableTreeUtils.getTuples(properties.get(0));
        Set<TestTuple> databaseProps = getAggregateUserProperties(((User)user1.getEntity()), entitySet);
        assertTrue(TableTreeUtils.setsEqual(props, databaseProps));
        
        //user2 properties
        props = TableTreeUtils.getTuples(properties.get(1));
        databaseProps = getAggregateUserProperties(((User)user2.getEntity()), entitySet);
        assertTrue(TableTreeUtils.setsEqual(props, databaseProps));
        
    }
    
    @Test
    public void testCantEditOtherUsersProperty()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
        entitySet.add(project);
        entitySet.add(source);
        ArrayList<TableTreeKey> properties = tc.setEntities(entitySet, dsc);
       
        assertFalse(properties.get(1).isEditable());
    }
   
    static Set<TestTuple> getAggregateUserProperties(User u, Set<IEntityWrapper> entities) {
        
        Set<TestTuple> databaseProps = new HashSet<TestTuple>();
        for (IEntityWrapper ew : entities) {
            Map<String, Object> props = ew.getEntity().getUserProperties(u);
            for (String key : props.keySet())
            {
                databaseProps.add(new TestTuple(key, props.get(key)));
            }
        }
        return databaseProps;
    }

    @Override
    public IAuthenticatedDataStoreCoordinator getConnection() {
        return dsc;
    }

    @Override
    public void addConnectionListener(ConnectionListener cl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeConnectionListener(ConnectionListener cl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resetConnection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Lookup getLookup() {
        return l;
    }
}
