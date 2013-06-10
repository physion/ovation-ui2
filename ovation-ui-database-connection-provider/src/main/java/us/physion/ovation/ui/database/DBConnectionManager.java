/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import ovation.*;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.User;
import us.physion.ovation.exceptions.AuthenticationException;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IUpgradeDB;
import us.physion.ovation.ui.interfaces.UpdateInfo;

/**
 *
 * @author huecotanks
 */
class DBConnectionManager {
    
    ConnectionDialog connectionDialog;
    CancellableDialog installVersionDialog;
    CancellableDialog shouldRun;
    
    boolean cancelled = false;
    DataStoreCoordinator dsc;
    public void setConnectionDialog(ConnectionDialog d)
    {
        connectionDialog = d;
    }
    
    public void setInstallVersionDialog(CancellableDialog d)
    {
        installVersionDialog = d;
    }
    
    public void setShouldRunDialog(CancellableDialog d)
    {
        shouldRun = d;
    }
    
    public void showDialog()
    {
        connectionDialog.showDialog();
    }

    public boolean dialogCancelled()
    {
         return cancelled;
    }

    public DataStoreCoordinator getDataStoreCoordinator()
    {
        return dsc;
    }
    
    void tryToConnect(final String username, final String password, final String connectionFile) {
        //run this on a thread that's not the event queue thread
        Runnable r = new Runnable(){

            @Override
            public void run() {
                connectionDialog.startConnectionStatusBar();
                
                DataContext c = getContextFromConnectionFile(connectionFile, username, password);
                if (c == null) {
                    return;
                }

                boolean authenticated = false;
                try {
                    Iterable<User> users = c.getRepository().getWithUsername(username);
                    if (!users.iterator().hasNext())
                        connectionDialog.showErrors(new AuthenticationException("No user with username '" + username), c.getCoordinator());   
                    
                    for (User u : users)
                    {
                        try {
                            if (c.getCoordinator().authenticateUser(u.getEmail(), password.toCharArray()).get()) {
                                authenticated = true;
                                break;
                            }
                        } catch(Exception e)
                        {
                            //pass
                        }
                    }
                } catch (Exception ex) {
                    connectionDialog.showErrors(ex, c.getCoordinator());
                    return;
                }
                if (!authenticated) {
                    connectionDialog.showErrors(new AuthenticationException("Username and password didn't match"), c.getCoordinator());
                    return;
                }
                dsc = c.getCoordinator();
                cancelled = false;
                
                connectionDialog.disposeOnEDT();
            }
            
        };
        EventQueueUtilities.runOffEDT(r);
    }
    void cancel()
    {
        cancelled = true;
    }
     protected DataContext getContextFromConnectionFile(String connectionFile, String username, String password)
    {
        /*DataStoreCoordinator dsc = null;
        DataContext c = null;
        try {
            dsc = DataStoreCoordinator.coordinatorWithConnectionFile(connectionFile);
            c = dsc.getContext();
        } catch(DatabaseNotFoundException ex)
        {
            Preferences p = Preferences.userNodeForPackage(Ovation.class);                        
            String idString = p.get("local_fd_ids", "");
            String[] idStrings = idString.split(",");
            Set ids = new HashSet();
            for (String s : idStrings)
            {
                if (s.isEmpty())
                    continue;
                ids.add(Integer.valueOf(s));
            }
            int fdid = 5000;
            while(ids.contains(fdid))
            {
                fdid++;
            }
            idString += ","+ fdid;
            p.put("local_fd_ids", idString);
                       
            String lockserver;
            try {
                lockserver = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex1) {
                lockserver = "127.0.0.1";
            }
            DatabaseManager.createLocalDatabase(connectionFile, lockserver, fdid);
            
            //license database, and add user
            try {
                dsc = DataStoreCoordinator.coordinatorWithConnectionFile(connectionFile);
                String institution = p.get("ovation_license_institution", "");
                String lab = p.get("ovation_license_lab", "");
                String text = p.get("ovation_license_licenseText", "");
                dsc.licenseDatabase(institution, lab, text, false);
                
                c = dsc.getContext();
                c.addUser(username, password);
                try {
                    c.authenticateUser(username, password);
                } catch (UserAuthenticationException ex1) {
                    cancelled = true;
                    connectionDialog.showErrors(ex1, dsc);
                    return c;
                }
                return c;

            } catch (DatabaseOpenException ex1) {
                cancelled = true;
                connectionDialog.showErrors(ex1, dsc);
                return c;
            } catch (DatabaseNotFoundException ex1) {
                cancelled = true;
                connectionDialog.showErrors(ex1, dsc);
                return c;
            }
        }
        catch (SchemaVersionException ex2)
        {
            int databaseVersion = ex2.getDatabaseSchemaNumber();
            int apiVersion = ex2.getAPISchemaNumber();
            boolean success = shouldRunUpdater(databaseVersion, apiVersion); //ask the user if they want to run the upgrader
            if (success)
            {
                Collection<? extends UpdateInfo> updates = Lookup.getDefault().lookupAll(UpdateInfo.class);
                List<UpdateInfo> versions = new LinkedList<UpdateInfo>();
                for (UpdateInfo u : updates)
                {
                    int updateVersion = u.getSchemaVersion();
                    if (updateVersion > databaseVersion && updateVersion <= apiVersion)
                    {
                        versions.add(u);
                    }
                }
                Collections.sort(versions, new DBConnectionManager.UpdateComparator());
                UpdaterInProgressDialog uiUpdater = new UpdaterInProgressDialog();
                UpgradeTool tool = new UpgradeTool(versions, connectionFile, username, password, uiUpdater);
                uiUpdater.setUpgradeTool(tool);
                try{
                    success = runUpdater(tool, uiUpdater);
                } catch (Exception e)
                {
                    cancelled = true;
                    connectionDialog.showErrors(e, dsc);
                    return c;
                }
            }
            if (success)
            {
                try {
                    dsc = DataStoreCoordinator.coordinatorWithConnectionFile(connectionFile);
                    c = dsc.getContext();
                } catch (Exception ex)
                {
                    connectionDialog.showErrors(ex, dsc);
                    return c;
                }
            }
            else{
                cancelled = true;
                return c;
            }
        }
        catch (DatabaseIsUpgradingException ex)
        {
            DBIsUpgradingDialog d = new DBIsUpgradingDialog();
            d.showDialog();
            if (d.forceUpgrade())
            {
                try {
                    dsc = DataStoreCoordinator.coordinatorWithConnectionFile(connectionFile);
                    dsc.removeUpgradeLock("OVATION_UPGRADE_FLAG");
                } catch (Exception ex1)
                {
                    connectionDialog.showErrors(ex1, dsc);
                    return c;
                }
               return getContextFromConnectionFile(connectionFile, username, password);
            }
                    
        }
        catch (Exception ex) {
            connectionDialog.showErrors(ex, dsc);
            return c;
        }
        return c;
        *
        */
        return null;
    }
    
    protected boolean shouldRunUpdater(int databaseVersion, int apiVersion)
    {
        if (databaseVersion <0 || apiVersion <0 )
        {
            connectionDialog.showErrors(new RuntimeException("Invalid database schema version (" + databaseVersion + ") or api schema version (" + apiVersion + ")"), null);
            return false;
        }
        
        if (databaseVersion > apiVersion)
        {
            if (installVersionDialog == null)
                installVersionDialog = new InstallLatestVersionDialog();
            installVersionDialog.showDialog();
            return !installVersionDialog.isCancelled();
        }
        else if (databaseVersion < apiVersion)
        {
            if (shouldRun == null)
                shouldRun = new ShouldRunUpdaterDialog();
            shouldRun.showDialog();
            return !shouldRun.isCancelled();
        }
        return false;
    }
    
    protected boolean runUpdater(final IUpgradeDB tool, CancellableDialog inProgress)
    {
        /*inProgress.showDialog();
        try{
            tool.start();
        } catch (DatabaseIsUpgradingException e)
        {
            inProgress.cancel();
            //TODO: pop up a dialog here
            
        } catch (Exception e)
        {
            inProgress.cancel();
            throw new RuntimeException(e.getLocalizedMessage());
        }
        if (inProgress.isCancelled())
        {
            return false;
        }
        else{
            return true;
        }*/
        return true;
    }
    class UpdateComparator implements Comparator<UpdateInfo>
    {
        @Override
        public int compare(UpdateInfo t, UpdateInfo t1) {
            return t.getSchemaVersion() - t1.getSchemaVersion();
        }
    }
}
