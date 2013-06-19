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
public class MeasurementController extends BasicWizardPanel{
   
    Epoch epoch;
    MeasurementController(Epoch e)
   {
       super();
       epoch = e;
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
       data.putProperty("epoch", epoch);
    }
    
    @Override
    public void storeSettings(WizardDescriptor data) {
        MeasurementPanel p = (MeasurementPanel)component;
        data.putProperty("measurement.file", p.getFile());
        data.putProperty("measurement.contentType", p.getContentType());
        data.putProperty("measurement.name", p.getMeasurementName());
    }

    @Override
    public boolean isValid() {
        MeasurementPanel p = (MeasurementPanel)component;
        return p.getFile() != null && !p.getContentType().isEmpty() && !p.getMeasurementName().isEmpty();
    }
}
