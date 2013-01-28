/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.query;

import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    private QueryProvider qp;
    @Override
    public void restored() {
        qp = new QueryProvider();
    }
}
