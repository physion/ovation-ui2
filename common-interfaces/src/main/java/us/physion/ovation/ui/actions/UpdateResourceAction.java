/*
 * Copyright (C) 2015 Physion LLC
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
package us.physion.ovation.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

@Messages({
    "UpdateResourceAction_Name=Save Version",
    "UpdateResourceAction_Updating=Saving new version...",
})
public class UpdateResourceAction extends AbstractAction {
    private final Resource resource;
    
    public UpdateResourceAction(Resource resource) {
        this.resource = resource;
        putValue(NAME, Bundle.UpdateResourceAction_Name());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EventQueueUtilities.runOffEDT(new Runnable() {

            @Override
            public void run() {
                resource.addRevision();
            }
        }, ProgressHandleFactory.createHandle(Bundle.UpdateResourceAction_Updating()));
    }
 
}
