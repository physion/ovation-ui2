/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.util.*;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.joda.time.DateTime;
import org.junit.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import ovation.*;
import us.physion.ovation.ui.interfaces.*;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.*;
import us.physion.ovation.ui.*;
import us.physion.ovation.ui.test.OvationTestCase;

@ServiceProvider(service = Lookup.Provider.class)
/**
 *
 * @author huecotanks
 */
public class ParameterViewTest extends OvationTestCase implements Lookup.Provider, ConnectionProvider{
        
    private Lookup l;
    InstanceContent ic;
    private TestEntityWrapper e1;
    private TestEntityWrapper e2;
    private ParametersTopComponent t;
    
    public ParameterViewTest() {
        ic = new InstanceContent();
        l = new AbstractLookup(ic);
    }
    
    public void setUp() {

        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        byte[] data = {1, 2, 3, 4, 5};
        String uti = "unknown-uti";
        
        DataContext c = dsc.getContext();
        Project p = c.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);
        HashMap params = new HashMap();
        params.put("color", "yellow");
        params.put("size", 10.5);
        e1 = new TestEntityWrapper(dsc, p.insertExperiment("purpose", UNUSED_START).insertEpochGroup("label", UNUSED_START, null, null, null).insertEpoch(UNUSED_START, UNUSED_START, null, params, null));
        params = new HashMap();
        params.put("id", 4);
        params.put("birthday", "6/23/1988");
        e2 = new TestEntityWrapper(dsc, p.insertExperiment("purpose", UNUSED_START).insertEpochGroup("label", UNUSED_START, null, null, null).insertEpoch(UNUSED_START, UNUSED_START, null, params, null));

        t = new ParametersTopComponent();
        t.setTableTree(new DummyTableTree());
        ic.add(this);

        Lookup.getDefault().lookup(ConnectionProvider.class);
    }
    
    /*TODO: test this somewhere
     * @Test
    public void testGetsProperTreeNodeStructure() throws InterruptedException {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();

        entitySet.add(e1);
        entitySet.add(e2);
        assertTrue(t.getEntities() == null || t.getEntities().isEmpty());
        t.setEntities(entitySet);

        JTree tree = t.getTableTree().getTree();
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) ((DefaultTreeModel) tree.getModel()).getRoot();
        assertEquals(n.getChildCount(), 1);

        DefaultMutableTreeNode currentUserNode = (DefaultMutableTreeNode) n.getChildAt(0);

        assertTrue(((DefaultMutableTreeNode) currentUserNode.getChildAt(0)) instanceof TableNode);
        assertEquals(currentUserNode.getChildCount(), 1);
    }*/
    
    @Test
    public void testGetsParametersAppropriatelyForEpochs()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
       
        entitySet.add(e1);
        entitySet.add(e2);
        List<TableTreeKey> params = t.setEntities(entitySet);
        
        assertEquals(params.size(), 1);
        assertTrue(params.get(0) instanceof ParameterSet);
        ParameterSet protocolParams = (ParameterSet)params.get(0);
        assertEquals(protocolParams.getDisplayName(), "Protocol Parameters");
        assertEquals(protocolParams.isEditable(), false);
        Set<Tuple> databaseParams = new HashSet<Tuple>();
        for (IEntityWrapper ew : entitySet)
        {
            Map<String, Object> ps = ((Epoch)ew.getEntity()).getProtocolParameters();
            aggregateDatabaseParams(databaseParams, ps);
        }
        assertTrue(TableTreeUtils.setsEqual(TableTreeUtils.getTuples(protocolParams), databaseParams));
    }
    
    @Test
    public void testGetsParametersAppropriatelyForResponses()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        Epoch e = ((Epoch)e1.getEntity());
        Map<String, Object> deviceParameters = new HashMap();
        deviceParameters.put("one", 1);
        deviceParameters.put("two", "fish");
        NumericData data = new NumericData();
        Measurement r1 = e.insertNumericMeasurement("name", new HashSet<String>(), new HashSet<String>(), data);
        e = ((Epoch)e2.getEntity());
        deviceParameters.put("one", 2);
        Measurement r2 = e.insertNumericMeasurement("name", new HashSet<String>(), new HashSet<String>(), data);
        entitySet.add(new TestEntityWrapper(dsc, r1));
        entitySet.add(new TestEntityWrapper(dsc, r2));

        List<TableTreeKey> params = t.setEntities(entitySet);
        
        assertEquals(params.size(), 1);
        assertTrue(params.get(0) instanceof ParameterSet);
        ParameterSet protocolParams = (ParameterSet)params.get(0);
        assertEquals(protocolParams.getDisplayName(), "Device Parameters");
        assertEquals(protocolParams.isEditable(), false);
        Set<Tuple> databaseParams = new HashSet<Tuple>();
        /*for (IEntityWrapper ew : entitySet)
        {
            Map<String, Object> ps = ((Measurement)ew.getEntity()).getDeviceParameters();
            aggregateDatabaseParams(databaseParams, ps);

        }*/
        assertTrue(TableTreeUtils.setsEqual(TableTreeUtils.getTuples(protocolParams), databaseParams));
    }
    
    @Test
    public void testGetsParametersAppropriatelyForAnalysisRecords()
    {
        /*Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        Epoch e = ((Epoch)e1.getEntity());
        Map<String, Object> deviceParameters = new HashMap();
        deviceParameters.put("one", 1);
        deviceParameters.put("two", "fish");
        AnalysisRecord a1 = e.getEpochGroup().getExperiment().getProjects()[0].insertAnalysisRecord("name", new Epoch[]{e}, "function name", deviceParameters, "file:///URL", "revision");
        e = ((Epoch)e2.getEntity());
        deviceParameters.put("one", 2);
        AnalysisRecord a2 = e.getEpochGroup().getExperiment().getProjects()[0].insertAnalysisRecord("name2", new Epoch[]{e}, "function name", deviceParameters, "file:///URL", "revision");
        entitySet.add(new TestEntityWrapper(dsc, a1));
        entitySet.add(new TestEntityWrapper(dsc, a2));
       
        List<TableTreeKey> params = t.setEntities(entitySet);
        
        assertEquals(params.size(), 1);
        assertTrue(params.get(0) instanceof ParameterSet);
        ParameterSet protocolParams = (ParameterSet)params.get(0);
        assertEquals(protocolParams.getDisplayName(), "Analysis Parameters");
        assertEquals(protocolParams.isEditable(), false);
        Set<Tuple> databaseParams = new HashSet<Tuple>();
        for (IEntityWrapper ew : entitySet)
        {
            //Map<String, Object> ps = ((AnalysisRecord)ew.getEntity()).getAnalysisParameters();
            //aggregateDatabaseParams(databaseParams, ps);
            
        }
        assertTrue(TableTreeUtils.setsEqual(TableTreeUtils.getTuples(protocolParams), databaseParams));
    */}
    
    @Override
    public Lookup getLookup() {
        return l;
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

    void assertSetsEqual(Set s1, Set s2) {
        assertEquals(s1.size(), s2.size());
        for (Object t1 : s1)
        {
            for (Object t2 : s2)
            {
                if (t1.equals(t2))
                {
                    s2.remove(t2);
                    break;
                }
            }    
                
        }
        assertTrue(s2.isEmpty());
        //assertTrue(s1.containsAll(s2));
    }
    
    Set<Tuple> getPropertiesByKey(String key, Set<Tuple> props)
    {
        Set<Tuple> result = new HashSet<Tuple>();
        for (Tuple p : props)
        {
            if (p.getKey().equals(key))
            {
                result.add(p);
            }
        }
        return result;
    }
    
    private Set<Tuple> getProperties(ScrollableTableTree t, String userURI) {
        Set<Tuple> properties = new HashSet<Tuple>();
        TableTreeKey k = t.getTableKey(userURI);
        if (k == null)
        {
            return properties;
        }    
        
        if (!(k instanceof ParameterSet))
            throw new RuntimeException("Wrong type!");
        
        Object[][] data = ((ParameterSet)k).getData();
        //DefaultTableModel m = ((DefaultTableModel) ((TableInTreeCellRenderer) t.getTree().getCellRenderer()).getTableModel(k));
        for (int i  = 0; i < data.length; ++i)
        {
            properties.add(new Tuple((String) data[i][0], data[i][1]));
        }
        return properties;
    }

    @Override
    public void resetConnection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void aggregateDatabaseParams(Set<Tuple> databaseParams, Map<String, Object> ps) {
        for (String key : ps.keySet())
            {
                if (!TableTreeUtils.getTuplesByKey(key, databaseParams).isEmpty())
                {
                    Tuple tuple = TableTreeUtils.getTuplesByKey(key, databaseParams).iterator().next();
                    MultiUserParameter p = new MultiUserParameter(tuple.getValue());
                    p.add(ps.get(tuple.getKey()));
                    databaseParams.remove(tuple);
                    databaseParams.add(new Tuple(tuple.getKey(), p));
                }
                else{
                    databaseParams.add(new Tuple(key, ps.get(key)));
                }
            }
    }
}
