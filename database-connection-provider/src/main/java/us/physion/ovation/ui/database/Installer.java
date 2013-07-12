/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import org.openide.modules.ModuleInstall;
import us.physion.ovation.api.Ovation;
import us.physion.ovation.logging.Logging;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

public class Installer extends ModuleInstall {

    private ConnectionProvider dbc;
    @Override
    public synchronized void restored() {

        if (dbc == null)
        {
            Logging.configureRootLoggerRollingAppender();
            dbc = new DatabaseConnectionProvider();
        }
        // Set the displayed version number from API marketing version
        if(Ovation.getVersion() != null) {
            System.setProperty("netbeans.buildnumber", Ovation.getVersion());
        }
    }
}
