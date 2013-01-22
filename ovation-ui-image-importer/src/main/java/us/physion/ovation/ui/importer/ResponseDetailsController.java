/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.awt.Component;
import java.util.Map;
import org.openide.WizardDescriptor;
import us.physion.ovation.ui.interfaces.BasicWizardPanel;

/**
 *
 * @author jackie
 */
public class ResponseDetailsController extends BasicWizardPanel{

    String responseName;
    int responseCount;
    public ResponseDetailsController(int epochCount, int responseCount)
    {
        super();
        responseName = "epoch" + epochCount + ".response" + responseCount;
        this.responseCount = responseCount;
    }
    
    @Override
    public Component getComponent() {
        if (component == null)
        {
            component = new ResponseDetailsPanel(changeSupport, responseCount);
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data)
    {
        ResponseDetailsPanel c = (ResponseDetailsPanel)getComponent();

        c.setURL((String)data.getProperty(responseName + ".url"));
        c.setUTI((String)data.getProperty(responseName + ".uti"));
        c.setUnits((String)data.getProperty(responseName + ".units"));
        c.setSamplingRateUnits((String[])data.getProperty(responseName + ".samplingRateUnits"));
        c.setDimensionLabels((String[])data.getProperty(responseName + ".dimensionLabels"));
        c.setSamplingRates((double[])data.getProperty(responseName + ".samplingRates"));
        c.setShape((long[])data.getProperty(responseName + ".shape"));

    }
    
    @Override
    public void storeSettings(WizardDescriptor data) {
        ResponseDetailsPanel c = (ResponseDetailsPanel)getComponent();
        
        data.putProperty(responseName + ".url", c.getURL());
        data.putProperty(responseName + ".uti", c.getUTI());
        data.putProperty(responseName + ".samplingRateUnits", c.getSamplingRateUnits());
        data.putProperty(responseName + ".dimensionLabels", c.getDimensionLabels());
        data.putProperty(responseName + ".samplingRates", c.getSamplingRates());
        data.putProperty(responseName + ".shape", c.getShape());
    }

    @Override
    public boolean isValid() {
        ResponseDetailsPanel c = (ResponseDetailsPanel)getComponent();

        return  (c.getURL() != null && !c.getURL().isEmpty() &&
                c.getUTI() != null && !c.getUTI().isEmpty() &&
                c.getSamplingRateUnits() != null && c.getSamplingRateUnits().length !=0 &&
                c.getSamplingRates() != null && c.getSamplingRates().length !=0 &&
                c.getDimensionLabels() != null && c.getDimensionLabels().length !=0 &&
                c.getShape() != null && c.getShape().length !=0);
    }
}
