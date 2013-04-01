/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.awt.Component;
import us.physion.ovation.domain.Measurement;

/**
 *
 * @author huecotanks
 */
public interface Visualization {
    public Component generatePanel();
    
    public boolean shouldAdd(Measurement r);
    
    public void add(Measurement r);
}
