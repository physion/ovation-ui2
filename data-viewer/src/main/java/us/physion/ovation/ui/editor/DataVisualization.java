/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import javax.swing.JComponent;
import us.physion.ovation.domain.mixin.DataElement;

/**
 *
 * @author huecotanks
 */
public interface DataVisualization extends Visualization {
    boolean shouldAdd(DataElement r);

    void add(DataElement r);

    /* @Nullable */
    JComponent generateInfoPanel();
}
