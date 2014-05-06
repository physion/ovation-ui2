/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import org.junit.*;
import static org.junit.Assert.*;
import us.physion.ovation.domain.Project;
import us.physion.ovation.ui.TableTreeKey;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;
/**
 *
 * @author huecotanks
 */
public class TagsViewTopComponentTest extends OvationTestCase{

    @Before
    public void setUp() {
        super.setUp();

    }

    private Project makeProject()
    {
        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        return ctx.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);
    }

    @Test
    public void testUpdateSetsMySelectedTagsProperly()
    {
        TagsViewTopComponent t = new TagsViewTopComponent();
        Set entitySet = new HashSet();

        Project project = makeProject();
        project.addTag("tag1");
        project.addTag("tag2");
        entitySet.add(new TestEntityWrapper(ctx, project));

        Project project2 = makeProject();
        project.addTag("tag1");
        project.addTag("another tag");
        entitySet.add(new TestEntityWrapper(ctx, project2));

        //both projects are selected
        List<TableTreeKey> tagSets = t.update(entitySet, ctx);
        List<String> myTagsFromUserNode = ((TagsSet)tagSets.get(0)).getTags();
        Set<String> mytags = new HashSet();
        for (String tag : project.getUserTags(ctx.getAuthenticatedUser()))
        {
            mytags.add(tag);
        }
        for (String tag : project2.getUserTags(ctx.getAuthenticatedUser()))
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
        entitySet.add(new TestEntityWrapper(ctx, project));
        myTagsFromUserNode = ((TagsSet)t.update(entitySet, ctx).get(0)).getTags();
        mytags = new HashSet<String>();
        for (String tag : project.getTags().values())
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

        Project project = makeProject();
        project.addTag("tag1");
        project.addTag("tag2");
        entitySet.add(new TestEntityWrapper(ctx, project));

        Project project2 = makeProject();
        project.addTag("tag1");
        project.addTag("another tag");
        entitySet.add(new TestEntityWrapper(ctx, project2));

        //both projects are selected
        List<TableTreeKey> tagSets = t.update(entitySet, ctx);
        List<String> myTagsFromUserNode = ((TagsSet)tagSets.get(0)).getTags();
        Set<String> mytags = new HashSet();
        for (String tag : project.getUserTags(ctx.getAuthenticatedUser()))
        {
            mytags.add(tag);
        }
        for (String tag : project2.getUserTags(ctx.getAuthenticatedUser()))
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
