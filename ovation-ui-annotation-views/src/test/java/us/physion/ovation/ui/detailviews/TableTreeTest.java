/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.joda.time.DateTime;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import ovation.*;
import us.physion.ovation.ui.detailviews.MockResizableTree;
import us.physion.ovation.ui.detailviews.SelectionViewTestManager;
import us.physion.ovation.ui.interfaces.*;
import us.physion.ovation.ui.test.OvationTestCase;
import us.physion.ovation.ui.test.TestManager;

@ServiceProvider(service = Lookup.Provider.class)

/**
 *
 * @author huecotanks
 */
public class TableTreeTest extends OvationTestCase implements Lookup.Provider, ConnectionProvider{

    private Lookup l;
    InstanceContent ic;
    private TestEntityWrapper project;
    private TestEntityWrapper source;
    
    private Set<String> uris;
    private MockResizableTree mockTree;

    static TestManager mgr = new SelectionViewTestManager();
    public TableTreeTest() {
	setTestManager(mgr); //this is because there are static and non-static methods that need to use the test manager
        ic = new InstanceContent();
        l = new AbstractLookup(ic);
        ic.add(this);
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        OvationTestCase.setUpDatabase(mgr, 5);
    }
    
    @Before
    public void setUp() throws UserAuthenticationException {
        mockTree = new MockResizableTree();

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
        
        c.authenticateUser("newUser", "password");
        p.addProperty("color", "chartreuse");
        p.addProperty("interesting", true);
        
        uris = new HashSet();
        uris.add(project.getEntity().getURIString());
        uris.add(source.getEntity().getURIString());
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
    public void testUpdatesTableModelAndTreeWithRowAddition()
    {
        TableTreeKey k = new TestTreeKey(dsc, uris);
        TableNode node = new TableNode(k);
        PropertyTableModelListener listener = new PropertyTableModelListener(uris, mockTree, node, dsc);

        DefaultTableModel m = new DefaultTableModel();
        JTable table = new JTable();
        table.setModel(m);
        m.addTableModelListener(listener);
        EditableTable t = new EditableTable(table, null);
        t.addBlankRow();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        int rowCount = m.getRowCount();
        assertEquals(rowCount, 1);
        
        assertTrue(mockTree.wasResized());
    }
    
    @Test
    public void testAllEntitesContainModifiedPropertyValueEvenIfTheyHadNoPropertyBefore()
    {
        String key1 = "something";
        String val1 = "else";
        String newVal1 = "thing2";
        
        int i=0;
        for (String uri : uris)
        {
            IEntityBase eb = dsc.getContext().objectWithURI(uri);
            if (i++%2 == 0)
                eb.addProperty(key1, val1);//only some uris have the second property
        }
        
        TableTreeKey k = new TestTreeKey(dsc, uris);
        PropertyTableModelListener listener = new PropertyTableModelListener(uris, mockTree, new TableNode(k), dsc);
  
        final DefaultTableModel m = new DefaultTableModel();
        m.setDataVector(new Object[][]{new Object[]{key1, val1}}, new Object[]{"Name", "Parameter"});
      
        assertEquals(m.getRowCount(), 1);
        assertEquals(m.getValueAt(0, 0), key1);
        assertEquals(m.getValueAt(0, 1), val1);
        
        m.setValueAt(newVal1, 0, 1);//create a new
        TableModelEvent t = new TableModelEvent(m, 0, 0, 1, TableModelEvent.UPDATE);

        listener.tableChanged(t);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assertEquals(m.getRowCount(), 1);
    
        for (String uri : uris)
        {
            IEntityBase eb = dsc.getContext().objectWithURI(uri);
            assertEquals(eb.getMyProperty(key1), newVal1);
        }
    }
  
     @Test
    public void testUpdatesDatabaseAndTableModelWithModifiedPropertyKey()
    {
        String key1 = "something";
        String val1 = "else";
        String newKey1 = "thing2";
        
        int i=0;
        for (String uri : uris)
        {
            IEntityBase eb = dsc.getContext().objectWithURI(uri);
            eb.addProperty(key1, val1);
        }
        
        TableTreeKey k = new TestTreeKey(dsc, uris);
        PropertyTableModelListener listener = new PropertyTableModelListener(uris, mockTree, new TableNode(k), dsc);
  
        final DefaultTableModel m = new DefaultTableModel();
        m.setDataVector(new Object[][]{new Object[]{key1, val1}}, new Object[]{"Name", "Parameter"});
      
        assertEquals(m.getRowCount(), 1);
        assertEquals(m.getValueAt(0, 0), key1);
        assertEquals(m.getValueAt(0, 1), val1);
        
        m.setValueAt(newKey1, 0, 0);//create a new
        TableModelEvent t = new TableModelEvent(m, 0, 0, 0, TableModelEvent.UPDATE);

        listener.tableChanged(t);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assertEquals(m.getRowCount(), 1);
    
        for (String uri : uris)
        {
            IEntityBase eb = dsc.getContext().objectWithURI(uri);
            assertEquals(eb.getMyProperty(newKey1), val1);
        }
    }
    
     @Test
    public void testUpdatesDatabaseAndTableModelWithRowDeletion()
    {
        String newKey1 = "something";
        String newVal1 = "else";
        String newKey2 = "something2";
        String newVal2 = "else";
        
        int i=0;
        for (String uri : uris)
        {
            IEntityBase eb = dsc.getContext().objectWithURI(uri);
            eb.addProperty(newKey1, newVal1);
            
            if (i++%2 == 0)
                eb.addProperty(newKey2, newVal2);//only some uris have the second property
        }
        
        TableTreeKey k = new TestTreeKey(dsc, uris);
        PropertyTableModelListener listener = new PropertyTableModelListener(uris, mockTree, new TableNode(k), dsc);
  
        final DefaultTableModel m = new DefaultTableModel();
        m.setDataVector(new Object[][]{new Object[]{newKey1, newVal1}, new Object[]{newKey2, newVal2}}, new Object[]{"Name", "Parameter"});
      
        assertEquals(m.getRowCount(), 2);
        assertEquals(m.getValueAt(0, 0), newKey1);
        assertEquals(m.getValueAt(0, 1), newVal1);
        assertEquals(m.getValueAt(1, 0), newKey2);
        assertEquals(m.getValueAt(1, 1), newVal2);
        
        JTable table = new JTable();
        table.setModel(m);
        m.addTableModelListener(listener);
        EditableTable t = new EditableTable(table, null);
        
        t.deleteRows(new int[] {0, 1});
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        for (String uri : uris)
        {
            IEntityBase eb = dsc.getContext().objectWithURI(uri);
            assertFalse(eb.getProperties().containsKey(newKey1));
            assertFalse(eb.getProperties().containsKey(newKey2));
        }
        
        int rowCount = m.getRowCount();
        assertEquals(rowCount, 0);
    }
     
    @Test
    public void testNewPropertiesHaveTheRightType()
    {
        String key1 = "something";
        String val1 = "else";
        
        int i=0;
        for (String uri : uris)
        {
            IEntityBase eb = dsc.getContext().objectWithURI(uri);
            eb.addProperty(key1, val1);
        }
        
        TableTreeKey k = new TestTreeKey(dsc, uris);
        PropertyTableModelListener listener = new PropertyTableModelListener(uris, mockTree, new TableNode(k), dsc);
  
        final DefaultTableModel m = new DefaultTableModel();
        m.setDataVector(new Object[][]{new Object[]{key1, val1}}, new Object[]{"Name", "Parameter"});
      
        assertEquals(m.getRowCount(), 1);
        assertEquals(m.getValueAt(0, 0), key1);
        assertEquals(m.getValueAt(0, 1), val1);
        
        assertNewValueClassIsAppropriate(key1, "6/23/1988", Timestamp.class, m, listener);
        assertNewValueClassIsAppropriate(key1, "6/23/1988 6:30 pm", Timestamp.class, m, listener);
        assertNewValueClassIsAppropriate(key1, "1", Long.class, m, listener);
        assertNewValueClassIsAppropriate(key1, String.valueOf(Integer.MAX_VALUE) + "1", Long.class, m, listener);
        assertNewValueClassIsAppropriate(key1, "1.5", Double.class, m, listener);
        assertNewValueClassIsAppropriate(key1, "True", Boolean.class, m, listener);
        assertNewValueClassIsAppropriate(key1, "false", Boolean.class, m, listener);
    }
    
    void assertNewValueClassIsAppropriate(String key, String newValue, Class clazz, DefaultTableModel m, PropertyTableModelListener listener)
    {
        m.setValueAt(newValue, 0, 1);
        TableModelEvent t = new TableModelEvent(m, 0, 0, 0, TableModelEvent.UPDATE);

        listener.tableChanged(t);
        
        for (String uri : uris)
        {
            IEntityBase eb = dsc.getContext().objectWithURI(uri);
            assertEquals(eb.getMyProperty(key).getClass(), clazz);
        }
    }

    @Override
    public Lookup getLookup() {
        return l;
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
    
    private class TestTreeKey extends UserPropertySet {

        public TestTreeKey(IAuthenticatedDataStoreCoordinator dsc, Set<String> uris) {
            super(dsc.getContext().currentAuthenticatedUser(), true, true , new HashMap(), uris);
        }
        
        @Override
        public TableModelListener createTableModelListener(ScrollableTableTree t, TableNode n) {
            if (isEditable()) {
                return new PropertyTableModelListener(uris, (ExpandableJTree) t.getTree(), n, dsc);
            }
            return null;
        }
    }

}
