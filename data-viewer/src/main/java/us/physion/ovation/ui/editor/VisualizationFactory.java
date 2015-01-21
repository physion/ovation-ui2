package us.physion.ovation.ui.editor;

import us.physion.ovation.domain.Resource;

/**
 *
 * @author huecotanks
 */
public interface VisualizationFactory {
    public DataVisualization createVisualization(Resource r);

    public int getPreferenceForDataContainer(Resource r);
}
