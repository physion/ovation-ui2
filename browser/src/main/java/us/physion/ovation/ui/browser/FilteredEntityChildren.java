package us.physion.ovation.ui.browser;

import java.util.List;
import org.openide.nodes.Node;

public class FilteredEntityChildren extends EntityChildren {
    Iterable<Class> classesToInclude;

    public FilteredEntityChildren(List<EntityWrapper> childrenList, Iterable<Class> classesToInclude) {
        super(childrenList);
        this.classesToInclude = classesToInclude;
    }

    @Override
    protected Node[] createNodes(final EntityWrapper key)
    {
        return new Node[]{
            EntityWrapperUtilities.createNewNode(key,
            new FilteredEntityChildrenChildFactory(key, filter, classesToInclude)
            )};
    }
}
