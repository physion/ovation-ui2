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
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.mixin.Taggable;
import us.physion.ovation.ui.*;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;

/**
 *
 * @author jackie
 */
public class TagTableListenerTest extends OvationTestCase{
    private MockResizableTree mockTree;
    private EditableTable editableTable;
    
    @Before
    public void setUp() {
        super.setUp();

        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        
        mockTree = new MockResizableTree();
    }
    
    private Project makeProject()
    {
        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        return ctx.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);
    }
    
    @Test
    public void testAddMultipleTagsToTableModelAndDatabase()
    {
        String newTag1 = "something";
        String newTag2 = "something2";
        Set<String> uris = new HashSet();
        Project project = makeProject();
        
        project.addTag("tag1");
        project.addTag("tag2");
        uris.add(project.getURI().toString());
        
        Project project2 = makeProject();
        project.addTag("tag1");
        project.addTag("another tag");
        uris.add(project2.getURI().toString());
        
        TestTreeKey key = new TestTreeKey(ctx, uris);
        EditableTableModel m = createTableModel(key, uris);
        for (String uri : uris)
        {
            Taggable eb = (Taggable)ctx.getObjectWithURI(uri);
            assertContainsTag(newTag1, eb, false);
            assertContainsTag(newTag2, eb, false);
        }
        
        m.setValueAt(newTag1, 0, 0);
        m.setValueAt(newTag2, 1, 0);
        
        for (String uri : uris)
        {
            Taggable eb = (Taggable)dsc.getContext().getObjectWithURI(uri);
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
        
        Project project = makeProject();
        project.addTag("tag1");
        project.addTag("tag2");
        uris.add(project.getURI().toString());
        
        Project project2 = makeProject();
        project.addTag("tag1");
        project.addTag("another tag");
        uris.add(project2.getURI().toString());
        
        TestTreeKey key = new TestTreeKey(ctx, uris);
        EditableTableModel m = createTableModel(key, uris);
        
        m.setValueAt(oldTag1, 0, 0);
        m.setValueAt(oldTag2, 1, 0);
        
        for (String uri : uris)
        {
            Taggable eb = (Taggable)ctx.getObjectWithURI(uri);
            assertContainsTag(newTag1, eb, false);
            assertContainsTag(oldTag1, eb, true);
            assertContainsTag(newTag2, eb, false);
            assertContainsTag(oldTag2, eb, true);
        }
        
        m.setValueAt(newTag1, 0, 0);
        m.setValueAt(newTag2, 1, 0);

        for (String uri : uris)
        {
            Taggable eb = (Taggable)ctx.getObjectWithURI(uri);
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
        
        Project project = makeProject();
        project.addTag("tag1");
        project.addTag("tag2");
        uris.add(project.getURI().toString());
        
        Project project2 = makeProject();
        project.addTag("tag1");
        project.addTag("another tag");
        uris.add(project2.getURI().toString());
        
        TestTreeKey key = new TestTreeKey(ctx, uris);
        EditableTableModel m = createTableModel(key, uris);
        m.setValueAt(newTag, 0, 0);
        
        //edit tag
        m.setValueAt("", 0, 0);

        for (String uri : uris)
        {
            Taggable eb = (Taggable)ctx.getObjectWithURI(uri);
            assertContainsTag(newTag, eb, true);
        }
        TestCase.assertEquals(m.getValueAt(0, 0), newTag);
    }
    
    @Test
    public void testUpdatesDatabaseAndTableModelWithRowDeletion() throws InterruptedException
    {
        //Test:
        //project1 contains tag1, tag2, tag3
        //project2 contains tag1, tag3
        //removing rows 0, 1 should 
        //remove tag1 and tag2 from both projects
        
        Set<String> uris = new HashSet();
        
        Project project = makeProject();
        project.addTag("tag1");
        project.addTag("tag2");
        uris.add(project.getURI().toString());
        
        Project project2 = makeProject();
        project.addTag("tag1");
        project.addTag("another tag");
        uris.add(project2.getURI().toString());
        
        String newTag1 = "something";
        String newTag2 = "something2";
        String newTag3= "something3";
        
        int i=0;
        for (String uri : uris)
        {
            Taggable eb = (Taggable)ctx.getObjectWithURI(uri);
            eb.addTag(newTag1);
            assertContainsTag(newTag1, eb, true);

            if (i++%2 == 0){
                eb.addTag(newTag2);//only some uris have the second tag
                assertContainsTag(newTag2, eb, true);
            }
            eb.addTag(newTag3);
            assertContainsTag(newTag3, eb, true);
        }
        
        TestTreeKey key = new TestTreeKey(ctx, uris);
        final EditableTableModel m = createTableModel(key, uris);
        m.setValueAt(newTag1, 0, 0);
        m.setValueAt(newTag2, 1, 0);
        m.setValueAt(newTag3, 2, 0);
      
        TestCase.assertEquals(m.getValueAt(0, 0), newTag1);
        TestCase.assertEquals(m.getValueAt(1, 0), newTag2);
        TestCase.assertEquals(m.getValueAt(2, 0), newTag3);
        
        editableTable.deleteRows(new int[] {0, 1});
        
        Thread.sleep(3000);
        
        for (String uri : uris)
        {
            Taggable eb = (Taggable)ctx.getObjectWithURI(uri);
            assertContainsTag(newTag1, eb, false);
            assertContainsTag(newTag2, eb, false);
            assertContainsTag(newTag3, eb, true);
            
            TestCase.assertEquals(m.getValueAt(0, 0), newTag3);
        }
    }

    private void assertContainsTag(String t, Taggable eb, boolean b) {
        boolean contains = false;
        for (String tag : eb.getUserTags(ctx.getAuthenticatedUser()))
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
        TagTableModelListener listener = new TagTableModelListener(uris, mockTree, n, ctx);
        table.setModel(m);
        m.addTableModelListener(listener);
        return m;
    }
    
    private class TestTreeKey extends TagsSet {

        public TestTreeKey(DataContext ctx, Set<String> uris) {
            super(ctx.getAuthenticatedUser(), true, true , new ArrayList<String>(), uris);
        }
        
        @Override
        public TableModelListener createTableModelListener(ScrollableTableTree t, TableNode n) {
            if (isEditable()) {
                return new TagTableModelListener(uris, (ExpandableJTree) t.getTree(), n, ctx);
            }
            return null;
        }
    }
}
