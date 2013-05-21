/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.awt.Font;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.RectangleInsets;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.NumericMeasurementUtils;
import us.physion.ovation.values.NumericData;
import us.physion.ovation.values.NumericData.Data;
import ucar.ma2.DataType;

/**
 *
 * @author huecotanks
 */
class ChartGroupWrapper implements Visualization
{
    static Logger logger = LoggerFactory.getLogger(ChartGroupWrapper.class);
    DefaultXYDataset _ds;
    String _xAxis;
    String _yAxis;
    String _title;
    Map<String, Integer> dsCardinality;
    
    ChartGroupWrapper(DefaultXYDataset ds, NumericData data)
    {
        NumericData.Data d = data.getDataList().get(0);
        String xAxis = convertSamplingRateUnitsToGraphUnits(d.getSamplingRateUnits()[0]);
        String yAxis = d.getUnits();
        
        _ds = ds;
        _xAxis = xAxis;
        _yAxis = yAxis;
        dsCardinality = new HashMap<String, Integer>();
    }
    DefaultXYDataset getDataset(){ return _ds;}
    String getXAxis() { return _xAxis;}
    String getYAxis() { return _yAxis;}
    void setTitle(String s) {_title = s;}
    String getTitle() {return _title;}
    
    ChartPanel generateChartPanel()
    {
        JFreeChart chart = ChartFactory.createXYLineChart(getTitle(), getXAxis(), getYAxis(), getDataset(), PlotOrientation.VERTICAL, true, true, true);
        ChartPanel p = new ChartPanel(chart);

        chart.setTitle(convertTitle(getTitle()));
        chart.setPadding(new RectangleInsets(20, 20, 20, 20));
        XYPlot plot = chart.getXYPlot();
        plot.getDomainAxis().setLabelFont(new Font("Times New Roman", 1, 15));//new Font("timesnewroman", Font.LAYOUT_LEFT_TO_RIGHT, 15));
        plot.getRangeAxis().setLabelFont(new Font("Times New Roman", 1, 15));//new Font("timesnewroman", Font.LAYOUT_LEFT_TO_RIGHT, 15));
        return p;
    }
    
    private TextTitle convertTitle(String s)
    {
        return new TextTitle(s, new Font("Times New Roman", 1, 20));
    }
    
    public JPanel generatePanel()
    {
        return generateChartPanel();
    }
    
    protected void addXYDataset(NumericData.Data d) {
        if (d == null) {
            return;
        }

        if (d.getShape().length != 1)
        {
            logger.debug("Shape is multidimensional!");
            return;
        }
        String datasetName = d.getName();
        double samplingRate = d.getSamplingRates()[0];

        int[] shape = d.getShape();
        long size = 1;
        for (int dimension = 0; dimension < shape.length; dimension++) {
            size = size * shape[dimension];
        }

        int existingSeries = _ds.indexOf(datasetName);
        int scale = 0;
        if (dsCardinality.containsKey(datasetName)) {
            scale = dsCardinality.get(datasetName);
        }
        String newName = datasetName + "-" + String.valueOf(scale + 1);

        if (d.getType().equals(DataType.DOUBLE)) {
            double[] floatingData = (double[])d.getData();
            double[][] data = new double[2][(int) size];

            if (scale >= 0) {
                for (int i = 0; i < (int) size; ++i) {
                    data[1][i] = (floatingData[i]);
                    data[0][i] = i /samplingRate;
                }
            } else {
                for (int i = 0; i < (int) size; ++i) {
                    data[1][i] = (floatingData[i] + _ds.getYValue(existingSeries, i) * scale) / (scale + 1);
                    data[0][i] = i / samplingRate;
                }
            }

            dsCardinality.put(datasetName, scale + 1);

            if (existingSeries >= 0) {
                _ds.addSeries(newName, data);

            } else {
                _ds.addSeries(datasetName, data);
            }

        } else if (d.getType().equals(DataType.INT)) {
            int[] integerData = (int[])d.getData();
            double[][] data = new double[(int) size][2];

            if (scale >= 0) {
                for (int i = 0; i < (int) size; ++i) {
                    data[1][i] = (integerData[i]);
                    data[0][i] = i / samplingRate;
                }
            } else {
                for (int i = 0; i < (int) size; ++i) {
                    data[1][i] = (integerData[i] + _ds.getYValue(existingSeries, i) * scale) / (scale + 1);
                    data[0][i] = i / samplingRate;
                }
            }
            dsCardinality.put(datasetName, scale + 1);

            if (existingSeries >= 0) {
                _ds.addSeries(newName, data);

            } else {
                _ds.addSeries(datasetName, data);
            }
        } else {
            logger.debug("NumericData object has unknown type: " + d.getType());
        }
    }

    @Override
    public boolean shouldAdd(Measurement r) {
        NumericData data;
        try {
            data = NumericMeasurementUtils.getNumericData(r).get();
        } catch (InterruptedException ex) {
            throw new OvationException(ex);
        } catch (ExecutionException ex) {
            throw new OvationException(ex);
        }
        if (data.getDataList().size() == 1) {
            NumericData.Data d = data.getDataList().get(0);
            return (d.getUnits().equals(_xAxis)
                    && d.getSamplingRateUnits()[0].equals(_yAxis));
        }
        return false;
    }

    @Override
    public void add(Measurement r) {
        String preface = "Aggregate responses: ";
        NumericData data;
        try{
            data = NumericMeasurementUtils.getNumericData(r).get();
        } catch (Exception e)
        {
            throw new OvationException(e.getLocalizedMessage());
        }
        for (NumericData.Data d : data.getDataList()) {
            addXYDataset(d);
            String name = "";
            if (getTitle().startsWith(preface)) {
                name = getTitle().substring(preface.length());
            } else {
                name = getTitle();
            }
            setTitle(preface + name + ", " + d.getName());
        }
    }

    protected static String convertSamplingRateUnitsToGraphUnits(String samplingRateUnits) {
        if (samplingRateUnits.toLowerCase().contains("hz")) {
            String prefix = samplingRateUnits.substring(0, samplingRateUnits.toLowerCase().indexOf("hz"));
            return "Time (in " + prefix + "Seconds)";
        } else {
            return ("1 / " + samplingRateUnits);
        }
    }
}