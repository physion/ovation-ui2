/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.junit.Test;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.*;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.ui.ScrollableTableTree;
import us.physion.ovation.ui.TableTreeKey;
import us.physion.ovation.ui.interfaces.ConnectionListener;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import us.physion.ovation.DataContext;

@ServiceProvider(service = Lookup.Provider.class)
/**
 *
 * @author huecotanks
 */
public class ParameterViewTest extends OvationTestCase implements Lookup.Provider, ConnectionProvider{

    private Lookup l;
    InstanceContent ic;
    private ParametersTopComponent t;

    public ParameterViewTest() {
        ic = new InstanceContent();
        l = new AbstractLookup(ic);
    }

    public void setUp() {
        super.setUp();
        t = new ParametersTopComponent();
        t.setTableTree(new DummyTableTree());
        ic.add(this);
    }

    private Epoch createEpochWithProtocolParameters(Map<String, Object> params)
    {
        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        Source source = ctx.insertSource("source", "1");
        Project p = ctx.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);

        Map<String, Source> input = new HashMap();
        input.put("subject", source);

        return p.insertExperiment("purpose", UNUSED_START).insertEpochGroup("label", UNUSED_START, null, null, null).insertEpoch(input, null, UNUSED_START, UNUSED_START, null, params, null);

    }

    private Epoch createEpochWithDeviceParameters(Map<String, Object> params)
    {
        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        Source source = ctx.insertSource("source", "1");
        Project p = ctx.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);

        Map<String, Source> input = new HashMap();
        input.put("subject", source);

        return p.insertExperiment("purpose", UNUSED_START).insertEpochGroup("label", UNUSED_START, null, null, null).insertEpoch(input, null, UNUSED_START, UNUSED_START, null, null, params);

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
    public void testGetsProtocolParametersAppropriatelyForEpochs()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();

        Map<String, Object> epochParams = new HashMap();
        epochParams.put("thing1", "thing2");
        entitySet.add(new TestEntityWrapper(ctx, createEpochWithProtocolParameters(epochParams)));

        epochParams.put("thing1", "thing4");
        epochParams.put("thing3", "thing4");
        entitySet.add(new TestEntityWrapper(ctx, createEpochWithProtocolParameters(epochParams)));

        List<TableTreeKey> params = t.setEntities(entitySet);

        assertEquals(params.size(), 2);
        assertTrue(params.get(1) instanceof ParameterSet);
        ParameterSet protocolParams = (ParameterSet)params.get(1);
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
    public void testGetsDeviceParametersAppropriatelyForEpochs()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();

        Map<String, Object> epochParams = new HashMap();
        epochParams.put("thing1", "thing2");
        entitySet.add(new TestEntityWrapper(ctx, createEpochWithDeviceParameters(epochParams)));

        epochParams.put("thing1", "thing4");
        epochParams.put("thing3", "thing4");
        entitySet.add(new TestEntityWrapper(ctx, createEpochWithDeviceParameters(epochParams)));

        List<TableTreeKey> params = t.setEntities(entitySet);

        assertEquals(params.size(), 2);
        assertTrue(params.get(0) instanceof ParameterSet);
        ParameterSet deviceParams = (ParameterSet)params.get(0);
        assertEquals(deviceParams.getDisplayName(), "Device Parameters");
        assertEquals(deviceParams.isEditable(), false);
        Set<Tuple> databaseParams = new HashSet<Tuple>();
        for (IEntityWrapper ew : entitySet)
        {
            Map<String, Object> ps = ((Epoch)ew.getEntity()).getDeviceParameters();
            aggregateDatabaseParams(databaseParams, ps);
        }
        assertTrue(TableTreeUtils.setsEqual(TableTreeUtils.getTuples(deviceParams), databaseParams));

                }

    @Test
    public void testGetsParametersAppropriatelyForAnalysisRecords()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();

        Protocol protocol = ctx.insertProtocol("protocol-name", "protocol-doc");

        Epoch epoch = createEpochWithProtocolParameters(new HashMap());

        Map<String, Object> analysisProtocolParams = new HashMap();
        analysisProtocolParams.put("thing1", "thing2");
        AnalysisRecord a1 = epoch.addAnalysisRecord("a1", Maps.<String,DataElement>newHashMap(), protocol, analysisProtocolParams);
        entitySet.add(new TestEntityWrapper(ctx, a1));

        analysisProtocolParams.put("thing1", "thing4");
        analysisProtocolParams.put("thing3", "thing4");
        AnalysisRecord a2 = epoch.addAnalysisRecord("a1", Maps.<String,DataElement>newHashMap(), protocol, analysisProtocolParams);
        entitySet.add(new TestEntityWrapper(ctx, a2));

        List<TableTreeKey> params = t.setEntities(entitySet);

        assertEquals(params.size(), 1);
        assertTrue(params.get(0) instanceof ParameterSet);
        ParameterSet deviceParams = (ParameterSet)params.get(0);
        assertEquals(deviceParams.getDisplayName(), "Analysis Parameters");
        assertEquals(deviceParams.isEditable(), false);
        Set<Tuple> databaseParams = new HashSet<Tuple>();
        for (IEntityWrapper ew : entitySet)
        {
            Map<String, Object> ps = ((AnalysisRecord)ew.getEntity()).getProtocolParameters();
            aggregateDatabaseParams(databaseParams, ps);
        }
        assertTrue(TableTreeUtils.setsEqual(TableTreeUtils.getTuples(deviceParams), databaseParams));
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
}
