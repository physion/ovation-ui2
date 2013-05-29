/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.awt.Component;
import java.util.*;
import javax.swing.JPanel;
import org.openide.WizardDescriptor;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.browser.insertion.BasicWizardPanel;
import us.physion.ovation.ui.browser.insertion.SourceSelector;

/**
 *
 * @author jackie
 */
public class MeasurementSourceNamesController extends BasicWizardPanel{
    int epochNumber;
    int measurementNumber;
    MeasurementSourceNamesController(int epochNum, int measurementNum)
    {
        super();
        this.epochNumber = epochNum;
        this.measurementNumber = measurementNum;
    }
    
    @Override
    public JPanel getComponent() {
        if (component == null) {
            component = new ListSelectionPanel(changeSupport, 
                    "Select sources for this measurement", 
                    "Epoch " + (epochNumber+1) + " Measurement " + (measurementNumber +1) + ": Select Sources");
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        Map<String, Source> sources = (Map<String, Source>)data.getProperty("sources");
        if (sources == null)
            sources = new HashMap();
        ArrayList l = new ArrayList(sources.keySet());
        Collections.sort(l);
        ListSelectionPanel c = (ListSelectionPanel)getComponent();
        
        //pull selected names, if previously selected
        List<Map<String, Object>> epochs = (List<Map<String, Object>>)data.getProperty("epochs");
        Map<String, Object> epoch = epochs.get(epochNumber);
        List<Map<String, Object>> measurements = (List<Map<String, Object>>)epoch.get("measurements");
        Map<String, Object> m = measurements.get(measurementNumber);
        c.setNames(l, (Collection<String>)m.get("sourceNames"));
    }
    @Override
    public void storeSettings(WizardDescriptor data) {
                
        List<Map<String, Object>> epochs = (List<Map<String, Object>>)data.getProperty("epochs");
        Map<String, Object> epoch = epochs.remove(epochNumber);
        List<Map<String, Object>> measurements = (List<Map<String, Object>>)epoch.get("measurements");
        Map<String, Object> m = measurements.remove(measurementNumber);
        
        ListSelectionPanel c = (ListSelectionPanel)getComponent();
        
        m.put("sourceNames", c.getNames());
       
        measurements.add(measurementNumber, m);
        epoch.put("measurements", measurements);
        epochs.add(epochNumber, epoch);
        
        data.putProperty("epochs", epochs);
      
    }

    @Override
    public boolean isValid() {
      return true;
    }
    
}
