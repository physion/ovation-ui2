/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import com.google.common.collect.Sets;
import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
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
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.DataType;
import ucar.nc2.stream.NcStreamProto;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.*;
import us.physion.ovation.domain.factories.MeasurementFactory;
import us.physion.ovation.domain.impl.StdMeasurementFactory;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.editor.ResponseViewTopComponent;
import us.physion.ovation.ui.interfaces.*;
import us.physion.ovation.ui.test.OvationTestCase;
import us.physion.ovation.values.NumericData;

/**
 *
 * @author huecotanks
 */
public class ResponseViewTopComponentTest extends OvationTestCase{
    
    Experiment experiment;
    Epoch epoch;
    MeasurementFactory factory;
    
    ResponseViewTopComponent t;
  
    @Before
    public void setUp() {
        
        String UNUSED_NAME = "name";
        String UNUSED_PURPOSE = "purpose";
        DateTime UNUSED_START = new DateTime(0);
        DateTime UNUSED_END = new DateTime(1);
        String UNUSED_LABEL = "label";
        
        DataContext c = dsc.getContext();
        
        factory = getInjector().getInstance(StdMeasurementFactory.class);
        
        /*Project project = c.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);
        experiment = project.insertExperiment(UNUSED_PURPOSE, UNUSED_START);

        EpochGroup group = experiment.insertEpochGroup(UNUSED_LABEL, UNUSED_START, null, null, null);

        epoch = group.insertEpoch(UNUSED_START, UNUSED_END, null, null, null);*/
        //t = new ResponseViewTopComponent();
        //assertNotNull(Lookup.getDefault().lookup(ConnectionProvider.class));
    }
    
    private Measurement makeNumericMeasurement()
    {
        NumericData data = new NumericData();
        double[] d = new double[10000];
        for (int i=0; i< d.length; ++i)
        {
            d[i] = i;
        }
        data.addData("data-name", d, "units", 10.5, "Hz");
        String name = "name";
        Set<String> sourceNames = null;
        Set<String> devices = null; 
        Measurement r = factory.createNumericMeasurement(dsc.getContext(), name, UUID.randomUUID(), sourceNames, devices, data);
        //NumericMeasurement r = epoch.insertNumericMeasurement(name, sourceNames, devices, data);
        return r;
    }

    @Test
    public void testGraphsSelectedEntity() {
        t = new ResponseViewTopComponent();
        
        Measurement r = makeNumericMeasurement();
        
        String name = r.getName();
        NumericData.Data data;
        try {
            data = NumericMeasurementUtils.getNumericData(r).get().getData().get(0);
        } catch (InterruptedException ex) {
            throw new OvationException(ex.getLocalizedMessage());
        } catch (ExecutionException ex) {
            throw new OvationException(ex.getLocalizedMessage());
        }
        double samplingRate = data.samplingRates[0];
        String samplingUnit = data.samplingRateUnits[0];
        String units = data.units;
        double[] d = (double[])data.dataArray.get1DJavaArray(Double.class);
        
        Collection entities = Sets.newHashSet(new TestEntityWrapper(dsc, r));
        List<Visualization> chartWrappers= t.updateEntitySelection(entities);
        
        assertEquals(chartWrappers.size(), entities.size());
        
        for (Visualization w : chartWrappers)
        {
            if (w instanceof ChartGroupWrapper) {
                ChartGroupWrapper p = (ChartGroupWrapper) w;
                XYDataset ds = p.getDataset();
                Comparable key = ds.getSeriesKey(0);
                assertEquals(key, name);
                for (int i = 0; i < d.length; ++i) {
                    assertTrue(d[i] == ds.getYValue(0, i));
                    assertTrue(i / samplingRate == ds.getXValue(0, i));
                }

                assertEquals(p.getXAxis(), ChartGroupWrapper.convertSamplingRateUnitsToGraphUnits(samplingUnit));
                assertEquals(p.getYAxis(), units);
            }
        }
    }
    
    @Test
    public void testGraphsSelectedEntities() {
    }
    
    @Test
    public void testCreateChartFromChartWrapper() 
    {
        Measurement r = makeNumericMeasurement();
        
        String name = r.getName();
        NumericData.Data data;
        try {
            data = NumericMeasurementUtils.getNumericData(r).get().getData().get(0);
        } catch (InterruptedException ex) {
            throw new OvationException(ex.getLocalizedMessage());
        } catch (ExecutionException ex) {
            throw new OvationException(ex.getLocalizedMessage());
        }
        double samplingRate = data.samplingRates[0];
        String samplingUnit = data.samplingRateUnits[0];
        String units = data.units;
        double[] d = (double[])data.dataArray.get1DJavaArray(Double.class);
        
        Collection entities = Sets.newHashSet(r);
        
        ChartGroupWrapper cw = (ChartGroupWrapper)ResponseWrapperFactory.create(r).createVisualization(r);
        DefaultXYDataset ds = cw.getDataset();
        ChartPanel p = cw.generateChartPanel();
        XYPlot plot = p.getChart().getXYPlot();
        Comparable key = ds.getSeriesKey(0);
        assertEquals(key, name);
        for (int i = 0; i < d.length; ++i) {
            assertTrue(d[i] == ds.getYValue(0, i));
            assertTrue(i / samplingRate == ds.getXValue(0, i));
        }

        assertEquals(plot.getDomainAxis().getLabel(), ChartGroupWrapper.convertSamplingRateUnitsToGraphUnits(samplingUnit));
        assertEquals(plot.getRangeAxis().getLabel(), units);
    }
    
    @Test
    public void testGraphsMultipleSelectedEntitiesWithSharedUnits()
    {
        Collection entities = new HashSet();
        NumericData nd1 = new NumericData();
        String units = "units";
        String dimensionLabel = "dimension label";
        double samplingRate = 3;
        String samplingRateUnits = "Hz";

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
        //assertTrue(series.contains(dev1.getName()));
        //assertTrue(series.contains(dev2.getName()));
    }
    
    @Test
    public void testGraphsMultipleSelectedEntitiesWithoutSharedUnits()
    {
        /*ResponseViewTopComponent t = new ResponseViewTopComponent();
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
        * 
        */
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
