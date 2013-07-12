/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author jackie
 */
class NotesPropertyListener implements PropertyChangeListener {

    private NotesTableRenderer renderer;
    public NotesPropertyListener(NotesTableRenderer r) {
        renderer = r;
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        
        if (pce.getPropertyName().equals("delete"))
        {
            renderer.delete();
        }
    }
    
}
