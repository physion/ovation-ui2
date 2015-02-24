package us.physion.ovation.ui.editor;

import org.jfree.data.xy.DefaultXYDataset;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.NumericDataElements;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.values.NumericData;

import java.util.concurrent.ExecutionException;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author jackie
 */
public class ChartVisualizationFactory implements VisualizationFactory {

    @Override
    public DataVisualization createVisualization(Resource r) {

        if (!NumericDataElements.isNumeric(r)) {
            throw new OvationException("Can only plot Numeric data with the ChartVisualization plugin");
        }
        NumericData data;
        try {
            data = NumericDataElements.getNumericData(r).get();
        } catch (InterruptedException ex) {
            throw new OvationException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw new OvationException(ex.getMessage());
        }

        ChartGroupWrapper g = new ChartGroupWrapper(new DefaultXYDataset(), data, r);
        g.setTitle(r.getName());

        for (NumericData.Data d : data.getData().values()) {
            g.addXYDataset(d);
        }
        return g;
    }

    @Override
    public int getPreferenceForDataContentType(String contentType) {
        if (contentType.equals(NumericDataElements.NUMERIC_MEASUREMENT_CONTENT_TYPE)) {
            return 100;
        }
        return -1;
    }
}
