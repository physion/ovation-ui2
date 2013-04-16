/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import javax.swing.JPanel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author jackie
 */
public class InsertEpochGroupWizardPanel4  extends BasicWizardPanel{
    @Override
    public JPanel getComponent() {
        if (component == null) {
            component = new ParameterCreator(changeSupport, "Add optional device parameters");
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
        return true;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        ParameterCreator c = (ParameterCreator)component;
        wiz.putProperty("epochGroup.deviceParameters", c.getParameters());
    }
}
