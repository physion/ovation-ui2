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

import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.DataContext;

@Messages({
    "Reset_Loading_Projects=Loading Projects",
    "Reset_Loading_Sources=Loading Sources",
    "Reset_Loading_Protocols=Loading Protocols"
})
public class EntityRootChildrenChildFactory extends EntityChildrenChildFactory {

    public EntityRootChildrenChildFactory(TreeFilter filter) {
        super(null, filter);
    }

    @Override
    protected EntityChildrenWrapperHelper createEntityChildrenWrapperHelper(final TreeFilter filter, BusyCancellable cancel) {
        return new EntityChildrenWrapperHelper(filter, cancel) {

            @Override
            public List<EntityWrapper> createKeysForEntity(List<EntityWrapper> list, DataContext ctx, EntityWrapper ew, ProgressHandle ph) {
                switch (filter.getNavigatorType()) {
                    case PROJECT:
                        EntityWrapperUtilities.wrap(list, ctx.getProjects());
                        break;
                    case SOURCE:
                        EntityWrapperUtilities.wrap(list, ctx.getTopLevelSources());
                        break;
                    case PROTOCOL:
                        EntityWrapperUtilities.wrap(list, ctx.getProtocols());
                        break;
                }
                return list;
            }
        };
    }

    @Override
    protected String getProgressDisplayName() {
        switch (filter.getNavigatorType()) {
            case PROJECT:
                return Bundle.Reset_Loading_Projects();
            case SOURCE:
                return Bundle.Reset_Loading_Sources();
            case PROTOCOL:
                return Bundle.Reset_Loading_Protocols();
        }
        //shouldn't happen
        return Bundle.Reset_Loading_Data();
    }
}
