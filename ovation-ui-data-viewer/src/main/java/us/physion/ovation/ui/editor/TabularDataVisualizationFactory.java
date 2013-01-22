/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.util.HashSet;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;
import ovation.Response;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author jackie
 */
public class TabularDataVisualizationFactory implements VisualizationFactory {

    Set<String> utis;

    public TabularDataVisualizationFactory()
    {
        utis = new HashSet<String>();
        utis.add("public.csv");
        utis.add("public.comma-separated-values-text");
        utis.add("dyn.age80g650");
    }
    
    @Override
    public Visualization createVisualization(Response r) {
        return new TabularDataWrapper(r);
    }

    @Override
    public int getPreferenceForDataContainer(Response r) {
        if (utis.contains(r.getUTI()))
        {
            return 100;
        }
        return -1;
    }
    
}
