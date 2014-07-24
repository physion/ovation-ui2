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

import java.util.LinkedList;
import java.util.List;
import us.physion.ovation.DataContext;

public class FilteredEntityChildrenWrapperHelper extends EntityChildrenWrapperHelper {
    private final Iterable<Class> classesToInclude;

    public FilteredEntityChildrenWrapperHelper(TreeFilter filter, Iterable<Class> classesToInclude,  BusyCancellable cancel) {
        super(filter, cancel);
        this.classesToInclude = classesToInclude;
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

}
