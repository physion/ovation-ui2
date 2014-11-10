/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.mixin.PropertyAnnotatable;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.TableTreeKey;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 *
 * @author huecotanks
 */
public class PropertyViewTest extends OvationTestCase {

    private Lookup l;
    InstanceContent ic;
    private User user1;
    private User user2;
    private Set<String> userURIs;
    private PropertiesViewTopComponent tc;
    private int sourceCount = 0;

    DataContext otherUserCtx;

    public PropertyViewTest() {
        ic = new InstanceContent();
        l = new AbstractLookup(ic);
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();

        String otherName = "new-user";
        String otherEmail = "new-user@email.com";
        String otherPassword = "password";

        user1 = ctx.getAuthenticatedUser();
        user2 = createNewUser(otherName, otherEmail, otherPassword);
        userURIs = new HashSet();
        userURIs.add(user1.getURI().toString());
        userURIs.add(user2.getURI().toString());

        otherUserCtx = dsc.getContext();

        ic.add(this);

        tc = new PropertiesViewTopComponent();
        tc.setTableTree(new DummyTableTree());
    }

    @After
    public void tearDown()
    {
        //delete the new user database created for user2
        if (otherUserCtx != null)
        {
            otherUserCtx.getCoordinator().deleteDB();
        }
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
    public void testGetsPropertiesAppropriatelyForEachUser() throws InterruptedException, ExecutionException
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();

        Source source1 = makeSource();
        Source source2 = makeSource();
        UUID s1 = source1.getUuid();
        UUID s2 = source2.getUuid();


        //add properties for user 1
        addProperty(s1, user1, "key", "value");
        addProperty(s2, user1, "key", "value");

        //sync down the sources, and add properties for user 2
        assertTrue(otherUserCtx.getCoordinator().sync().get());
        addProperty(s1, user2, "key", "value");
        addProperty(s2, user2, "key2", "value2");

        assertTrue(ctx.getCoordinator().sync().get());


        entitySet.add(new TestEntityWrapper(ctx, source1));
        entitySet.add(new TestEntityWrapper(ctx, source2));

        Thread.sleep(5000);

        User u2 = (User)ctx.getObjectWithUuid(user2.getUuid());
        //sanity check that everything has made it into the database as we expected
        assertTrue(source1.getUserProperties(user1).containsKey("key"));
        assertTrue(source1.getUserProperties(u2).containsKey("key"));
        assertTrue(source2.getUserProperties(user1).containsKey("key"));
        assertTrue(source2.getUserProperties(u2).containsKey("key2"));

        List<TableTreeKey> properties = tc.setEntities(entitySet, ctx);
        assertEquals(properties.size(), 2);

        //user1 properties
        Set<Tuple> props = TableTreeUtils.getTuples(properties.get(0));
        Set<Tuple> databaseProps = getAggregateUserProperties(user1, entitySet);
        assertTrue(TableTreeUtils.setsEqual(props, databaseProps));

        //user2 properties
        props = TableTreeUtils.getTuples(properties.get(1));
        databaseProps = getAggregateUserProperties(u2, entitySet);
        assertTrue(TableTreeUtils.setsEqual(props, databaseProps));
    }

    @Test
    public void testCantEditOtherUsersProperty() throws InterruptedException, ExecutionException
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();

        Source source1 = makeSource();
        UUID s1 = source1.getUuid();
        addProperty(s1, user1, "key", "value");

        otherUserCtx.getCoordinator().sync().get();
        addProperty(s1, user2, "key", "value");

        ctx.getCoordinator().sync().get();

        Thread.sleep(1000);

        //sanity check that everything has made it into the database as we expected
        assertTrue(source1.getUserProperties(user1).containsKey("key"));
        assertTrue(source1.getUserProperties(user2).containsKey("key"));

        entitySet.add(new TestEntityWrapper(ctx, source1));

        List<TableTreeKey> properties = tc.setEntities(entitySet, ctx);

        assertFalse(properties.get(1).isEditable());
    }

     public void addProperty(UUID obj, User user, String key, Object value)
    {
        if (user == user1)
        {
            ((PropertyAnnotatable)ctx.getObjectWithUuid(obj)).addProperty(key, value);
        }else if (user == user2)
        {
            ((PropertyAnnotatable)otherUserCtx.getObjectWithUuid(obj)).addProperty(key, value);
        }else{
            throw new OvationException("Tried to add property by a User that doesn't exist");
        }
    }

    public Source makeSource()
    {
        return ctx.insertSource("label" + sourceCount, "identifier" + sourceCount++);
    }

    static Set<Tuple> getAggregateUserProperties(User u, Set<IEntityWrapper> entities) {

        Set<Tuple> databaseProps = Sets.newHashSet();
        for (IEntityWrapper ew : entities) {
            Map<String, Object> props = ((PropertyAnnotatable)ew.getEntity()).getUserProperties(u);
            for (String key : props.keySet())
            {
                databaseProps.add(new Tuple(key, props.get(key)));
            }
        }
        return databaseProps;
    }
}
