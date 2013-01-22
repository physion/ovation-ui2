/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import ovation.Response;

/**
 *
 * @author huecotanks
 */
public interface VisualizationFactory {
    public Visualization createVisualization(Response r);
    public int getPreferenceForDataContainer(Response r);
}
