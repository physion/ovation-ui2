/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import org.joda.time.DateTime;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.*;
import us.physion.ovation.domain.mixin.PropertyAnnotatable;
import us.physion.ovation.ui.*;
import us.physion.ovation.ui.detailviews.MockResizableTree;
import us.physion.ovation.ui.interfaces.*;
import us.physion.ovation.ui.test.OvationTestCase;

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
    private EditableTable editableTable;

    public TableTreeTest() {
        ic = new InstanceContent();
        l = new AbstractLookup(ic);
        ic.add(this);
    }
    
    @Before
    @Override
    public void setUp() {
        super.setUp();
        mockTree = new MockResizableTree();

        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        
        project = new TestEntityWrapper(ctx, ctx.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START));
        source = new TestEntityWrapper(ctx, ctx.insertSource("source", "2983"));
        
        uris = new HashSet();
        uris.add(project.getEntity().getURI().toString());
        uris.add(source.getEntity().getURI().toString());
    }
    
//table tree tests. TODO: separate out tests of the scrollable table tree to another test class
    @Test
    public void testUpdatesTableModelAndTreeWithRowAddition()
    {
        TableTreeKey k = new TestTreeKey(dsc, uris);
        EditableTableModel m = createTableModel(k, uris);
        assertEquals(m.getRowCount(), 1); //blank row

        m.setValueAt("something", 0, 0);
        assertEquals(m.getRowCount(), 2);
        
        assertTrue(mockTree.wasResized());
    }
 
    //property tree tests
    @Test
    public void testUpdatesDatabaseAndTableModelWithModifiedPropertyKey()
    {
        String key1 = "something";
        String val1 = "else";
        String newVal1 = "thing2";
        
        int i=0;
        for (String uri : uris)
        {
            PropertyAnnotatable eb = (PropertyAnnotatable)ctx.getObjectWithURI(uri);
            if (i++%2 == 0)
                eb.addProperty(key1, val1);//only half of the uris have the property
        }
        
        TableTreeKey k = new TestTreeKey(dsc, uris);
        EditableTableModel m = createTableModel(k, uris);
        m.setValueAt(key1, 0, 0);
        m.setValueAt(val1, 0, 1);
      
        assertEquals(m.getRowCount(), 2);
        assertEquals(m.getValueAt(0, 0), key1);
        assertEquals(m.getValueAt(0, 1), val1);
        
        m.setValueAt(newVal1, 0, 1);//change value 
        //this should add value to project1, and change value for project 2's existing property
        
        assertEquals(m.getRowCount(), 2);
    
        for (String uri : uris)
        {
            PropertyAnnotatable eb = (PropertyAnnotatable)ctx.getObjectWithURI(uri);
            assertEquals(eb.getUserProperty(getUser(), key1), newVal1);
        }
    }
    
    public User getUser()
    {
        return ctx.getAuthenticatedUser();
    }
  
     @Test
    public void testUpdatesDatabaseAndTableModelWithRowDeletion() throws InterruptedException
    {
        String newKey1 = "something";
        String newVal1 = "else";
        String newKey2 = "something2";
        String newVal2 = "else";
        
        int i=0;
        for (String uri : uris)
        {
            PropertyAnnotatable eb = (PropertyAnnotatable)ctx.getObjectWithURI(uri);
            eb.addProperty(newKey1, newVal1);
            
            if (i++%2 == 0)
                eb.addProperty(newKey2, newVal2);//only some uris have the second property
        }
        
        TableTreeKey k = new TestTreeKey(dsc, uris);
        EditableTableModel m = createTableModel(k, uris);
        
        m.setValueAt(newKey1, 0, 0);
        m.setValueAt(newVal1, 0, 1);
        m.setValueAt(newKey2, 1, 0);
        m.setValueAt(newVal2, 1, 1);
      
        assertEquals(m.getRowCount(), 3);
        assertEquals(m.getValueAt(0, 0), newKey1);
        assertEquals(m.getValueAt(0, 1), newVal1);
        assertEquals(m.getValueAt(1, 0), newKey2);
        assertEquals(m.getValueAt(1, 1), newVal2);
        
        editableTable.deleteRows(new int[] {0, 1});
        
        Thread.sleep(3000);

        for (String uri : uris)
        {
            PropertyAnnotatable eb = (PropertyAnnotatable)ctx.getObjectWithURI(uri);
            assertFalse(eb.getProperties().containsKey(newKey1));
            assertFalse(eb.getProperties().containsKey(newKey2));
        }
        
        assertEquals(1, m.getRowCount());
    }
     
    @Test
    public void testNewPropertiesHaveTheRightType()
    {
        String key1 = "something";
        String val1 = "else";
        
        for (String uri : uris)
        {
            PropertyAnnotatable eb = (PropertyAnnotatable)ctx.getObjectWithURI(uri);
            eb.addProperty(key1, val1);
        }
        
        TableTreeKey k = new TestTreeKey(dsc, uris);
        EditableTableModel m = createTableModel(k, uris);
        m.setValueAt(key1, 0, 0);
        m.setValueAt(val1, 0, 1);
      
        assertNewValueClassIsAppropriate(key1, "6/23/1988", DateTime.class, m);
        assertNewValueClassIsAppropriate(key1, "6/23/1988 6:30 pm", DateTime.class, m);
        assertNewValueClassIsAppropriate(key1, "1", Integer.class, m);
        assertNewValueClassIsAppropriate(key1, String.valueOf(Integer.MAX_VALUE) + "1", Long.class, m);
        assertNewValueClassIsAppropriate(key1, "1.5", Double.class, m);
        assertNewValueClassIsAppropriate(key1, "True", Boolean.class, m);
        assertNewValueClassIsAppropriate(key1, "false", Boolean.class, m);
    }
    
    void assertNewValueClassIsAppropriate(String key, String newValue, Class clazz, EditableTableModel m)
    {
        m.setValueAt(newValue, 0, 1);
        
        for (String uri : uris)
        {
            PropertyAnnotatable eb = (PropertyAnnotatable)ctx.getObjectWithURI(uri);
            assertEquals(eb.getUserProperty(ctx.getAuthenticatedUser(), key).getClass(), clazz);
        }
    }

    @Override
    public Lookup getLookup() {
        return l;
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
    public DataContext getDefaultContext() {
       return ctx;
    }

    @Override
    public DataContext getNewContext() {
        return ctx.getCoordinator().getContext();
    }

    @Override
    public void login() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private class TestTreeKey extends UserPropertySet {

        public TestTreeKey(DataStoreCoordinator dsc, Set<String> uris) {
            super(dsc.getContext().getAuthenticatedUser(), true, true , new HashMap(), uris);
        }
        
        @Override
        public TableModelListener createTableModelListener(ScrollableTableTree t, TableNode n) {
            if (isEditable()) {
                return new PropertyTableModelListener(uris, (ExpandableJTree) t.getTree(), n, ctx);
            }
            return null;
        }
    }
    private EditableTableModel createTableModel(TableTreeKey key, Set<String> uris) {
        EditableTableModel m = (EditableTableModel)key.createTableModel();

        JTable table = new JTable();
        editableTable = new EditableTable(table, new DummyTableTree());
        m.setTable(table);
        
        TableNode n = new TableNode(key);
        n.setPanel(editableTable);
        PropertyTableModelListener listener = new PropertyTableModelListener(uris, mockTree, n, ctx);
        table.setModel(m);
        m.addTableModelListener(listener);
        return m;
    }
}
