/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.api.Ovation;
import us.physion.ovation.exceptions.AuthenticationException;
import us.physion.ovation.ui.interfaces.ConnectionListener;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author jackie
 */
@ServiceProvider(service = ConnectionProvider.class)
public class DatabaseConnectionProvider implements ConnectionProvider {

    static Logger logger = LoggerFactory.getLogger(DatabaseConnectionProvider.class);


    private static class LoginModel {
        String email;
        char[] password;
        boolean cancelled = true;

        void setEmail(String email) {
            this.email = email;
        }

        void setPassword(char[] pw) {
            this.password = pw;
        }

        char[] getPassword() {
            return password;
        }

        String getEmail() {
            return email;
        }

        boolean isCancelled() {
            return cancelled;
        }
    }

    private DataStoreCoordinator dsc = null;
    private Set<ConnectionListener> connectionListeners;
    private boolean waitingForDSC = false;

    public DatabaseConnectionProvider() {

        connectionListeners = Collections.synchronizedSet(new HashSet());
    }

    @Override
    public synchronized void resetConnection() {
        dsc = null;
        getConnection();
    }

    @Override
    public DataStoreCoordinator getConnection() {

        synchronized (this) {
            if (waitingForDSC || dsc != null) {
                return dsc;
            }
            setWaitingFlag(true);
        }

        final ConnectionListener[] listeners = connectionListeners.toArray(new ConnectionListener[0]);

        final Runnable r = new Runnable() {

            public void run() {
                //ProgressHandle ph = ProgressHandleFactory.createHandle("Authenticating...");
                try {
                    //ph.start();
                    DataStoreCoordinator toAuthenticate = Ovation.newDataStoreCoordinator();
                    boolean succeeded = authenticateUser(toAuthenticate, null);
                    if (succeeded) {
                        setDsc(toAuthenticate);
                        setWaitingFlag(false);

                        for (ConnectionListener l : listeners) {
                            l.propertyChange(new PropertyChangeEvent(toAuthenticate, "ovation.connectionChanged", 0, 1));
                        }
                    }

                } finally {
                    setWaitingFlag(false);
                    // ph.finish();
                }
            }
        };

        EventQueueUtilities.runOnEDT(r);

        return dsc;
    }

    private LoginModel showLoginDialog(String error) {

        final LoginModel model = new LoginModel();

        final LoginDialog d = new LoginDialog(new JFrame(), true);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.addLoginActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                model.setEmail(d.getEmail());
                model.setPassword(d.getPassword());
                model.cancelled = false;
                d.getBusyLabel().setBusy(true);
                d.dispose();
            }
        });

        d.addCancelActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                model.cancelled = true;
                d.dispose();
            }
        });

        //show dialog
        //d.setLocationRelativeTo(null);
        d.setVisible(true);
        return model;
    }

    private boolean authenticateUser(DataStoreCoordinator dsc, String error) {
        LoginModel m = showLoginDialog(error);
        if (!m.isCancelled()) {
            try {
                return dsc.authenticateUser(m.getEmail(), m.getPassword()).get();
            } catch (AuthenticationException e) {
                return authenticateUser(dsc, e.getLocalizedMessage());
            } catch (InterruptedException e) {
                return authenticateUser(dsc, e.getLocalizedMessage());
            } catch (ExecutionException e) {
                return authenticateUser(dsc, e.getLocalizedMessage());
            }
            //TODO: add other common errors here
        }
        return false;
    }

    private synchronized void setDsc(DataStoreCoordinator the_dsc) {
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
