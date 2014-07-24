package us.physion.ovation.ui.browser;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

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

    @Override
    protected Node[] createNodes(final EntityWrapper key)
    {
        return new Node[]{EntityWrapperUtilities.createNewNode(key, Children.createLazy(getFilteredChildrenCallable(key)))};
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

    @Override
    protected EntityChildrenWrapperHelper createEntityChildrenWrapperHelper(TreeFilter filter) {
        return new FilteredEntityChildrenWrapperHelper(filter, classesToInclude) {

            @Override
            protected void displayUpdatedList(List<EntityWrapper> list, Comparator entityComparator) {
                FilteredEntityChildren.this.updatedList(list, entityComparator);
            }
            
        };
    }    
}
