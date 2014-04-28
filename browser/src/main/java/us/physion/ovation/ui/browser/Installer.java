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

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import us.physion.ovation.DataContext;
import us.physion.ovation.logging.Logging;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author barry
 */
@NbBundle.Messages({
    "ExitConfirmationPanel_title=Exit Ovation?",
    "ExitConfirmationPanel_quitButton=Quit",
    "ExitConfirmationPanel_dontQuitButton=Don't Quit"
})
public class Installer extends ModuleInstall {
    
        @Override
    public synchronized void restored() {
        
        // Trigger login when the UI is ready
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                
                Logging.configureRootLoggerRollingAppender();
                Lookup.getDefault().lookup(ConnectionProvider.class).login();
            }
        });
        
    }
    
    @Override
    public synchronized boolean closing() {
        DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        if (ctx != null && ctx.getFileService().hasPendingUploads()) {

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

            return (DialogDisplayer.getDefault().notify(nd) == quitOption);
        } 
        
        return true;
    }
}
