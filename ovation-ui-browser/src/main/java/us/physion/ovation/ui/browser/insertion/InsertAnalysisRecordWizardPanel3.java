/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class InsertAnalysisRecordWizardPanel3 extends BasicWizardPanel {

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
        InsertAnalysisRecordVisualPanel3 c = (InsertAnalysisRecordVisualPanel3)component;
        if (c != null)
        {
           return (c.getEntryFunction() != null && !c.getEntryFunction().isEmpty() 
                   && c.getParameters() != null && !c.getParameters().isEmpty()
                   && c.getScmURL() != null && !c.getScmURL().isEmpty()
                   && c.getScmRevision() != null && !c.getScmRevision().isEmpty());
        }
        return false;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        InsertAnalysisRecordVisualPanel3 c = (InsertAnalysisRecordVisualPanel3)component;
        wiz.putProperty("analysisRecord.entryFn", c.getEntryFunction());
        wiz.putProperty("analysisRecord.parameters", c.getParameters());
        wiz.putProperty("analysisRecord.scmURL", c.getScmURL());
        wiz.putProperty("analysisRecord.scmRevision", c.getScmRevision());
        // use wiz.putProperty to remember current panel state
    }
}
