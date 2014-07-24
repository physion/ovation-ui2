package us.physion.ovation.ui.browser;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.LazyChildren;

@Messages({
    "# {0} - parent display name",
    "Loading_Entity_Children=Loading data for {0}",
    "Loading_Epochs=Loading epochs",
    "Loading_Epochs_Done=Done loading epochs"
})
public class EntityChildren extends Children.Keys<EntityWrapper> implements LazyChildren {
    protected final TreeFilter filter;
    private boolean loadedKeys = false;

    public EntityChildren(List<EntityWrapper> children) {
        this(children, TreeFilter.NO_FILTER);
    }

    public EntityChildren(List<EntityWrapper> children, TreeFilter filter) {
        this.filter = filter;
        updateWithKeys(children == null ? Lists.<EntityWrapper>newArrayList() : children);
    }

    @Override
    public boolean isLoaded() {
        return super.isInitialized() && loadedKeys;
    }

    @Override
    protected Node[] createNodes(final EntityWrapper key) {
        return new Node[]{
            EntityWrapperUtilities.createNode(key,
            new EntityChildrenChildFactory(key, filter)
            )};
    }

    protected final ListenableFuture<Void> updateWithKeys(final List<EntityWrapper> list) {
        return EventQueueUtilities.runOnEDT(new Runnable() {
            @Override
            public void run() {
                setKeys(list);
                //addNotify();
                //refresh();
                loadedKeys = true;
            }
        });
    }
}
