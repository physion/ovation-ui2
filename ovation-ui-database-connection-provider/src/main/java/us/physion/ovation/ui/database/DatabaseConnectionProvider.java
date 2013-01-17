/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import ovation.IAuthenticatedDataStoreCoordinator;
import ovation.LogLevel;
import ovation.Ovation;
import us.physion.ovation.ui.interfaces.ConnectionListener;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

@ServiceProvider(service = ConnectionProvider.class)
/**
 *
 * @author huecotanks
 */
public class DatabaseConnectionProvider implements ConnectionProvider {

    private IAuthenticatedDataStoreCoordinator dsc = null;
    private Set<ConnectionListener> connectionListeners;
    private boolean waitingForDSC = false;

    public DatabaseConnectionProvider() {

        Ovation.enableLogging(LogLevel.DEBUG);
        connectionListeners = Collections.synchronizedSet(new HashSet());
    }

    public synchronized void resetConnection()
    {
        dsc = null;
        getConnection();
    }

    @Override
    public IAuthenticatedDataStoreCoordinator getConnection() {

        synchronized (this) {
            if (waitingForDSC || dsc != null) {
                return dsc;
            }
            setWaitingFlag(true);
        }

        final ConnectionListener[] listeners = connectionListeners.toArray(new ConnectionListener[0]);

        Runnable r = new Runnable() {

            public void run() {

                /*
                 * try {
                 * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                 * /* for (javax.swing.UIManager.LookAndFeelInfo info :
                 * javax.swing.UIManager.getInstalledLookAndFeels()) { if
                 * ("Nimbus".equals(info.getName())) {
                 * javax.swing.UIManager.setLookAndFeel(info.getClassName());
                 * break; } }
                 */

                /*
                 * } catch (ClassNotFoundException ex) {
                 * java.util.logging.Logger.getLogger(DBConnectionDialog.class.getName()).log(java.util.logging.Level.SEVERE,
                 * null, ex); } catch (InstantiationException ex) {
                 * java.util.logging.Logger.getLogger(DBConnectionDialog.class.getName()).log(java.util.logging.Level.SEVERE,
                 * null, ex); } catch (IllegalAccessException ex) {
                 * java.util.logging.Logger.getLogger(DBConnectionDialog.class.getName()).log(java.util.logging.Level.SEVERE,
                 * null, ex); } catch
                 * (javax.swing.UnsupportedLookAndFeelException ex) {
                 * java.util.logging.Logger.getLogger(DBConnectionDialog.class.getName()).log(java.util.logging.Level.SEVERE,
                 * null, ex);
                }
                 */

                try {
                    //If licensing information is not set in the java preferences, set it now
                    Preferences p = Preferences.userNodeForPackage(Ovation.class);
                    if (p.get("ovation_license_licenseText", null) == null)
                    {
                        LicenseInfoDialog licenseDialog = new LicenseInfoDialog();
                        licenseDialog.showDialog();
                        p.put("ovation_license_institution", licenseDialog.getInstitution());
                        p.put("ovation_license_lab", licenseDialog.getLab());
                        p.put("ovation_license_licenseText", licenseDialog.getLicenseText());
                    }
                    
                    DBConnectionDialog dialog = new DBConnectionDialog();
                    DBConnectionManager manager = new DBConnectionManager();
                    dialog.setConnectionManager(manager);
                    manager.setConnectionDialog(dialog);
                    manager.showDialog();
                    if (!manager.dialogCancelled())
                    {
                        setDsc(manager.getDataStoreCoordinator());
                        setWaitingFlag(false);

                        for (PropertyChangeListener l : listeners) {
                            dialog.addPropertyChangeListener(l);
                        }
                        dialog.firePropertyChange("ovation.connectionChanged", 0, 1);
                    }
                } finally {
                    setWaitingFlag(false);
                }
            }
        };

        EventQueueUtilities.runOnEDT(r);

        return dsc;
    }

    private synchronized void setDsc(IAuthenticatedDataStoreCoordinator the_dsc) {
        dsc = the_dsc;
    }

    private synchronized void setWaitingFlag(boolean b) {
        waitingForDSC = b;
    }

    @Override
    public void addConnectionListener(ConnectionListener cl) {
        connectionListeners.add(cl);
    }

    @Override
    public void removeConnectionListener(ConnectionListener cl) {
        connectionListeners.remove(cl);
    }

    
}