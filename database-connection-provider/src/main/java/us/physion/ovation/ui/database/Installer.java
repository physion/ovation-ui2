/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import org.openide.modules.ModuleInstall;
import us.physion.ovation.api.Ovation;

public class Installer extends ModuleInstall {
    
    @Override
    public synchronized void restored() {
        // Set the displayed version number from API marketing version
        if(Ovation.getVersion() != null) {
            System.setProperty("netbeans.buildnumber", Ovation.getVersion());
        }
    }
}
