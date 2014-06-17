/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;

/**
 *
 * @author huecotanks
 */
public class FilteredEntityChildren extends EntityChildren {
    Iterable<Class> classesToInclude;
    public FilteredEntityChildren(EntityWrapper e, Iterable<Class> classesToInclude) {
        super(e);
        this.classesToInclude = classesToInclude;
    }

    public FilteredEntityChildren(List<EntityWrapper> childrenList, Iterable<Class> classesToInclude) {
        super(childrenList);
        this.classesToInclude = classesToInclude;
    }

    public static List<EntityWrapper> wrap(Iterable<? extends OvationEntity> entities)    {
        List<EntityWrapper> wrapped = Lists.newArrayList();
         for (OvationEntity entity : entities)
         {
             wrapped.add(new EntityWrapper(entity));
        }

        Collections.sort(wrapped, new EntityComparator());

         return wrapped;
    }

    @Override
    protected Node[] createNodes(final EntityWrapper key)
    {
        return new Node[]{EntityWrapperUtilities.createNewNode(key, Children.createLazy(getFilteredChildrenCallable(key)))};
    }

    @Override
    public List<EntityWrapper> createKeysForEntity(DataContext ctx, EntityWrapper ew)
    {
        List<EntityWrapper> all = super.createKeysForEntity(ctx, ew);
        List<EntityWrapper> filtered = new LinkedList();
        for(EntityWrapper child : all)
        {
            for (Class c : classesToInclude) {
                if (c.isAssignableFrom(child.getType())) {
                    filtered.add(child);
                    break;//break from for class in classesToInclude loop
                }
            }
        }

        return filtered;
    }

    protected Callable<Children> getFilteredChildrenCallable(final EntityWrapper parent)
    {
        return new Callable<Children>() {

            @Override
            public Children call() throws Exception {
                return new FilteredEntityChildren(parent, classesToInclude);
            }
        };
    }
}
