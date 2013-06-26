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
public class ProcedureElementController extends BasicWizardPanel {

    String objectPrefix;
    ProcedureElementController(String objectPrefix)
    {
        this.objectPrefix = objectPrefix;
    }
    @Override
    public boolean isValid() {
        ProcedureElementSelector c = (ProcedureElementSelector)component;
        return c.getProcedureElement() != null;
    }
    
    @Override
    public Component getComponent() {
        if (component == null)
        {
            component = new ProcedureElementSelector(changeSupport);
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data){
    }

    @Override
    public void storeSettings(WizardDescriptor data) {
        ProcedureElement e = ((ProcedureElementSelector)getComponent()).getProcedureElement();
        if (e == null)
            return;
        if (e instanceof Epoch)
            data.putProperty(objectPrefix + ".epoch", e);
        else{
            data.putProperty(objectPrefix + ".epochContainer", e);
        }
    }
    
}
