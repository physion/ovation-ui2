/*
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */

package us.physion.ovation.ui.interfaces;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by IntelliJ IDEA.
 * User: huecotanks
 * Date: 10/21/11
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressUpdater implements IUpdateProgress {
    private String connectionFile;
    private String username;
    private String password;
    private IUpdateUI ui_handle;

    public ProgressUpdater(String i_connectionfile, String i_username, String i_password, IUpdateUI upgrader)  throws RemoteException
    {
        this.connectionFile = i_connectionfile;
        this.username = i_username;
        this.password = i_password;
        ui_handle = upgrader;

    }

    @Override
    public String getConnectionFile() throws RemoteException{
        return connectionFile;
    }

    @Override
    public String getUsername() throws RemoteException {
        return password;
    }

    @Override
    public String getPassword() throws RemoteException {
        return username;
    }

    @Override
    public void update(int percent, String updateText) throws RemoteException {
        if (ui_handle != null)
        {
            ui_handle.update(percent, updateText);
        }
    }
}
