/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import us.physion.ovation.domain.Measurement;


/**
 *
 * @author huecotanks
 */
public interface VisualizationFactory {
    public Visualization createVisualization(Measurement r);
    public int getPreferenceForDataContainer(Measurement r);
}
