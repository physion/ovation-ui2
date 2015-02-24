/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.ui.interfaces.IEntityNode;

/**
 *
 * @author huecotanks
 */
public class ResponseWrapperFactory {

    static Collection<? extends VisualizationFactory> dataVizFactories = Lookup.getDefault().lookupAll(VisualizationFactory.class);
    static Collection<? extends ContainerVisualizationFactory> containerVizFactories = Lookup.getDefault().lookupAll(ContainerVisualizationFactory.class);

    static Logger logger = LoggerFactory.getLogger(ResponseWrapperFactory.class);

    static LoadingCache<String, VisualizationFactory> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<String, VisualizationFactory>() {
                @Override
                public VisualizationFactory load(String key) {
                    int preference = 0;
                    VisualizationFactory vis = null;

                    for (VisualizationFactory f : dataVizFactories) {
                        int factoryPref = f.getPreferenceForDataContentType(key);
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
            });
    
    public static VisualizationFactory create(Resource r) {
        String contentType = r.getDataContentType();
        VisualizationFactory vis;


        try {
            vis = cache.get(contentType);
        } catch (ExecutionException ex) {
            vis = new DefaultVisualizationFactory();
        }

        return vis;
    }

    public static ContainerVisualizationFactory create(IEntityNode n) {
        int preference = 0;
        ContainerVisualizationFactory vis = null;
        for (ContainerVisualizationFactory f : containerVizFactories) {
            int factoryPref = f.getPreferenceForContainer(n.getEntity());
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
