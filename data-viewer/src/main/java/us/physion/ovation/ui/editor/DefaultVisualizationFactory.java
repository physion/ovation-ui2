package us.physion.ovation.ui.editor;

import com.google.common.collect.Sets;
import javax.swing.JComponent;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Resource;

public class DefaultVisualizationFactory implements VisualizationFactory {

//    static LoadingCache<Resource, DataVisualization> cache = CacheBuilder.newBuilder()
//            .maximumSize(100)
//            .build(new CacheLoader<Resource, DataVisualization>() {
//                @Override
//                public DataVisualization load(Resource key) {
//                    return new DefaultVisualization(key);
//                }
//            });

    @Override
    public DataVisualization createVisualization(Resource r) {
        
        return new DefaultVisualization(r);

    }

    @Override
    public int getPreferenceForDataContentType(String contentType) {
        return 2;
    }

    static class DefaultVisualization extends AbstractDataVisualization {

        final Resource data;
        private final JComponent panel;

        DefaultVisualization(Resource d) {
            data = d;
            panel = new DefaultDataPanel(data);
        }

        @Override
        public JComponent generatePanel() {
            return panel;
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
