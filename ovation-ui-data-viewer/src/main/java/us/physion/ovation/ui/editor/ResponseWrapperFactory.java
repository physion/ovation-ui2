/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.util.Collection;
import javax.imageio.ImageIO;
import org.openide.util.Lookup;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.mixin.DataElement;

/**
 *
 * @author huecotanks
 */
public class ResponseWrapperFactory {
    
    static Collection<? extends VisualizationFactory> factories = Lookup.getDefault().lookupAll(VisualizationFactory.class);
    public static VisualizationFactory create(DataElement r)
    {
        int preference = 0;
        VisualizationFactory vis = null;
        for (VisualizationFactory f : factories)
        {
            int factoryPref = f.getPreferenceForDataContainer(r);
            if (factoryPref > preference)
            {
                preference = factoryPref;
                vis = f;
            }
        }
        if (preference == 0)
        {
            return new DefaultVisualizationFactory();
        }
        return vis;
    }
}
