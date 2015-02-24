package us.physion.ovation.ui.editor;

import us.physion.ovation.domain.mixin.Content;

/**
 *
 * @author huecotanks
 */
public interface VisualizationFactory {
    public DataVisualization createVisualization(Content r);

    public int getPreferenceForDataContentType(String contentType);
}
