/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.util.Collection;
import org.openide.util.Lookup;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.mixin.DataElement;

/**
 *
 * @author huecotanks
 */
public class ResponseWrapperFactory {

    static Collection<? extends VisualizationFactory> dataVizFactories = Lookup.getDefault().lookupAll(VisualizationFactory.class);
    static Collection<? extends ContainerVisualizationFactory> containerVizFactories = Lookup.getDefault().lookupAll(ContainerVisualizationFactory.class);

    public static VisualizationFactory create(DataElement r) {
        int preference = 0;
        VisualizationFactory vis = null;
        for (VisualizationFactory f : dataVizFactories) {
            int factoryPref = f.getPreferenceForDataContainer(r);
            if (factoryPref > preference) {
                preference = factoryPref;
                vis = f;
            }
        }
        if (preference == 0) {
            return new DefaultVisualizationFactory();
        }
        return vis;
    }

    public static <T extends OvationEntity> ContainerVisualizationFactory create(T e) {
        int preference = 0;
        ContainerVisualizationFactory vis = null;
        for (ContainerVisualizationFactory f : containerVizFactories) {
            int factoryPref = f.getPreferenceForContainer(e);
            if (factoryPref > preference) {
                preference = factoryPref;
                vis = f;
            }
        }
        if (preference == 0) {
            return new DefaultContainerVisualizationFactory();
        }
        return vis;
    }
}
