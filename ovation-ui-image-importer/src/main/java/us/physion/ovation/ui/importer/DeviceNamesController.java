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
public class DeviceNamesController extends BasicWizardPanel {

    String responseName;
    int responseCount;

    public DeviceNamesController(int epochCount, int responseCount) {
        super();
        if (epochCount <0)
            responseName = "response";
        else
            responseName = "epoch" + epochCount + ".response" + responseCount;
        this.responseCount = responseCount;
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new KeyValuePanel(changeSupport, "name", "description");
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
       
    }

    @Override
    public void storeSettings(WizardDescriptor data) {
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
