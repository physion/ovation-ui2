/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.Presenter.Toolbar;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

@ActionID(category = "Profile",
id = "us.physion.ovation.ui.database.LoginLogout")
@ActionRegistration(iconBase = "us/physion/ovation/ui/database/switch-user.png",
displayName = "#CTL_LoginLogout")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1000),
    @ActionReference(path = "Toolbars/Edit", position = 20),
    @ActionReference(path = "Shortcuts", name = "M-L")
})
@Messages("CTL_LoginLogout=Change Connection")
public final class LoginLogout extends AbstractAction {// implements Presenter.Toolbar

    public void actionPerformed(ActionEvent e) {
        ConnectionProvider cp = Lookup.getDefault().lookup(ConnectionProvider.class);
        if (cp == null)
        {
            cp = new DatabaseConnectionProvider();
            cp.getConnection();
        }else {
            cp.resetConnection();
        }
    }
}
