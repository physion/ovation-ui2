/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.awt.Component;
import us.physion.ovation.domain.mixin.DataElement;

/**
 *
 * @author huecotanks
 */
public interface Visualization {
    public Component generatePanel();
    
    public boolean shouldAdd(DataElement r);
    
    public void add(DataElement r);
}
