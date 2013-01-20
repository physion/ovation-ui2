/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 *
 * @author huecotanks
 */
public class DummyChangeListener implements ChangeListener{
    ChangeSupport cs;
    boolean stateChanged = false;

    DummyChangeListener() {
        cs = new ChangeSupport(this);
        cs.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        stateChanged = true;
    }

    boolean getStateChanged() {
        return stateChanged;
    }
    void resetStateChanged()
    {
        stateChanged = false;
    }

    ChangeSupport getChangeSupport() {
        return cs;
    }
}
