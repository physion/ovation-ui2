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
public class NameWizard extends BasicWizardPanel{

    String objectPrefix;
    NameWizard(String prefix)
    {
        objectPrefix = prefix;
    }
    
    @Override
    public Component getComponent() {
        if (component == null){
            component = new NamePanel(changeSupport);
        }
        return component;
    }
    
    @Override
    public boolean isValid()
    {
        return !((NamePanel)getComponent()).getObjectName().isEmpty();
    }

    @Override
    public void readSettings(WizardDescriptor data) {
    }

    @Override
    public void storeSettings(WizardDescriptor data) {
        data.putProperty(objectPrefix + ".name", ((NamePanel)getComponent()).getObjectName());
    }
    
}
