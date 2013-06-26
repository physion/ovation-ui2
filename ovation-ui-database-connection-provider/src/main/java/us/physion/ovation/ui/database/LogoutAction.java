/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.DataStoreCoordinator;

@ActionID(category = "Edit",
id = "us.physion.ovation.ui.database.LogoutAction")
@ActionRegistration(iconBase = "us/physion/ovation/ui/database/logout.png",
displayName = "#CTL_LogoutAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1100),
    //@ActionReference(path = "Toolbars/QuickSearch", position = 500),
    @ActionReference(path = "Shortcuts", name = "SM-L")
})
@Messages("CTL_LogoutAction=Logout")
public final class LogoutAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        ctx.getCoordinator().logout();
    }
}
