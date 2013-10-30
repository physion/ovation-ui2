package us.physion.ovation.ui.editor;

import javax.imageio.ImageIO;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.mixin.DataElement;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author jackie
 */
public class DefaultImageVisualizationFactory implements VisualizationFactory{

    @Override
    public Visualization createVisualization(DataElement r) {
        return new DefaultImageWrapper(r);
    }


    @Override
    public int getPreferenceForDataContainer(DataElement r) {
        String lowercaseUTI = r.getDataContentType().toLowerCase();
        for (String name : ImageIO.getReaderFormatNames()) {
            if (lowercaseUTI.contains(name.toLowerCase())) {
                return 100;
            }
        }
        return -1;
    }
    
}
