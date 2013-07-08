/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.api.Ovation;
import us.physion.ovation.ui.interfaces.ConnectionListener;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.exceptions.AuthenticationException;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author jackie
 */
@ServiceProvider(service = ConnectionProvider.class)
public class DatabaseConnectionProvider implements ConnectionProvider{

    @Override
    public DataContext getNewContext() {
        return context.getCoordinator().getContext();
    }
    
    private DataContext context = null;
    private Set<ConnectionListener> connectionListeners;
    private boolean waitingForContext = false;

    public DatabaseConnectionProvider() {
        
        connectionListeners = Collections.synchronizedSet(new HashSet());
    }

    public synchronized void resetConnection()
    {
        context = null;
        getDefaultContext();
    }

    @Override
    public DataContext getDefaultContext() {

        synchronized (this) {
            if (waitingForContext || context != null) {
                return context;
            }
            setWaitingFlag(true);
        }

        final ConnectionListener[] listeners = connectionListeners.toArray(new ConnectionListener[0]);
        
        final Runnable r = new Runnable() {

            public void run() {
                ProgressHandle ph = ProgressHandleFactory.createHandle("Authenticating...");
                try {
                    ph.start();
                    LoginDialog d = new LoginDialog();//loginDialog creates the dsc, because it can take a while
                    d.showDialog();
                    if (d.isAuthenticated()) {
                        setContext(d.getContext());
                        setWaitingFlag(false);

                        for (ConnectionListener l : listeners) {
                            l.propertyChange(new PropertyChangeEvent(context, "ovation.connectionChanged", 0, 1));
                        }
                    }
                } finally {
                    setWaitingFlag(false);
                    ph.finish();
                }
            }
        };
        
        EventQueueUtilities.runOnEDT(r);
        
        return context;
    }
    private synchronized void setContext(DataContext context) {
        this.context = context;
    }

    private synchronized void setWaitingFlag(boolean b) {
        waitingForContext = b;
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
