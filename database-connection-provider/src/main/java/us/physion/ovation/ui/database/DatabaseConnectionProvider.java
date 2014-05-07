package us.physion.ovation.ui.database;

import com.google.common.eventbus.EventBus;
import java.beans.PropertyChangeEvent;
import java.util.*;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.DataContext;
import us.physion.ovation.ui.interfaces.ConnectionListener;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventBusProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

/**
 *
 * @author jackie
 */
@ServiceProviders({
    @ServiceProvider(service = ConnectionProvider.class),
    @ServiceProvider(service = EventBusProvider.class)
})
public class DatabaseConnectionProvider implements ConnectionProvider, EventBusProvider {

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

    @Override
    public synchronized void login()
    {
        final ConnectionListener[] listeners = connectionListeners.toArray(new ConnectionListener[0]);

        final Runnable r = new Runnable() {

            @Override
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

        EventQueueUtilities.runOnEDT(r);
    }

    @Override
    public void addConnectionListener(ConnectionListener cl) {
        connectionListeners.add(cl);
    }

    @Override
    public void removeConnectionListener(ConnectionListener cl) {
        connectionListeners.remove(cl);
    }

    @Override
    public EventBus getDefaultEventBus() {
        return getDefaultContext().getEventBus();
    }
}
