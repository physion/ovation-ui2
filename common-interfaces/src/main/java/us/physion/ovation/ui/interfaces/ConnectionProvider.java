/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import us.physion.ovation.DataContext;

/**
 *
 * @author huecotanks
 */
public interface ConnectionProvider {
    
    public DataContext getDefaultContext();
    public DataContext getNewContext();
    public void addConnectionListener(ConnectionListener cl);
    public void removeConnectionListener(ConnectionListener cl);
    public void resetConnection();
    public void login();
}
