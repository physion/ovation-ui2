package us.physion.ovation.ui.editor;

import java.awt.Component;
import us.physion.ovation.domain.mixin.DataElement;

/**
 *
 * @author huecotanks
 */
public class DefaultVisualizationFactory implements VisualizationFactory
{
    @Override
    public DataVisualization createVisualization(DataElement r) {
        return new DefaultVisualization(r);
    }

    @Override
    public int getPreferenceForDataContainer(DataElement r) {
        return 2;
    }
    
    class DefaultVisualization implements DataVisualization
    {
        DataElement data;
        DefaultVisualization(DataElement d)
        {
            data =d;
        }
        
        @Override
        public Component generatePanel() {
            return new DefaultDataPanel(data);
        }

        @Override
        public boolean shouldAdd(DataElement r) {
            return false;
        }

        @Override
        public void add(DataElement r) {
            throw new UnsupportedOperationException("Create a new Visualization, rather than adding to an existing one"); 
        }
        
    }
    
}
