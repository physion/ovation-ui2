/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class InsertEpochGroupWizardPanel2 extends BasicWizardPanel {

    
    @Override
    public JPanel getComponent() {
        if (component == null) {
            component = new InsertEpochGroupVisualPanel2(changeSupport);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        InsertEpochGroupVisualPanel2 c = (InsertEpochGroupVisualPanel2)component;
        return c.getStart() != null && c.getLabel() != null && !c.getLabel().isEmpty();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        InsertEpochGroupVisualPanel2 c = (InsertEpochGroupVisualPanel2)component;
        wiz.putProperty("epochGroup.start", c.getStart());
        wiz.putProperty("epochGroup.label", c.getLabel());
        //wiz.putProperty("epochGroup.protocolParameters", c.getProtocolParameters());
        //wiz.putProperty("epochGroup.deviceParameters", c.getDeviceParameters());
    }
}
