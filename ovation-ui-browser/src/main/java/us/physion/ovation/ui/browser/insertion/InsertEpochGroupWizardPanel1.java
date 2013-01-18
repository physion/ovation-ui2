/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import ovation.Source;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

public class InsertEpochGroupWizardPanel1 extends BasicWizardPanel {

    private IEntityWrapper existingSource;
   InsertEpochGroupWizardPanel1(IEntityWrapper source)
   {
       existingSource = source;
   }
    @Override
    public JPanel getComponent() {
        if (component == null) {
            component = new SourceSelector(changeSupport, existingSource);
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
        /*IEntityWrapper src = ((SourceSelector)component).getSource();
        if (src != null && src.getType().equals(Source.class))
            return true;
        return false;*/
    }


    @Override
    public void storeSettings(WizardDescriptor wiz) {
        IEntityWrapper source = ((SourceSelector)component).getSource();
        if (source == null)
            source = existingSource;
        wiz.putProperty("epochGroup.source", source);
    }
}
