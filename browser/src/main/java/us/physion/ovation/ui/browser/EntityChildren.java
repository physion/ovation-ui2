package us.physion.ovation.ui.browser;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.DataContext;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.LazyChildren;

@Messages({
    "# {0} - parent display name",
    "Loading_Entity_Children=Loading data for {0}",
    "Loading_Epochs=Loading epochs",
    "Loading_Epochs_Done=Done loading epochs"
})
public class EntityChildren extends Children.Keys<EntityWrapper> implements LazyChildren {

    EntityWrapper parent;
    boolean projectView;
    private final TreeFilter filter;
    private boolean loadedKeys = false;
    private int childcount = 0;
    private final int UPDATE_FACTOR = 1;

    protected boolean initKeysAfterFirstAddNotify = false;

    public EntityChildren(EntityWrapper e) {
        this(e, TreeFilter.NO_FILTER);
    }

    public EntityChildren(EntityWrapper e, TreeFilter filter) {
        if (e == null) {
            throw new OvationException("Pass in the list of Project/Source EntityWrappers, instead of null");
        }

        parent = e;
        this.filter = filter;
        //if its per user, we create
        if (e instanceof PerUserEntityWrapper) {
            loadedKeys = true;
            setKeys(((PerUserEntityWrapper) e).getChildren());
        } else {
            initKeysAfterFirstAddNotify = true;
        }
    }

    public EntityChildren(List<EntityWrapper> children) {
        this(children, TreeFilter.NO_FILTER);
    }

    public EntityChildren(List<EntityWrapper> children, TreeFilter filter) {
        parent = null;
        this.filter = filter;
        updateWithKeys(children == null ? Lists.<EntityWrapper>newArrayList() : children);
    }

    @Override
    public boolean isLoaded() {
        return super.isInitialized() && loadedKeys;
    }

    protected Callable<Children> getChildrenCallable(final EntityWrapper key) {
        return new Callable<Children>() {
            @Override
            public Children call() throws Exception {
                return new EntityChildren(key, filter);
            }
        };
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        if (initKeysAfterFirstAddNotify && !loadedKeys) {
            initKeysAfterFirstAddNotify = false;
            initKeys();
        }
    }

    @Override
    protected Node[] createNodes(final EntityWrapper key) {
        return new Node[]{EntityWrapperUtilities.createNode(key, Children.createLazy(getChildrenCallable(key)))};
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

    protected final void initKeys() {
        if (parent == null) {
            return;
        }

        final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Loading_Entity_Children(parent.getDisplayName()));

        EventQueueUtilities.runOffEDT(new Runnable() {
            @Override
            public void run() {
                ph.switchToIndeterminate();
                createKeys(ph);
            }
        }, ph);
    }

    public ListenableFuture<Void> refreshKeys() {
        return createKeys(null);
    }

    protected EntityChildrenWrapperHelper createEntityChildrenWrapperHelper(TreeFilter filter) {
        return new EntityChildrenWrapperHelper(filter) {

            @Override
            protected void displayUpdatedList(List<EntityWrapper> list, Comparator entityComparator) {
                EntityChildren.this.updatedList(list, entityComparator);
            }
            
        };
    }
    
    protected void updatedList(List<EntityWrapper> list, Comparator entityComparator) {
        displayUpdatedList(list, entityComparator);
    }
    
    protected ListenableFuture<Void> createKeys(ProgressHandle ph) {
        DataContext c = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        if (c == null) {
            return Futures.immediateFuture(null);
        }

        return updateWithKeys(createEntityChildrenWrapperHelper(filter).createKeysForEntity(c, parent, ph));
    }

    private void displayUpdatedList(List<EntityWrapper> list, Comparator entityComparator) {
        if (childcount % UPDATE_FACTOR == 0) {
            for (int i = 0; i < Math.min(UPDATE_FACTOR, list.size()); i++) {
                EntityWrapper last = list.get(list.size() - 1);
                for (int j = 0; j < list.size(); j++) {
                    //if last element < jth element of the list
                    if (entityComparator.compare(last, list.get(j)) < 0) {
                        list.remove(list.size() - 1);
                        list.add(j, last);
                        break;
                    }
                }

            }
            setKeys(list);
            addNotify();
        }
        childcount++;
    }

}
