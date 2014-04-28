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
package us.physion.ovation.ui.database;

import java.util.Collection;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.DataContext;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 * Life cycle manager for Ovation. Prompts for exit if uploads are pending.
 *
 * @author barry
 */
@ServiceProvider(service = LifecycleManager.class, position = 1)
@Messages({
    "ExitConfirmationPanel_title=Exit Ovation?",
    "ExitConfirmationPanel_quitButton=Quit",
    "ExitConfirmationPanel_dontQuitButton=Don't Quit"
})
public class OvationLifecycleManager extends LifecycleManager {

    @Override
    public void saveAll() {
    }

    @Override
    public void exit() {
        DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        if (true) {//ctx != null && ctx.getFileService().hasPendingUploads()) {

            String quitOption = Bundle.ExitConfirmationPanel_quitButton();
            String dontQuitOption = Bundle.ExitConfirmationPanel_dontQuitButton();
            
            NotifyDescriptor nd = new NotifyDescriptor(
                    new ExitConfirmationPanel(), // instance of your panel
                    Bundle.ExitConfirmationPanel_title(), // title of the dialog
                    NotifyDescriptor.YES_NO_OPTION, // it is Yes/No dialog ...
                    NotifyDescriptor.WARNING_MESSAGE, // ... of a question type => a question mark icon
                    new String[] {quitOption, dontQuitOption},
                    dontQuitOption // default option is "Yes"
            );

            if (DialogDisplayer.getDefault().notify(nd) == quitOption) {
                exitAll();
            }
        } else {
            exitAll();
        }
    }

    private void exitAll() {
        Collection<LifecycleManager> c = Lookup.getDefault().lookup(new Lookup.Template(LifecycleManager.class)).allInstances();
        
        for (LifecycleManager lm : c) {
            if (lm != this) {
                lm.exit();
            }
        }
    }

}
