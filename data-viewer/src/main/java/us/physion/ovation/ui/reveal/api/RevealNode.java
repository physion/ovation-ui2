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

import org.openide.util.Lookup;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.ui.actions.SelectInProjectNavigatorActionFactory;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

public abstract class RevealNode {

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
}
