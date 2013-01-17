/*
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */

package us.physion.ovation.ui.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by IntelliJ IDEA.
 * User: huecotanks
 * Date: 9/26/11
 * Time: 3:53 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IUpdateProgress extends Remote {
    public String getConnectionFile() throws RemoteException;
    public String getUsername() throws RemoteException;
    public String getPassword() throws RemoteException;

    public void update(int percent, String updateText) throws RemoteException;
}
