/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import javax.imageio.ImageIO;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.Measurement;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author jackie
 */
public class DefaultImageVisualizationFactory implements VisualizationFactory{

    @Override
    public Visualization createVisualization(Measurement r) {
        return new DefaultImageWrapper(r);
    }


    @Override
    public int getPreferenceForDataContainer(Measurement r) {
        String lowercaseUTI = r.getMimeType().toLowerCase();
        for (String name : ImageIO.getReaderFormatNames()) {
            if (lowercaseUTI.contains(name.toLowerCase())) {
                return 100;
            }
        }
        return -1;
    }
    
}
