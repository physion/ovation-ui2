package us.physion.ovation.ui.database;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.ImageUtilities;
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

    static Logger logger = LoggerFactory.getLogger(DatabaseConnectionProvider.class);

    @Override
    public DataContext getNewContext() {
        return context.getCoordinator().getContext();
    }

    private DataContext context = null;
    private Set<ConnectionListener> connectionListeners;

    public DatabaseConnectionProvider() {

        connectionListeners = Collections.synchronizedSet(new HashSet());
    }

    public synchronized void resetConnection()
    {
        context = null;
        login();
        getDefaultContext();
    }

    @Override
    public DataContext getDefaultContext() {

        return context;
    }
    
    public synchronized void login()
    {
        final ProgressHandle ph = ProgressHandleFactory.createHandle("Authenticating...");
        
        final ConnectionListener[] listeners = connectionListeners.toArray(new ConnectionListener[0]);

        final Runnable r = new Runnable() {

            public void run() {
                    LoginModel m = new LoginWindow().showLoginDialog();
                    if (!m.isCancelled()) {
                        DatabaseConnectionProvider.this.context = m.getDSC().getContext();

                        for (ConnectionListener l : listeners) {
                            l.propertyChange(new PropertyChangeEvent(context, "ovation.connectionChanged", 0, 1));
                        }
                    }
            }
        };

        EventQueueUtilities.runOnEDT(r, ph);
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
