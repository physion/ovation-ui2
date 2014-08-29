/*
 * Copyright (C) 2014 Physion LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package us.physion.ovation.ui.browser;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.DataContext;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

@Messages({
    "Loading_Interrupted=Loading interrupted"
    ,"Loading_Interrupted_Description=Use Reload from the parent element's context menu to continue loading"
})
public class EntityChildrenChildFactory extends ChildFactory<EntityWrapper> {
    private final EntityWrapper parent;
    protected final TreeFilter filter;

    public EntityChildrenChildFactory(EntityWrapper parent, TreeFilter filter) {
        this.parent = parent;
        this.filter = filter;
    }
    
    public void refresh() {
        loaded.set(false);
        super.refresh(false);
    }

    @Override
    protected Node createNodeForKey(EntityWrapper key) {
        if(key == EntityWrapper.EMPTY) {
            return new AbstractNode(Children.LEAF) {
                {
                    setIconBaseWithExtension("us/physion/ovation/ui/browser/interrupted.png");
                    setDisplayName(Bundle.Loading_Interrupted());
                    setShortDescription(Bundle.Loading_Interrupted_Description());
                }
            };
        }
        return EntityWrapperUtilities.createNode(key, new EntityChildrenChildFactory(key, filter));
    }
    
    @Override
    protected final boolean createKeys(List<EntityWrapper> toPopulate) {
        boolean finished = createKeysInternal(toPopulate);
        if (finished) {
            loaded.set(true);
        }
        return finished;
    }
    
    private boolean createKeysInternal(List<EntityWrapper> toPopulate) {
        if (parent instanceof PerUserEntityWrapper) {
            toPopulate.addAll(((PerUserEntityWrapper) parent).getChildren());
            return true;
        }
        
        DataContext c = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        if (c == null) {
            return true;
        }

        BusyCancellable cancel = new BusyCancellable() {

            private final AtomicBoolean cancelled = new AtomicBoolean();

            @Override
            public boolean cancel() {
                //user expressly cancelled in the progress UI, allow it.
                cancelled.set(true);
                return true;
            }

            @Override
            public boolean isCancelled() {
                return cancelled.get()
                        || HeavyLoadManager.getDefault().isCancelled(parent)
                        || Thread.interrupted();
            }
        };
        
        final ProgressHandle ph = ProgressHandleFactory.createHandle(getProgressDisplayName(), cancel);
        ph.start();
        
        HeavyLoadManager.getDefault().startLoading(parent);

        try {
            createEntityChildrenWrapperHelper(filter, cancel).createKeysForEntity(toPopulate, c, parent, ph);

            //XXX: Technically here we only need to sort the newly added elements since the previous call but it's too finicky and I'm not certain it's such a performance bottleneck
            Collections.sort(toPopulate, new EntityComparator());
            
        } finally {
            ph.finish();
            HeavyLoadManager.getDefault().finishedLoading(parent);
        }
        
        return true;
    }
    
    protected String getProgressDisplayName(){
        return Bundle.Loading_Entity_Children(parent.getDisplayName());
    }
    
    protected EntityChildrenWrapperHelper createEntityChildrenWrapperHelper(TreeFilter filter,  BusyCancellable cancel) {
        return new EntityChildrenWrapperHelper(filter, cancel);
    }

    private final AtomicBoolean loaded = new AtomicBoolean();
    
    public boolean isLoaded() {
        return loaded.get();
    }
}
