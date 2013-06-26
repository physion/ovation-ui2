/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.awt.Component;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.openide.WizardDescriptor;
import us.physion.ovation.ui.interfaces.BasicWizardPanel;

/**
 *
 * @author jackie
 */
public class MeasurementDetailsController extends BasicWizardPanel{

    int epochNumber;
    int responseNumber;
    public MeasurementDetailsController(int epochCount, int responseCount)
    {
        super();
        this.responseNumber = responseCount;
        this.epochNumber = epochCount;
    }
    
    @Override
    public Component getComponent() {
        if (component == null)
        {
            component = new MeasurementDetailsPanel(changeSupport, responseNumber);
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data)
    {
        List<Map<String, Object>> epochs = (List<Map<String, Object>>)data.getProperty("epochs");
        Map<String, Object> epoch = epochs.get(epochNumber);
        List<Map<String, Object>> measurements = (List<Map<String, Object>>)epoch.get("measurements");
        Map<String, Object> m = measurements.get(responseNumber);
        Map<String, Object> mProperties = (Map<String, Object>)m.get("properties");
        
        MeasurementDetailsPanel c = (MeasurementDetailsPanel)getComponent();

        String url = (String)m.get("url");
        String[] pieces= url.split("/");
        String filename = pieces[pieces.length-1];
        c.setMeasurementName(filename);
        c.setURL(((String)m.get("url")));
        c.setUTI((String)m.get("mimeType"));
        c.setUnits((String)m.get("units"));
        c.setSamplingRateUnits((String[])mProperties.get("samplingRateUnits"));
        c.setDimensionLabels((String[])mProperties.get("dimensionLabels"));
        c.setSamplingRates((double[])mProperties.get("samplingRates"));
        c.setShape((int[])mProperties.get("shape"));

    }
    
    @Override
    public void storeSettings(WizardDescriptor data) {
        
        List<Map<String, Object>> epochs = (List<Map<String, Object>>)data.getProperty("epochs");
        Map<String, Object> epoch = epochs.remove(epochNumber);
        List<Map<String, Object>> measurements = (List<Map<String, Object>>)epoch.get("measurements");
        Map<String, Object> m = measurements.remove(responseNumber);
        Map<String, Object> mProperties = (Map<String, Object>)m.get("properties");
        
        MeasurementDetailsPanel c = (MeasurementDetailsPanel)getComponent();
        
        m.put("name", c.getMeasurementName());
        m.put("url", c.getURL());
        m.put("mimeType", c.getMimeType());
        m.put("units", c.getUnits());
        mProperties.put("samplingRateUnits", c.getSamplingRateUnits());
        mProperties.put("dimensionLabels", c.getDimensionLabels());
        mProperties.put("samplingRates", c.getSamplingRates());
        mProperties.put("shape", c.getShape());
        
        m.put("properties", mProperties);
        measurements.add(responseNumber, m);
        epoch.put("measurements", measurements);
        epochs.add(epochNumber, epoch);
        
        data.putProperty("epochs", epochs);
    }

    @Override
    public boolean isValid() {
        MeasurementDetailsPanel c = (MeasurementDetailsPanel)getComponent();

        return  (c.getURL() != null && !c.getURL().isEmpty() &&
                c.getMimeType() != null && !c.getMimeType().isEmpty() &&
                c.getMeasurementName() != null && !c.getMeasurementName().isEmpty());
    }
}
