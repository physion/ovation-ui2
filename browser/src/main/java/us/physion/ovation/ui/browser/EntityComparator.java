package us.physion.ovation.ui.browser;

import java.util.Comparator;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.mixin.TimelineElement;

/**
 * Comparator for Ovation entities. Compares start time for time line elements,
 * or display name for all others.
 *
 * @param <T> entity type T
 */
public class EntityComparator<T extends EntityWrapper> implements Comparator<T> {

    @Override
    public int compare(T o1, T o2) {
        //put the empty one last!
        if (o1 == EntityWrapper.EMPTY) {
            if (o2 == EntityWrapper.EMPTY) {
                return 0;
            } else {
                return 1;
            }
        } else if (o2 == EntityWrapper.EMPTY) {
            return -1;
        }

        
        final OvationEntity entity1 = o1.getEntity(true);
        final OvationEntity entity2 = o2.getEntity(true);

        if (entity1 instanceof Project && entity2 instanceof Project) {
            return ((Project) entity1).getName().toLowerCase().compareTo(((Project) entity2).getName().toLowerCase());
        }

        if (o1.getEntity() instanceof Source && o2.getEntity() instanceof Source) {
            return ((Source) entity1).getLabel().toLowerCase().compareTo(((Source) entity2).getLabel().toLowerCase());
        }

        if (o1.getEntity() instanceof Protocol && o2.getEntity() instanceof Protocol) {
            return ((Protocol) entity1).getName().toLowerCase().compareTo(((Protocol) entity2).getName().toLowerCase());
        }

        if (entity1 instanceof TimelineElement && entity2 instanceof TimelineElement) {
            TimelineElement t1 = (TimelineElement) entity1;
            TimelineElement t2 = (TimelineElement) entity2;
            return t1.getStart().compareTo(t2.getStart());
        }

        if (entity1 instanceof Measurement && entity2 instanceof Measurement) {
            Measurement m1 = (Measurement) entity1;
            Measurement m2 = (Measurement) entity2;

            if (m1.getEpoch().equals(m2.getEpoch())) {
                return m1.getName().toLowerCase().compareTo(m2.getName().toLowerCase());
            }

            return ((Measurement) entity1).getEpoch().getStart().compareTo(((Measurement) entity2).getEpoch().getStart());
        }

        return entity1.getURI().compareTo(entity2.getURI());
    }

}
