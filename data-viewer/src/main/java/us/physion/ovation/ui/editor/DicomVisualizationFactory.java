package us.physion.ovation.ui.editor;

import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.mixin.DataElement;

@ServiceProvider(service = VisualizationFactory.class)
public class DicomVisualizationFactory implements VisualizationFactory{

    @Override
    public int getPreferenceForDataContainer(DataElement r) {
        if (r.getDataContentType().equals("application/dicom"))
        {
            return 100;
        }
        return -1;
    }

    @Override
    public DataVisualization createVisualization(DataElement r) {
        return new DicomWrapper(r);
    }

}
