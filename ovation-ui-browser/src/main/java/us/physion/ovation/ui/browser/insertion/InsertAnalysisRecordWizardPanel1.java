/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.util.Iterator;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class InsertAnalysisRecordWizardPanel1 extends BasicWizardPanel {
// Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public InsertAnalysisRecordVisualPanel1 getComponent() {
        if (component == null) {
            component = new InsertAnalysisRecordVisualPanel1(changeSupport);
        }
        return (InsertAnalysisRecordVisualPanel1)component;
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
        InsertAnalysisRecordVisualPanel1 c = (InsertAnalysisRecordVisualPanel1)component;
        if (c != null)
        {
           return (c.getAnalysisRecordName() != null && !c.getAnalysisRecordName().isEmpty() 
                   && c.getEpochs() != null && c.getEpochs().hasNext() );
        }
        return false;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }
    
    @Override
    public void storeSettings(WizardDescriptor wiz) {
        InsertAnalysisRecordVisualPanel1 c = (InsertAnalysisRecordVisualPanel1)component;
        wiz.putProperty("analysisRecord.name", c.getAnalysisRecordName());
        wiz.putProperty("analysisRecord.epochs", c.getEpochs());
        // use wiz.putProperty to remember current panel state
    }
}
