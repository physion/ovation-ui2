/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import us.physion.ovation.DataStoreCoordinator;


/**
 *
 * @author huecotanks
 */
interface ConnectionDialog {
    public void showDialog();
    void disposeOnEDT();
    void showErrors(Exception e, DataStoreCoordinator dsc);

    public void startConnectionStatusBar();
}
