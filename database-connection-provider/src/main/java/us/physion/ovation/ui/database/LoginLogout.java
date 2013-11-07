package us.physion.ovation.ui.database;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

@ActionID(category = "Profile",
id = "us.physion.ovation.ui.database.LoginLogout")
@ActionRegistration(iconBase = "us/physion/ovation/ui/database/switch-user.png",
displayName = "#CTL_LoginLogout")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 2440),
    @ActionReference(path = "Shortcuts", name = "D-L")
})
@Messages("CTL_LoginLogout=Login")
public final class LoginLogout extends AbstractAction {// implements Presenter.Toolbar

    @Override
    public void actionPerformed(ActionEvent e) {
        ConnectionProvider cp = Lookup.getDefault().lookup(ConnectionProvider.class);
        if (cp == null)
        {
            cp = new DatabaseConnectionProvider();
        }
        cp.resetConnection();
    }
}
