package us.physion.ovation.ui.editor;

import com.google.common.collect.Sets;
import javax.swing.JComponent;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Resource;

/**
 *
 * @author huecotanks
 */
public class DefaultVisualizationFactory implements VisualizationFactory
{
    @Override
    public DataVisualization createVisualization(Resource r) {
        return new DefaultVisualization(r);
    }

    @Override
    public int getPreferenceForDataContainer(Resource r) {
        return 2;
    }

    class DefaultVisualization extends AbstractDataVisualization    {
        final Resource data;
        DefaultVisualization(Resource d)
        {
            data = d;
        }

        @Override
        public JComponent generatePanel() {
            return new DefaultDataPanel(data);
        }

        @Override
        public boolean shouldAdd(Resource r) {
            return false;
        }

        @Override
        public void add(Resource r) {
            throw new UnsupportedOperationException("Create a new Visualization, rather than adding to an existing one");
        }

        @Override
        public Iterable<? extends OvationEntity> getEntities() {
            return Sets.newHashSet(data);
        }

    }

}
