/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.util.HashSet;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.Measurement;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author jackie
 */
public class TabularDataVisualizationFactory implements VisualizationFactory {

    Set<String> mimeTypes;

    public TabularDataVisualizationFactory()
    {
        mimeTypes = new HashSet<String>();
        mimeTypes.add("application/vnd.ms-excel");
        mimeTypes.add("text/comma-separated-values");
    }
    
    @Override
    public Visualization createVisualization(Measurement r) {
        return new TabularDataWrapper(r);
    }

    @Override
    public int getPreferenceForDataContainer(Measurement r) {
        if (mimeTypes.contains(r.getMimeType()))
        {
            return 100;
        }
        return -1;
    }
    
}
