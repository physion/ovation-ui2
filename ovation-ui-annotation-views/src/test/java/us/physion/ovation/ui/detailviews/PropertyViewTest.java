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
import us.physion.ovation.ui.TableTreeKey;
import us.physion.ovation.ui.interfaces.*;
import us.physion.ovation.ui.test.OvationTestCase;

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
    private UserFactory userFactory;
    
    public PropertyViewTest() {
        ic = new InstanceContent();
        l = new AbstractLookup(ic);
    }
    
    
    @Before
    public void setUp() {
        super.setUp();
        
        userFactory = getInjector().getInstance(StdUserFactory.class);
        
        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        
        project = new TestEntityWrapper(ctx, ctx.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START));
        source = new TestEntityWrapper(ctx, ctx.insertSource("source", "10010"));
        Project p = (Project)project.getEntity();
        p.addProperty("color", "yellow");
        p.addProperty("size", 10.5);
        Source s = (Source)source.getEntity();
        s.addProperty("id", 4);
        s.addProperty("birthday", "6/23/1988");
        
        User newUser = (User)userFactory.rehydrate(ctx, createUserDto(), new ArrayList());
        user1 = new TestEntityWrapper(ctx, ctx.getAuthenticatedUser());
        user2 = new TestEntityWrapper(ctx, newUser);
        userURIs = new HashSet();
        userURIs.add(user1.getURI());
        userURIs.add(user2.getURI());
        
        ctx.getCoordinator().authenticateUser(newUser.getEmail(), "password".toCharArray());
        p.addProperty("color", "chartreuse");
        p.addProperty("interesting", true);
        
        ic.add(this);

        tc = new PropertiesViewTopComponent();
        tc.setTableTree(new DummyTableTree());
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
        
        List<TableTreeKey> properties = tc.setEntities(entitySet, dsc);
        assertEquals(properties.size(), 2);
        
        //user1 properties
        Set<Tuple> props = TableTreeUtils.getTuples(properties.get(0));
        Set<Tuple> databaseProps = getAggregateUserProperties(((User)user1.getEntity()), entitySet);
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
        List<TableTreeKey> properties = tc.setEntities(entitySet, dsc);
       
        assertFalse(properties.get(1).isEditable());
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
    
    private us.physion.ovation.domain.dto.User createUserDto()
    {
        return new us.physion.ovation.domain.dto.User(){

            @Override
            public String getUsername() {
                return "username"; 
                        }

            @Override
            public String getEmail() {
                return "email"; }

            @Override
            public char[] getPasswordHash() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getDigestAlgorithm() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int getPkcs5Iterations() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public char[] getPasswordSalt() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public char[] getPasswordPepper() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public UUID getUuid() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getRevision() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Set<UUID> getWriteGroups() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public List<String> getConflicts() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Class getEntityClass() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public EntityBase.PersistentComponentUpdate getPersistentComponentsForUpdate(EntityDao entityDao) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void fetchPersistentComponents(EntityDao entityDao) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }

    @Override
    public DataStoreCoordinator getConnection() {
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
