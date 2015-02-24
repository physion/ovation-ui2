package us.physion.ovation.ui.editor;

import javax.imageio.ImageIO;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.Resource;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author jackie
 */
public class DefaultImageVisualizationFactory implements VisualizationFactory{

    @Override
    public DataVisualization createVisualization(Resource r) {
        return new DefaultImageWrapper(r);
    }


    @Override
    public int getPreferenceForDataContentType(String contentType) {
        String lowercaseUTI = contentType.toLowerCase();
        for (String name : ImageIO.getReaderFormatNames()) {
            if (lowercaseUTI.contains(name.toLowerCase())) {
                return 100;
            }
        }
        return -1;
    }

}
