/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import org.openide.util.Exceptions;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.NumericMeasurement;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.values.NumericData;
import us.physion.ovation.values.NumericData.Data;

/**
 *
 * @author huecotanks
 */
public class ChartWrapper {
    NumericData data;
    String name;
    double samplingRate;
    String yunits;
    String xunits;
    
    public ChartWrapper(Measurement m)
    {
        if (m instanceof NumericMeasurement)
        {
            NumericMeasurement r = (NumericMeasurement)m;
            data = r.getNumericData();
            name = r.getName();
            Data d = data.getDataList().get(0);
            //samplingRate = data.getSamplingRate();//Numeric data should have samplingRate and units
            yunits = d.getUnits();
            //xunits = convertSamplingRateUnitsToGraphUnits(d.getUnits());
        } else {
            throw new OvationException("Only NumericMeasuements may be displayed as a chart!");
        }
    }
   
    protected static String convertSamplingRateUnitsToGraphUnits(String samplingRateUnits){
       if (samplingRateUnits.toLowerCase().contains("hz"))
       {
           String prefix = samplingRateUnits.substring(0, samplingRateUnits.toLowerCase().indexOf("hz"));
           return "Time (in " + prefix + "Seconds)";
       }
       else return ("1 / " + samplingRateUnits);
    }
    
    public String getXUnits()
    {
        return xunits;
    }
    
    public String getYUnits()
    {
        return yunits;
    }
    
    public double getSamplingRate()
    {
       return samplingRate; 
    }
    
    public String getName()
    {
        return name;
    }
    
    public NumericData getNumericData()
    {
        return data;
    }
}
