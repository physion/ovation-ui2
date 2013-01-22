/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.io.File;
import java.net.InetAddress;
import java.util.*;
import junit.framework.TestCase;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.joda.time.DateTime;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import ovation.IAuthenticatedDataStoreCoordinator;
import ovation.*;
import ovation.database.DatabaseManager;
import us.physion.ovation.ui.editor.ResponseViewTestManager;
import us.physion.ovation.ui.editor.ResponseViewTopComponent;
import us.physion.ovation.ui.test.TestManager;
import us.physion.ovation.ui.interfaces.*;
import us.physion.ovation.ui.test.OvationTestCase;

/**
 *
 * @author huecotanks
 */
public class ResponseViewTopComponentTest extends OvationTestCase{
    
   IAuthenticatedDataStoreCoordinator dsc;
   Experiment experiment = null;
   Epoch epoch = null;
    static TestManager mgr = new ResponseViewTestManager();
    public ResponseViewTopComponentTest() {
        setTestManager(mgr); //this is because there are static and non-static methods that need to use the test manager
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        OvationTestCase.setUpDatabase(mgr, 4);
    }
    
    @Before
    public void setUp() {
        dsc = setUpTest();
        
        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        DateTime UNUSED_END = new DateTime(1);
        String UNUSED_LABEL = "label";
        
        DataContext c = dsc.getContext();
        Project project = c.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);
        experiment = project.insertExperiment(UNUSED_PURPOSE, UNUSED_START);

        Source p = c.insertSource("experiment source");
        Source source = p.insertSource("source");

        EpochGroup group = experiment.insertEpochGroup(source, UNUSED_LABEL, UNUSED_START);

        epoch = group.insertEpoch(UNUSED_START, UNUSED_END, "protocol", new HashMap<String, Object>());

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
    public void testGraphsSelectedEntity() {
        
        ResponseViewTopComponent t = new ResponseViewTopComponent();
        //assertNotNull(Lookup.getDefault().lookup(ConnectionProvider.class));
        Collection entities = new HashSet();
        ExternalDevice dev = experiment.externalDevice("device-name", "manufacturer");
        double[] d = new double[10000];
        for (int i=0; i< d.length; ++i)
        {
            d[i] = i;
        }
        NumericData data = new NumericData(d);
        String units = "units";
        String dimensionLabel = "dimension label";
        double samplingRate = 3;
        String samplingRateUnits = "Hz";
        String dataUTI = Response.NUMERIC_DATA_UTI;
        Response r = epoch.insertResponse(dev, new HashMap(), data, units, dimensionLabel, samplingRate, samplingRateUnits, dataUTI);
        entities.add(new TestEntityWrapper(dsc, r));
        
        List<Visualization> chartWrappers= t.updateEntitySelection(entities);
        
        assertEquals(chartWrappers.size(), entities.size());
        
        for (Visualization w : chartWrappers)
        {
            if (w instanceof ChartGroupWrapper) {
                ChartGroupWrapper p = (ChartGroupWrapper) w;
                XYDataset ds = p.getDataset();
                Comparable key = ds.getSeriesKey(0);
                assertEquals(key, dev.getName());
                for (int i = 0; i < d.length; ++i) {
                    assertTrue(d[i] == ds.getYValue(0, i));
                    assertTrue(i / samplingRate == ds.getXValue(0, i));
                }

                assertEquals(p.getXAxis(), ChartWrapper.convertSamplingRateUnitsToGraphUnits(r.getSamplingUnits()[0]));
                assertEquals(p.getYAxis(), r.getUnits());
            }
        }
    }
    
    @Test
    public void testCreateChartFromChartWrapper() 
    {
        ExternalDevice dev = experiment.externalDevice("device-name", "manufacturer");
        double[] d = new double[10000];
        for (int i=0; i< d.length; ++i)
        {
            d[i] = i;
        }
        NumericData data = new NumericData(d);
        String units = "units";
        String dimensionLabel = "dimension label";
        double samplingRate = 3;
        String samplingRateUnits = "Hz";
        String dataUTI = Response.NUMERIC_DATA_UTI;
        Response r = epoch.insertResponse(dev, new HashMap(), data, units, dimensionLabel, samplingRate, samplingRateUnits, dataUTI);
        
        ChartGroupWrapper cw = (ChartGroupWrapper)ResponseWrapperFactory.create(r).createVisualization(r);
        DefaultXYDataset ds = cw.getDataset();
        ChartPanel p = cw.generateChartPanel();
        XYPlot plot = p.getChart().getXYPlot();
        Comparable key = ds.getSeriesKey(0);
        assertEquals(key, dev.getName());
        for (int i = 0; i < d.length; ++i) {
            assertTrue(d[i] == ds.getYValue(0, i));
            assertTrue(i / samplingRate == ds.getXValue(0, i));
        }

        assertEquals(plot.getDomainAxis().getLabel(), ChartWrapper.convertSamplingRateUnitsToGraphUnits(r.getSamplingUnits()[0]));
        assertEquals(plot.getRangeAxis().getLabel(), r.getUnits());
    }
    
    @Test
    public void testGraphsMultipleSelectedEntitiesWithSharedUnits()
    {
        ResponseViewTopComponent t = new ResponseViewTopComponent();
        //assertNotNull(Lookup.getDefault().lookup(ConnectionProvider.class));
        Collection entities = new HashSet();
        ExternalDevice dev1 = experiment.externalDevice("device-name", "manufacturer");
        ExternalDevice dev2 = experiment.externalDevice("second-device-name", "manufacturer");
        double[] d = new double[10000];
        for (int i=0; i< d.length; ++i)
        {
            d[i] = i;
        }
        NumericData data = new NumericData(d);
        String units = "units";
        String dimensionLabel = "dimension label";
        double samplingRate = 3;
        String samplingRateUnits = "Hz";
        String dataUTI = Response.NUMERIC_DATA_UTI;
        Response r1 = epoch.insertResponse(dev1, new HashMap(), data, units, dimensionLabel, samplingRate, samplingRateUnits, dataUTI);
        Response r2 = epoch.insertResponse(dev2, new HashMap(), data, units, dimensionLabel, samplingRate, samplingRateUnits, dataUTI);

        entities.add(new TestEntityWrapper(dsc, epoch));
        
        Collection<Visualization> chartWrappers= t.updateEntitySelection(entities);
        
        assertEquals(1, chartWrappers.size());
        
        Set<String> series = new HashSet();
        for (Visualization w : chartWrappers)
        {
            if (w instanceof ChartGroupWrapper) {
                ChartGroupWrapper p = (ChartGroupWrapper) w;
                XYDataset ds = p.getDataset();
                series.add(ds.getSeriesKey(0).toString());
                series.add(ds.getSeriesKey(1).toString());
            }
        }
        
        assertEquals(series.size(), 2);
        assertTrue(series.contains(dev1.getName()));
        assertTrue(series.contains(dev2.getName()));
        
    }
    
    @Test
    public void testGraphsMultipleSelectedEntitiesWithoutSharedUnits()
    {
        ResponseViewTopComponent t = new ResponseViewTopComponent();
        //assertNotNull(Lookup.getDefault().lookup(ConnectionProvider.class));
        Collection entities = new HashSet();
        ExternalDevice dev1 = experiment.externalDevice("device-name", "manufacturer");
        ExternalDevice dev2 = experiment.externalDevice("second-device-name", "manufacturer");
        double[] d = new double[10000];
        for (int i=0; i< d.length; ++i)
        {
            d[i] = i;
        }
        NumericData data = new NumericData(d);
        String units = "units";
        String dimensionLabel = "dimension label";
        double samplingRate = 3;
        String samplingRateUnits = "Hz";
        String dataUTI = Response.NUMERIC_DATA_UTI;
        Response r1 = epoch.insertResponse(dev1, new HashMap(), data, units, dimensionLabel, samplingRate, samplingRateUnits, dataUTI);
        Response r2 = epoch.insertResponse(dev2, new HashMap(), data, "other-units", dimensionLabel, samplingRate, samplingRateUnits, dataUTI);

        entities.add(new TestEntityWrapper(dsc, r1));
        entities.add(new TestEntityWrapper(dsc, r2));
        
        Collection<Visualization> chartWrappers= t.updateEntitySelection(entities);
        
        assertEquals(chartWrappers.size(), entities.size());
        
        Set<String> series = new HashSet();
        for (Visualization w : chartWrappers)
        {
            if (w instanceof ChartGroupWrapper) {
                ChartGroupWrapper p = (ChartGroupWrapper) w;
                XYDataset ds = p.getDataset();
                series.add(ds.getSeriesKey(0).toString());
            }
        }
        
        assertEquals(series.size(),  entities.size());
        assertTrue(series.contains(dev1.getName()));
        assertTrue(series.contains(dev2.getName()));
    }
    
    @Test 
    public void testDisplaysDicomURLResponse()
    {
        
    }
    
    @Test
    public void testDisplaysDicomResponse()
    {
        //fail("implement");
    }
}
