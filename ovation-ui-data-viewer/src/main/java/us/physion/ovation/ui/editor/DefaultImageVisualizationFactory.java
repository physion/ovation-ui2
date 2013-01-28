/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import javax.imageio.ImageIO;
import org.openide.util.lookup.ServiceProvider;
import ovation.Response;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author jackie
 */
public class DefaultImageVisualizationFactory implements VisualizationFactory{

    @Override
    public Visualization createVisualization(Response r) {
        return new DefaultImageWrapper(r);
    }


    @Override
    public int getPreferenceForDataContainer(Response r) {
        String lowercaseUTI = r.getUTI().toLowerCase();
        for (String name : ImageIO.getReaderFormatNames()) {
            if (lowercaseUTI.contains(name.toLowerCase())) {
                return 100;
            }
        }
        return -1;
    }
    
}
