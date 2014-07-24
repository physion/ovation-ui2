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
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

public class EntityChildrenChildFactory extends ChildFactory<EntityWrapper> {
    private final EntityWrapper parent;
    protected final TreeFilter filter;

    public EntityChildrenChildFactory(EntityWrapper parent, TreeFilter filter) {
        this.parent = parent;
        this.filter = filter;
    }
    
    public void refresh() {
        super.refresh(false);
    }

    @Override
    protected Node createNodeForKey(EntityWrapper key) {
        return EntityWrapperUtilities.createNode(key, new EntityChildrenChildFactory(key, filter));
    }
    
    @Override
    protected boolean createKeys(List<EntityWrapper> toPopulate) {
        if (parent instanceof PerUserEntityWrapper) {
            toPopulate.addAll(((PerUserEntityWrapper) parent).getChildren());
            return true;
        }
        
        DataContext c = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        if (c == null) {
            return true;
        }

        final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Loading_Entity_Children(parent.getDisplayName()));
        ph.start();
        
        try {
            createEntityChildrenWrapperHelper(filter).createKeysForEntity(toPopulate, c, parent, ph);

            //XXX: Technically here we only need to sort the newly added elements since the previous call but it's too finicky and I'm not certain it's such a performance bottleneck
            Collections.sort(toPopulate, new EntityComparator());
            
        } finally {
            ph.finish();
        }
        
        return true;
    }
    
    protected EntityChildrenWrapperHelper createEntityChildrenWrapperHelper(TreeFilter filter) {
        return new EntityChildrenWrapperHelper(filter);
    }
}
