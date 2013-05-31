/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.util.*;
import java.util.concurrent.ExecutionException;
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
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.AnnotatableEntity;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.dao.EntityDao;
import us.physion.ovation.domain.dto.EntityBase;
import us.physion.ovation.domain.factories.UserFactory;
import us.physion.ovation.domain.impl.StdUserFactory;
import us.physion.ovation.domain.mixin.PropertyAnnotatable;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.TableTreeKey;
import us.physion.ovation.ui.interfaces.*;
import us.physion.ovation.ui.test.OvationTestCase;

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
        
        DataStoreCoordinator dsc2 = getInjector().getInstance(DataStoreCoordinator.class);
        try {
            dsc2.authenticateUser(otherEmail, otherPassword.toCharArray()).get();
        } catch (InterruptedException ex) {
            throw new OvationException(ex);
        } catch (ExecutionException ex) {
            throw new OvationException(ex);
        }
        assertTrue(dsc2.isAuthenticated());
        otherUserCtx = dsc2.getContext();
        
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
        otherUserCtx.getCoordinator().sync().get();
        addProperty(s1, user2, "key", "value");
        addProperty(s2, user2, "key2", "value2");
       
        ctx.getCoordinator().sync().get();
        entitySet.add(new TestEntityWrapper(ctx, source1));
        entitySet.add(new TestEntityWrapper(ctx, source2));
        
        //sanity check that everything has made it into the database as we expected
        assertTrue(source1.getUserProperties(user1).containsKey("key"));
        assertTrue(source1.getUserProperties(user2).containsKey("key"));
        assertTrue(source2.getUserProperties(user1).containsKey("key"));
        assertTrue(source2.getUserProperties(user2).containsKey("key2"));
        
        List<TableTreeKey> properties = tc.setEntities(entitySet, ctx);
        assertEquals(properties.size(), 2);
        
        //user1 properties
        Set<Tuple> props = TableTreeUtils.getTuples(properties.get(0));
        Set<Tuple> databaseProps = getAggregateUserProperties(user1, entitySet);
        assertTrue(TableTreeUtils.setsEqual(props, databaseProps));
        
        //user2 properties
        props = TableTreeUtils.getTuples(properties.get(1));
        databaseProps = getAggregateUserProperties(user2, entitySet);
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
        
        Set<Tuple> databaseProps = new HashSet<Tuple>();
        for (IEntityWrapper ew : entities) {
            Map<String, Object> props = ((AnnotatableEntity)ew.getEntity()).getUserProperties(u);
            for (String key : props.keySet())
            {
                databaseProps.add(new Tuple(key, props.get(key)));
            }
        }
        return databaseProps;
    }
}
