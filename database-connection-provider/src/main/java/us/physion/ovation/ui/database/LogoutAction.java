package us.physion.ovation.ui.database;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.DataContext;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

@ActionID(category = "Edit",
id = "us.physion.ovation.ui.database.LogoutAction")
@ActionRegistration(iconBase = "us/physion/ovation/ui/database/logout.png",
displayName = "#CTL_LogoutAction")
@ActionReferences({
    //exit has position 2600
    @ActionReference(path = "Menu/File", position = 2450),
    @ActionReference(path = "Shortcuts", name = "DS-L")
})
@Messages("CTL_LogoutAction=Logout")
public final class LogoutAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        ctx.getCoordinator().logout();
    }
}
