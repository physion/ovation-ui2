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
import us.physion.ovation.ui.*;
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
    private EditableTable editableTable;
    
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
        project2  = new TestEntityWrapper(dsc, p);
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
    public void testAddMultipleTagsToTableModelAndDatabase()
    {
        String newTag1 = "something";
        String newTag2 = "something2";
        Set<String> uris = new HashSet();
        uris.add(project.getURI());
        uris.add(project2.getURI());
        
        TestTreeKey key = new TestTreeKey(dsc, uris);
        EditableTableModel m = createTableModel(key, uris);
        for (String uri : uris)
        {
            ITaggableEntityBase eb = (ITaggableEntityBase)dsc.getContext().objectWithURI(uri);
            assertContainsTag(newTag1, eb, false);
            assertContainsTag(newTag2, eb, false);
        }
        
        m.setValueAt(newTag1, 0, 0);
        m.setValueAt(newTag2, 1, 0);
        
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
    public void testEditMultipleTagsModifiesTableModelAndDatabase()
    {
        String oldTag1 = "something old1";
        String newTag1 = "something new1";
        String oldTag2 = "something old2";
        String newTag2 = "something new2";
        Set<String> uris = new HashSet();
        uris.add(project.getURI());
        uris.add(project2.getURI());
        
        TestTreeKey key = new TestTreeKey(dsc, uris);
        EditableTableModel m = createTableModel(key, uris);
        
        m.setValueAt(oldTag1, 0, 0);
        m.setValueAt(oldTag2, 1, 0);
        
        for (String uri : uris)
        {
            ITaggableEntityBase eb = (ITaggableEntityBase)dsc.getContext().objectWithURI(uri);
            assertContainsTag(newTag1, eb, false);
            assertContainsTag(oldTag1, eb, true);
            assertContainsTag(newTag2, eb, false);
            assertContainsTag(oldTag2, eb, true);
        }
        
        m.setValueAt(newTag1, 0, 0);
        m.setValueAt(newTag2, 1, 0);

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
        EditableTableModel m = createTableModel(key, uris);
        m.setValueAt(newTag, 0, 0);
        
        //edit tag
        m.setValueAt("", 0, 0);

        for (String uri : uris)
        {
            ITaggableEntityBase eb = (ITaggableEntityBase)dsc.getContext().objectWithURI(uri);
            assertContainsTag(newTag, eb, true);
        }
        TestCase.assertEquals(m.getValueAt(0, 0), newTag);
    }
    
    @Test
    public void testUpdatesDatabaseAndTableModelWithRowDeletion()
    {
        //Test:
        //project1 contains tag1, tag2, tag3
        //project2 contains tag1, tag3
        //removing rows 0, 1 should 
        //remove tag1 and tag2 from both projects
        
        Set<String> uris = new HashSet();
        uris.add(project.getURI());
        uris.add(project2.getURI());
        
        String newTag1 = "something";
        String newTag2 = "something2";
        String newTag3= "something3";
        
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
            eb.addTag(newTag3);
            assertContainsTag(newTag3, eb, true);
        }
        
        TestTreeKey key = new TestTreeKey(dsc, uris);
        final EditableTableModel m = createTableModel(key, uris);
        m.setValueAt(newTag1, 0, 0);
        m.setValueAt(newTag2, 1, 0);
        m.setValueAt(newTag3, 2, 0);
      
        TestCase.assertEquals(m.getValueAt(0, 0), newTag1);
        TestCase.assertEquals(m.getValueAt(1, 0), newTag2);
        TestCase.assertEquals(m.getValueAt(2, 0), newTag3);
        
        editableTable.deleteRows(new int[] {0, 1});
        
        for (String uri : uris)
        {
            ITaggableEntityBase eb = (ITaggableEntityBase)dsc.getContext().objectWithURI(uri);
            assertContainsTag(newTag1, eb, false);
            assertContainsTag(newTag2, eb, false);
            assertContainsTag(newTag3, eb, true);
            
            TestCase.assertEquals(m.getValueAt(0, 0), newTag3);
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
        TestCase.assertEquals(b, contains);
    }

    private EditableTableModel createTableModel(TestTreeKey key, Set<String> uris) {
        EditableTableModel m = (EditableTableModel)key.createTableModel();

        JTable table = new JTable();
        editableTable = new EditableTable(table, new DummyTableTree());
        m.setTable(table);
        
        TableNode n = new TableNode(key);
        n.setPanel(editableTable);
        TagTableModelListener listener = new TagTableModelListener(uris, mockTree, n, dsc);
        table.setModel(m);
        m.addTableModelListener(listener);
        return m;
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
