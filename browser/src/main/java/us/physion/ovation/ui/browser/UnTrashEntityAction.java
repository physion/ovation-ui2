/*
 * Copyright (C) 2014 Physion Consulting LLC
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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;
import java.net.URI;
import java.util.UUID;
import us.physion.ovation.domain.URIs;

@ActionID(
    category = "Edit",
id = "us.physion.ovation.ui.browser.UnTrashEntityAction")
@ActionRegistration(
    displayName = "#CTL_UnTrashEntityAction")
@NbBundle.Messages({
    "CTL_UnTrashEntityAction=Restore from Trash",
    "Progress_RestoringFromTrash=Restoring from Trash",
    "# {0} - restored entity UUID",
    "Restored=Restored {0}"
})
public class UnTrashEntityAction extends TrashEntityAction {

    @Override
    public String getName() {
        return Bundle.CTL_UnTrashEntityAction();
    }

    @Override
    protected String getProgressText() {
        return Bundle.Progress_RestoringFromTrash();
    }

    @Override
    protected String getProgressSuccessText(UUID file) {
        return Bundle.Restored(file);
    }

    @Override
    protected ListenableFuture<Iterable<URI>> call(DataContext c, OvationEntity entity) {
        return Futures.transform(c.restoreFromTrash(entity), new Function<Iterable<UUID>, Iterable<URI>>() {
            @Override
            public Iterable<URI> apply(Iterable<UUID> input) {
                return input == null ? null : Iterables.transform(input, new Function<UUID, URI>() {

                    @Override
                    public URI apply(UUID input) {
                        return input == null ? null : URIs.create(input);
                    }
                });
            }
        });
    }

    @Override
    protected boolean isIncludingTrashEntities() {
        return true;
    }
    
    @Override
    protected OvationEntity filter(OvationEntity entity) {
        //allow all entities, including trashed
        return entity;
    }
}
