/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import com.google.common.collect.Multimap;
import java.io.File;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;
import ovation.*;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.mixin.Taggable;
import us.physion.ovation.ui.TableTreeKey;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;
/**
 *
 * @author huecotanks
 */
public class TagsViewTopComponentTest extends OvationTestCase{
    private TestEntityWrapper project;
    private TestEntityWrapper project2;
    
    @Before
    public void setUp() {

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
        for (String tag : ((Taggable)project.getEntity()).getUserTags(dsc.getContext().getAuthenticatedUser()))
        {
            mytags.add(tag);
        }
        for (String tag : ((Taggable)project2.getEntity()).getUserTags(dsc.getContext().getAuthenticatedUser()))
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
        for (String tag : ((Taggable)project.getEntity()).getTags().values())
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
        for (String tag : ((Taggable)project.getEntity()).getUserTags(dsc.getContext().getAuthenticatedUser()))
        {
            mytags.add(tag);
        }
        for (String tag : ((Taggable)project2.getEntity()).getUserTags(dsc.getContext().getAuthenticatedUser()))
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
