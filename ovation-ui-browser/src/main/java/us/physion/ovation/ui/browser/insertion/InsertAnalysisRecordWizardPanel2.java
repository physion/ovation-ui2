/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class InsertAnalysisRecordWizardPanel2 extends BasicWizardPanel {

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public JPanel getComponent() {
        if (component == null) {
            component = new InsertAnalysisRecordVisualPanel2(changeSupport);
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
        InsertAnalysisRecordVisualPanel2 c = (InsertAnalysisRecordVisualPanel2)component;
        wiz.putProperty("analysisRecord.inputs", c.getInputs());
        // use wiz.putProperty to remember current panel state
    }
}
