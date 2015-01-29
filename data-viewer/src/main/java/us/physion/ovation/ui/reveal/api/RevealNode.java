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
package us.physion.ovation.ui.reveal.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.actions.SelectInProjectNavigatorActionFactory;
import us.physion.ovation.ui.browser.BrowserUtilities;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.search.OvationSearchProvider;

public abstract class RevealNode {
    private final static Logger log = LoggerFactory.getLogger(OvationSearchProvider.class);

    /**
     * Select the node in the tree view.
     * 
     * May be called from any thread it will automatically dispatch to the event queue.
     * 
     * @param topComponentID The TopComponent ID of the tree view owner
     * @param entity The entity whose corresponding node must be selected
     */
    public static void forEntity(final String topComponentID, final OvationEntity entity) {
        if (entity == null) {
            return;
        }
        
        EventQueueUtilities.runOnEDT(new Runnable() {
            @Override
            public void run() {
                SelectInProjectNavigatorActionFactory factory = Lookup.getDefault().lookup(SelectInProjectNavigatorActionFactory.class);
                factory.selectInTopComponent(topComponentID, entity).actionPerformed(null);
            }
        });
    }

    public static void forPath(List<IEntityWrapper> path) {
        if (path == null || path.isEmpty()) {
            return;
        }

        final List<URI> uriPath = new ArrayList<URI>();

        uriPath.add(null);
        for (int i = path.size() - 1; i >= 0; i--) {
            URI uri = path.get(i).getEntity().getURI();
            uriPath.add(uri);
        }

        IEntityWrapper topLevelParent = path.get(path.size() - 1);
        final String topComponentId = getExplorerID(topLevelParent.getEntity());

        if (topComponentId == null) {
            log.warn("Cannot find top level explorer parent for " + uriPath);
            return;
        }

        EventQueueUtilities.runOnEDT(new Runnable() {
            @Override
            public void run() {
                SelectInProjectNavigatorActionFactory factory = Lookup.getDefault().lookup(SelectInProjectNavigatorActionFactory.class);
                factory.select(topComponentId, uriPath).actionPerformed(null);
            }
        });
    }

    private static String getExplorerID(OvationEntity e) {
        if (e instanceof Project) {
            return BrowserUtilities.PROJECT_BROWSER_ID;
        } else if (e instanceof Source) {
            return BrowserUtilities.SOURCE_BROWSER_ID;
        } else if (e instanceof Protocol) {
            return BrowserUtilities.PROTOCOL_BROWSER_ID;
        }
        return null;
    }
}
