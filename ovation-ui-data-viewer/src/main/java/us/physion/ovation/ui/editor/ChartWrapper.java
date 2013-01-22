/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.io.InputStream;
import ovation.NumericData;
import ovation.Response;
import ovation.URLResponse;


/**
 *
 * @author huecotanks
 */
public class ChartWrapper {
    NumericData data;
    InputStream dataStream;
    String name;
    double samplingRate;
    String yunits;
    String xunits;
    
    public ChartWrapper(Response r)
    {
        if (r instanceof URLResponse)
        {
            dataStream = ((URLResponse) r).getDataStream();
        }else{
            data = r.getData();
        }
        name = r.getExternalDevice().getName();
        samplingRate = r.getSamplingRates()[0];
        yunits = r.getUnits();
        xunits = convertSamplingRateUnitsToGraphUnits(r.getSamplingUnits()[0]);
        
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
    
    public InputStream getDataStream()
    {
        return dataStream;
    }
}
