/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.Component;
import javax.swing.JPanel;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
class InsertSourceWizardPanel1 extends BasicWizardPanel
{
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new InsertSourceVisualPanel1(changeSupport);
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
        String label = ((InsertSourceVisualPanel1)component).getLabel();
        String identifier = ((InsertSourceVisualPanel1)component).getIdentifier();
        return (label != null && !label.isEmpty()) &&
                (identifier != null && !identifier.isEmpty()) ;
    }


    @Override
    public void storeSettings(WizardDescriptor wiz) {
        String label = ((InsertSourceVisualPanel1)component).getLabel();
        wiz.putProperty("source.label", label);
        
        String id = ((InsertSourceVisualPanel1)component).getIdentifier();
        wiz.putProperty("source.identifier", id);
    }

    @Override
    public void readSettings(WizardDescriptor data) {
    }
}
