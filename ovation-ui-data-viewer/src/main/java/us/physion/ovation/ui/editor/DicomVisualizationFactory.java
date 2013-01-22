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
public class DicomVisualizationFactory implements VisualizationFactory{

    @Override
    public int getPreferenceForDataContainer(Response r) {
        if (r.getUTI().equals("org.nema.dicom"))
        {
            return 100;
        }
        return -1;
    }
    
    @Override
    public Visualization createVisualization(Response r) {
        return new DicomWrapper(r);
    }
    
}
