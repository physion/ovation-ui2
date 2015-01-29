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
import us.physion.ovation.domain.FolderContainer;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

@Messages({
    "AddFolderAction_Default_Folder_Name=New Folder",
    "AddFolderAction_Adding_Folder=Adding new folder...",
    "AddFolderAction_Name=Add Folder"
})
public class AddFolderAction extends AbstractAction {

    private final FolderContainer container;

    public AddFolderAction(FolderContainer container) {
        this.container = container;
        putValue(NAME, Bundle.AddFolderAction_Name());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EventQueueUtilities.runOffEDT(new Runnable() {
            @Override
            public void run() {
                container.addFolder(Bundle.AddFolderAction_Default_Folder_Name());
            }
        }, ProgressHandleFactory.createHandle(Bundle.AddFolderAction_Adding_Folder()));
    }
}
