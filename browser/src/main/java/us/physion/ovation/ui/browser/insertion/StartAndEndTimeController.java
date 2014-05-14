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
public class StartAndEndTimeController extends BasicWizardPanel {

    String objectPrefix;
    public StartAndEndTimeController(String objectPrefix)
    {
        this.objectPrefix = objectPrefix;
    }

    @Override
    public Component getComponent() {
        if (component == null)
        {
            component = new StartAndEndPanel(true);
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
        changeSupport.fireChange();//let the InsertEntityIterator know to check the protocol and device info checkboxes
    }

    public boolean includeProtocolInfo()
    {
        StartAndEndPanel c = (StartAndEndPanel)getComponent();
        return c.includeProtocolInfo();
    }

    public boolean includeDeviceInfo()
    {
        StartAndEndPanel c = (StartAndEndPanel)getComponent();
        return c.includeDeviceInfo();
    }

}
