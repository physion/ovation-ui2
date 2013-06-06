/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.Component;
import org.openide.WizardDescriptor;

/**
 *
 * @author huecotanks
 */
public class StartAndEndTimeController extends BasicWizardPanel{

    String objectPrefix;
    public StartAndEndTimeController(String objectPrefix)
    {
        this.objectPrefix = objectPrefix;
    }
    
    @Override
    public Component getComponent() {
        if (component == null)
        {
            component = new StartAndEndPanel();
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
    }

    @Override
    public void storeSettings(WizardDescriptor data) {
        StartAndEndPanel c = (StartAndEndPanel)getComponent();
        data.putProperty(objectPrefix + ".start", c.getStart());
        data.putProperty(objectPrefix + ".end", c.getEnd());
    }
    
}
