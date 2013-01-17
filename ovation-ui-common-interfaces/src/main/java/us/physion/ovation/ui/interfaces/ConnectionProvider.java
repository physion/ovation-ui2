/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import ovation.IAuthenticatedDataStoreCoordinator;

/**
 *
 * @author huecotanks
 */
public interface ConnectionProvider {
    
    public IAuthenticatedDataStoreCoordinator getConnection();
    public void addConnectionListener(ConnectionListener cl);
    public void removeConnectionListener(ConnectionListener cl);
    public void resetConnection();
}
