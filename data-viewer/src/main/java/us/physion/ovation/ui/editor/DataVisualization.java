/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import javax.swing.JComponent;
import us.physion.ovation.domain.Resource;

/**
 *
 * @author huecotanks
 */
public interface DataVisualization extends Visualization {
    boolean shouldAdd(Resource r);

    void add(Resource r);

    /* @Nullable */
    JComponent generateInfoPanel();
}
