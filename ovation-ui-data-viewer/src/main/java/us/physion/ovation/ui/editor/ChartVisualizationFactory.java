/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import org.jfree.data.xy.DefaultXYDataset;
import org.openide.util.lookup.ServiceProvider;
import ovation.Response;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author jackie
 */
public class ChartVisualizationFactory implements VisualizationFactory{

   @Override
    public Visualization createVisualization(Response r) {
       ChartWrapper cw = new ChartWrapper(r);
        ChartGroupWrapper g = new ChartGroupWrapper(new DefaultXYDataset(), cw.xunits, cw.yunits);
        g.setTitle(cw.getName());
        g.addXYDataset(cw);
        return g;
    }

    @Override
    public int getPreferenceForDataContainer(Response r) {
        if (r.getUTI().equals(Response.NUMERIC_DATA_UTI) && r.getShape().length == 1)
        {
            return 100;
        }
        return -1;
    }
    
}
