/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import org.openide.WizardDescriptor;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.Source;

/**
 *
 * @author huecotanks
 */
public class DataElementController extends BasicWizardPanel{
   
    DataElementController()
   {
       super();
   }
   
   @Override
    public Component getComponent() {
        if (component == null) {
            component = new MeasurementPanel(changeSupport);
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
    }
    
    @Override
    public void storeSettings(WizardDescriptor data) {
        MeasurementPanel p = (MeasurementPanel)component;
        data.putProperty("dataElement.file", p.getFile());
        data.putProperty("dataElement.contentType", p.getContentType());
        data.putProperty("dataElement.name", p.getMeasurementName());
    }

    @Override
    public boolean isValid() {
        MeasurementPanel p = (MeasurementPanel)component;
        return p.getFile() != null && !p.getContentType().isEmpty() && !p.getMeasurementName().isEmpty();
    }
}
