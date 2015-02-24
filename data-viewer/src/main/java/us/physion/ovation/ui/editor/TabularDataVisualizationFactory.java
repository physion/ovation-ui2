package us.physion.ovation.ui.editor;

import java.util.HashSet;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.mixin.Content;

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
        mimeTypes.add("application/csv");
        mimeTypes.add("text/csv");
    }
    
    @Override
    public DataVisualization createVisualization(Content r) {
        return new TabularDataWrapper(r);
    }

    @Override
    public int getPreferenceForDataContentType(String contentType) {
        if (mimeTypes.contains(contentType))
        {
            return 100;
        }
        return -1;
    }
}
