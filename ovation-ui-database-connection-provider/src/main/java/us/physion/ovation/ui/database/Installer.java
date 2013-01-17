/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import ovation.Ovation;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

public class Installer extends ModuleInstall {

    private ConnectionProvider dbc;
    @Override
    public synchronized void restored() {
        
        if (dbc == null)
            dbc = new DatabaseConnectionProvider();
        
        // Set the displayed version number from API marketing version
        if(Ovation.getVersion() != null) {
            System.setProperty("netbeans.buildnumber", Ovation.getVersion());
        }
    }
}
