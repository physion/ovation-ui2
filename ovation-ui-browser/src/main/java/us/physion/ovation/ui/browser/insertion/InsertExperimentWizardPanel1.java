/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import javax.swing.JPanel;
import org.joda.time.DateTime;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;

/**
 *
 * @author huecotanks
 */
class InsertExperimentWizardPanel1 extends BasicWizardPanel {

    @Override
    public JPanel getComponent() {
        if (component == null) {
            component = new InsertExperimentVisualPanel1(changeSupport);
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
        String purpose = ((InsertExperimentVisualPanel1)component).getPurpose();
        DateTime start = ((InsertExperimentVisualPanel1)component).getStart();
        
        return purpose != null && !purpose.isEmpty() && start != null;
    }


    @Override
    public void storeSettings(WizardDescriptor wiz) {
        String purpose = ((InsertExperimentVisualPanel1)component).getPurpose();
        DateTime start = ((InsertExperimentVisualPanel1)component).getStart();
        wiz.putProperty("experiment.purpose", purpose);
        wiz.putProperty("experiment.start", start);
    }

    @Override
    public void readSettings(WizardDescriptor data) {
    }
}
