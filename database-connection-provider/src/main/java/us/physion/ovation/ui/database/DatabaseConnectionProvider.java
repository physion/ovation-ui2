package us.physion.ovation.ui.database;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
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
@Messages({
    "Sync_Task=Syncing data from cloud..."
})
public class DatabaseConnectionProvider implements ConnectionProvider, EventBusProvider {


    static Logger logger = LoggerFactory.getLogger(DatabaseConnectionProvider.class);

    @Override
    public DataContext getNewContext() {
        return context.getCoordinator().getContext();
    }

    private DataContext context = null;
    private final Set<ConnectionListener> connectionListeners;

    public DatabaseConnectionProvider() {

        connectionListeners = Collections.synchronizedSet(new HashSet());
    }

    @Override
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

    private static final String FIRST_RUN_SYNC = "first_run_sync_completed";

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

                        final Preferences prefs = NbPreferences.forModule(LoginWindow.class);
                        boolean firstRunSync = prefs.getBoolean(FIRST_RUN_SYNC, false);

                        if (!firstRunSync) {

                            final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Sync_Task());
                            ph.start();

                            ListenableFuture<Boolean> sync = m.getDSC().sync(null);

                            Futures.addCallback(sync, new FutureCallback<Boolean>() {

                                @Override
                                public void onSuccess(Boolean v) {
                                    prefs.putBoolean(FIRST_RUN_SYNC, true);
                                }

                                @Override
                                public void onFailure(Throwable thrwbl) {
                                    logger.error("First-run sync failed");
                                    prefs.putBoolean(FIRST_RUN_SYNC, false);
                                }
                            });
                        }

                        for (ConnectionListener l : listeners) {
                            l.propertyChange(new PropertyChangeEvent(context, "ovation.connectionChanged", 0, 1));
                        }

                        getDefaultEventBus().post(new ConnectionProvider.LoginCompleteEvent());
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
