/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

/**
 *
 * @author huecotanks
 */
public class ConnectionListener implements PropertyChangeListener{

    private Runnable toRun;
    
    public ConnectionListener(Runnable r)
    {
        toRun = r;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals("ovation.connectionChanged"))
        {
            toRun.run();
        }
    }
    
}
