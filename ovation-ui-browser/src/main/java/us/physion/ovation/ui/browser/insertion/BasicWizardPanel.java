/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 *
 * @author huecotanks
 */
abstract public class BasicWizardPanel implements WizardDescriptor.Panel<WizardDescriptor>{
    
     protected final ChangeSupport changeSupport = new ChangeSupport(this);
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    protected Component component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    /*@Override
    abstract public JPanel getComponent() {
        throw new UnsupportedOperationException("Subclasses must override this method");
    }*/

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
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    /*@Override
    public void readSettings(WizardDescriptor wiz) {
        // use wiz.getProperty to retrieve previous panel state
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
    }*/

    
    static String updateTextField(JTextField f, ChangeSupport cs, String value){
        if (f.getText().isEmpty())
        {
            value = "";
            cs.fireChange();
        }else{
            boolean notify = false;
            if (value == null || value.isEmpty())
            {
                notify = true;
            }
            value = f.getText();
            if (notify)
            {
                cs.fireChange();
            }
        }
        return value;
    }
    
    static String updateTextArea(JTextArea f, ChangeSupport cs, String value){
        if (f.getText().isEmpty())
        {
            value = "";
            cs.fireChange();
        }else{
            boolean notify = false;
            if (value == null || value.isEmpty())
            {
                notify = true;
            }
            value = f.getText();
            if (notify)
            {
                cs.fireChange();
            }
        }
        return value;
    }
}
