/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.Component;
import org.openide.WizardDescriptor;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.mixin.ProcedureElement;

/**
 *
 * @author jackie
 */
public class EpochSelectionController extends BasicWizardPanel {

    @Override
    public boolean isValid() {
        EpochSelector c = (EpochSelector)component;
        return c.getProcedureElement() != null;
    }
    
    @Override
    public Component getComponent() {
        if (component == null)
        {
            component = new EpochSelector(changeSupport);
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data){}

    @Override
    public void storeSettings(WizardDescriptor data) {
        ProcedureElement e = ((EpochSelector)getComponent()).getProcedureElement();
        if (e instanceof Epoch)
            data.putProperty("epoch", e);
        else{
            data.putProperty("epoch.parent", e);
        }
    }
    
}
