/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.util.concurrent.ExecutionException;
import org.jfree.data.xy.DefaultXYDataset;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.NumericMeasurementUtils;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.values.NumericData;
import us.physion.ovation.values.NumericData.Data;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author jackie
 */
public class ChartVisualizationFactory implements VisualizationFactory{

   @Override
    public Visualization createVisualization(Measurement r) {
       
        if (!NumericMeasurementUtils.isNumericMeasurement(r))
        {
            throw new OvationException("Can only plot Numeric data with the ChartVisualization plugin");
        }
        NumericData data;
            try {
                data = NumericMeasurementUtils.getNumericData(r).get();
            } catch (InterruptedException ex) {
                throw new OvationException(ex.getMessage());
            } catch (ExecutionException ex) {
                throw new OvationException(ex.getMessage());
            }
        
        ChartGroupWrapper g = new ChartGroupWrapper(new DefaultXYDataset(), data);
        g.setTitle(r.getName());
        
        for (NumericData.Data d : data.getData().values())
        {
            g.addXYDataset(d);
        }
        return g;
    }

    @Override
    public int getPreferenceForDataContainer(Measurement r) {
        if (NumericMeasurementUtils.isNumericMeasurement(r))
        {
            try {
                if (NumericMeasurementUtils.getNumericData(r).get().getData().size() == 1);
            } catch (InterruptedException ex) {
                return -1;
            } catch (ExecutionException ex) {
                return -1;
            }
                return 100;
        }
        return -1;
    }
}
