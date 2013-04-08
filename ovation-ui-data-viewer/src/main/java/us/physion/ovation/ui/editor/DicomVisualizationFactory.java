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
public class DicomVisualizationFactory implements VisualizationFactory{

    @Override
    public int getPreferenceForDataContainer(Measurement r) {
        if (r.getDataContentType().equals("application/dicom"))
        {
            return 100;
        }
        return -1;
    }
    
    @Override
    public Visualization createVisualization(Measurement r) {
        return new DicomWrapper(r);
    }
    
}
