/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.util.*;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeNode;
import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.junit.*;
import org.openide.util.Exceptions;
import ovation.*;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;
import us.physion.ovation.ui.test.TestManager;

/**
 *
 * @author jackie
 */
public class TagTableListenerTest extends OvationTestCase{
    private TestEntityWrapper project;
    private TestEntityWrapper project2;
    private MockResizableTree mockTree;
    
    static TestManager mgr = new SelectionViewTestManager();
    
    public TagTableListenerTest() {
        setTestManager(mgr); 
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
        
        mockTree = new MockResizableTree();
        
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
    public void testTableChanged()
    {   
        Set<String> uris = new HashSet();
        uris.add(project.getURI());
        uris.add(project2.getURI());
        
        DataContext c = dsc.getContext();
        
        List<String> oldTags = new ArrayList<String>();
        
        TagsSet key = new TagsSet(c.currentAuthenticatedUser(), true, true, oldTags, uris);
        TreeNode node = new TableNode(key);
        //TagTableModelListener l = new TagTableModelListener(uris, mockTree, node, dsc);
        //l.addBlankRow();
    }
    
    
    @Test
    public void testAddNewBlankRow()
    {
    }
    
    @Test
    public void testAddNewTagToTableModelAndDatabase()
    {
        String newTag = "something";
        Set<String> uris = new HashSet();
        uris.add(project.getURI());
        uris.add(project2.getURI());
        
        TestTreeKey key = new TestTreeKey(dsc, uris);
  
        DefaultTableModel m = (DefaultTableModel)key.createTableModel();
      
        TestCase.assertEquals(m.getRowCount(), 0);
        
        JTable table = new JTable();
        EditableTable t = new EditableTable(table, new DummyTableTree());
        TableNode n = new TableNode(key);
        n.setPanel(t);
        TagTableModelListener listener = new TagTableModelListener(uris, mockTree, n, dsc);
        table.setModel(m);
        m.addTableModelListener(listener);
        
        addTag(t, newTag, 0, m, listener);       
        
        for (String uri : uris)
        {
            ITaggableEntityBase eb = (ITaggableEntityBase)dsc.getContext().objectWithURI(uri);
            assertContainsTag(newTag, eb, true);
        }
        
        TestCase.assertEquals(m.getValueAt(0, 0), newTag);
    }
    
    @Test
    public void testAddMultipleTagsToTableModelAndDatabase()
    {
        String newTag1 = "something";
        String newTag2 = "something2";
        Set<String> uris = new HashSet();
        uris.add(project.getURI());
        uris.add(project2.getURI());
        
        TestTreeKey key = new TestTreeKey(dsc, uris);
  
        DefaultTableModel m = (DefaultTableModel)key.createTableModel();
      
        TestCase.assertEquals(m.getRowCount(), 0);
        
        JTable table = new JTable();
        EditableTable t = new EditableTable(table, new DummyTableTree());
        TableNode n = new TableNode(key);
        n.setPanel(t);
        TagTableModelListener listener = new TagTableModelListener(uris, mockTree, n, dsc);
        table.setModel(m);
        m.addTableModelListener(listener);
        
        addTag(t, newTag1, 0, m, listener);
        addTag(t, newTag2, 1, m, listener);
        
        for (String uri : uris)
        {
            ITaggableEntityBase eb = (ITaggableEntityBase)dsc.getContext().objectWithURI(uri);
            assertContainsTag(newTag1, eb, true);
            assertContainsTag(newTag2, eb, true);
        }
        
        TestCase.assertEquals(m.getValueAt(0, 0), newTag1);
        TestCase.assertEquals(m.getValueAt(1, 0), newTag2);
    }
    
    @Test
    public void testEditTagModifiesTableModelAndDatabase()
    {
        String oldTag = "something old";
        String newTag = "something new";
        Set<String> uris = new HashSet();
        uris.add(project.getURI());
        uris.add(project2.getURI());
        
        TestTreeKey key = new TestTreeKey(dsc, uris);
  
        DefaultTableModel m = (DefaultTableModel)key.createTableModel();
      
        TestCase.assertEquals(m.getRowCount(), 0);
        
        JTable table = new JTable();
        EditableTable t = new EditableTable(table, new DummyTableTree());
        TableNode n = new TableNode(key);
        n.setPanel(t);
        TagTableModelListener listener = new TagTableModelListener(uris, mockTree, n, dsc);
        table.setModel(m);
        m.addTableModelListener(listener);
        
        addTag(t, oldTag, 0, m, listener);
        
        //edit tag
        m.setValueAt(newTag, 0, 0);
        TableModelEvent event = new TableModelEvent(m, 0, 0, 0, TableModelEvent.UPDATE);
        listener.tableChanged(event);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        for (String uri : uris)
        {
            ITaggableEntityBase eb = (ITaggableEntityBase)dsc.getContext().objectWithURI(uri);
            assertContainsTag(newTag, eb, true);
            assertContainsTag(oldTag, eb, false);
        }
        
        TestCase.assertEquals(m.getValueAt(0, 0), newTag);
    }
    
    @Test
    public void testAddEditMultipleTagsModifiesTableModelAndDatabase()
    {
        String oldTag1 = "something old1";
        String newTag1 = "something new1";
        String oldTag2 = "something old2";
        String newTag2 = "something new2";
        Set<String> uris = new HashSet();
        uris.add(project.getURI());
        uris.add(project2.getURI());
        
        TestTreeKey key = new TestTreeKey(dsc, uris);
  
        DefaultTableModel m = (DefaultTableModel)key.createTableModel();
      
        TestCase.assertEquals(m.getRowCount(), 0);
        
        JTable table = new JTable();
        EditableTable t = new EditableTable(table, new DummyTableTree());
        TableNode n = new TableNode(key);
        n.setPanel(t);
        TagTableModelListener listener = new TagTableModelListener(uris, mockTree, n, dsc);
        table.setModel(m);
        m.addTableModelListener(listener);
        
        addTag(t, oldTag1, 0, m, listener);
        addTag(t, oldTag2, 1, m, listener);
        
        //edit tag
        m.setValueAt(newTag1, 0, 0);
        TableModelEvent event = new TableModelEvent(m, 0, 0, 0, TableModelEvent.UPDATE);
        listener.tableChanged(event);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        //edit tag
        m.setValueAt(newTag2, 1, 0);
        event = new TableModelEvent(m, 1, 1, 0, TableModelEvent.UPDATE);
        listener.tableChanged(event);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        for (String uri : uris)
        {
            ITaggableEntityBase eb = (ITaggableEntityBase)dsc.getContext().objectWithURI(uri);
            assertContainsTag(newTag1, eb, true);
            assertContainsTag(oldTag1, eb, false);
            assertContainsTag(newTag2, eb, true);
            assertContainsTag(oldTag2, eb, false);
        }
        
        TestCase.assertEquals(m.getValueAt(0, 0), newTag1);
        TestCase.assertEquals(m.getValueAt(1, 0), newTag2);
    }
    
    @Test
    public void testEditTagBySettingToEmptyStringDoesNothing()
    {
        String newTag = "something";
        Set<String> uris = new HashSet();
        uris.add(project.getURI());
        uris.add(project2.getURI());
        
        TestTreeKey key = new TestTreeKey(dsc, uris);
  
        DefaultTableModel m = (DefaultTableModel)key.createTableModel();
      
        TestCase.assertEquals(m.getRowCount(), 0);
        
        JTable table = new JTable();
        EditableTable t = new EditableTable(table, new DummyTableTree());
        TableNode n = new TableNode(key);
        n.setPanel(t);
        TagTableModelListener listener = new TagTableModelListener(uris, mockTree, n, dsc);
        table.setModel(m);
        m.addTableModelListener(listener);
        
        addTag(t, newTag, 0, m, listener);
        
        //edit tag
        m.setValueAt("", 0, 0);
        TableModelEvent event = new TableModelEvent(m, 0, 0, 0, TableModelEvent.UPDATE);
        listener.tableChanged(event);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
       
        for (String uri : uris)
        {
            ITaggableEntityBase eb = (ITaggableEntityBase)dsc.getContext().objectWithURI(uri);
            assertContainsTag(newTag, eb, true);
        }
    }
    
    @Test
    public void testUpdatesDatabaseAndTableModelWithRowDeletion()
    {
        Set<String> uris = new HashSet();
        uris.add(project.getURI());
        uris.add(project2.getURI());
        
        String newTag1 = "something";
        String newTag2 = "something2";
        
        int i=0;
        for (String uri : uris)
        {
            ITaggableEntityBase eb = (ITaggableEntityBase)dsc.getContext().objectWithURI(uri);
            eb.addTag(newTag1);
            assertContainsTag(newTag1, eb, true);

            if (i++%2 == 0){
                eb.addTag(newTag2);//only some uris have the second tag
                assertContainsTag(newTag2, eb, true);
            }
        }
        
        TestTreeKey key = new TestTreeKey(dsc, uris);
  
        final DefaultTableModel m = new DefaultTableModel();
        m.setDataVector(new Object[][]{new Object[]{newTag1}, new Object[]{newTag2}}, new Object[]{"Value"});
      
        TestCase.assertEquals(m.getRowCount(), 2);
        TestCase.assertEquals(m.getValueAt(0, 0), newTag1);
        TestCase.assertEquals(m.getValueAt(1, 0), newTag2);
        
        JTable table = new JTable();
        EditableTable t = new EditableTable(table, new DummyTableTree());
        TableNode n = new TableNode(key);
        n.setPanel(t);
        TagTableModelListener listener = new TagTableModelListener(uris, mockTree, n, dsc);
        table.setModel(m);
        m.addTableModelListener(listener);
        
        t.deleteRows(new int[] {0, 1});
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        for (String uri : uris)
        {
            ITaggableEntityBase eb = (ITaggableEntityBase)dsc.getContext().objectWithURI(uri);
            assertContainsTag(newTag1, eb, false);
            assertContainsTag(newTag2, eb, false);
        }
        
        int rowCount = m.getRowCount();
        TestCase.assertEquals(rowCount, 0);
    }

    private void addTag(EditableTable t, String newTag, int row, DefaultTableModel m, TagTableModelListener listener)
    {
        t.addBlankRow();
        m.setValueAt(newTag, row, 0);
        TableModelEvent event = new TableModelEvent(m, row, row, 0, TableModelEvent.UPDATE);
        listener.tableChanged(event);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void assertContainsTag(String t, ITaggableEntityBase eb, boolean b) {
        boolean contains = false;
        for (String tag : eb.getMyTags())
        {
            if (t.equals(tag))
            {
                contains = true;
            }
        }
        TestCase.assertEquals(contains, b);
    }
    
    private class TestTreeKey extends TagsSet {

        public TestTreeKey(IAuthenticatedDataStoreCoordinator dsc, Set<String> uris) {
            super(dsc.getContext().currentAuthenticatedUser(), true, true , new ArrayList<String>(), uris);
        }
        
        @Override
        public TableModelListener createTableModelListener(ScrollableTableTree t, TableNode n) {
            if (isEditable()) {
                return new TagTableModelListener(uris, (ExpandableJTree) t.getTree(), n, dsc);
            }
            return null;
        }
    }
}
