
package us.physion.ovation.ui.browser;

import java.util.Comparator;
import us.physion.ovation.domain.mixin.TimelineElement;

/**
 * Comparator for Ovation entities. Compares start time for time line elements,
 * or display name for all others.
 * @param <T>  entity type T
 */
public class EntityComparator<T extends EntityWrapper> implements Comparator<T>{

    @Override
    public int compare(T o1, T o2) {
        if(o1 instanceof TimelineElement && o2 instanceof TimelineElement) {
            TimelineElement t1 = (TimelineElement)o1;
            TimelineElement t2 = (TimelineElement) o2;
            return t1.getStart().compareTo(t2.getStart());
        }
        
        return o1.getEntity(true).getURI().compareTo(o2.getEntity(true).getURI());
        //return o1.getDisplayName().toLowerCase().compareTo(o2.getDisplayName().toLowerCase());
    }
    
}
