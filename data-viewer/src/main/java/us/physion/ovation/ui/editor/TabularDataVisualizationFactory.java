package us.physion.ovation.ui.editor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.mixin.Content;
import us.physion.ovation.loader.TabularService;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author jackie
 */
public class TabularDataVisualizationFactory implements VisualizationFactory {

    private final static Set<String> mimeTypes;

    public TabularDataVisualizationFactory()
    {
    }
    
    static {
        mimeTypes = new HashSet<String>();
        mimeTypes.add("application/vnd.ms-excel");
        mimeTypes.add("text/comma-separated-values");
        mimeTypes.add("application/csv");
        mimeTypes.add("text/csv");
    }
    
    private static boolean isTabularMimeType(String mimetype) {
        return mimeTypes.contains(mimetype);
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

    @ServiceProvider(service = TabularService.class)
    public static class Loader extends TabularService {

        @Override
        public String[][] read(File f) throws IOException {
            try {
                String contentType = ContentTypes.getContentType(f);
                if (!TabularDataVisualizationFactory.isTabularMimeType(contentType)) {
                    return null;
                }
            } catch (IOException ex) {
                return null;
            }

            TabularData data = TabularDataWrapper.load(f);
            if (data != null) {
                return data.getRawData();
            } else {
                return null;
            }
        }
    }

}
