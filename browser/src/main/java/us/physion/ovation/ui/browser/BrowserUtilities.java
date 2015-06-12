package us.physion.ovation.ui.browser;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import us.physion.ovation.DataContext;
import us.physion.ovation.ui.interfaces.ConnectionListener;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.ExpressionTreeProvider;
import us.physion.ovation.ui.interfaces.QueryListener;
import us.physion.ovation.ui.interfaces.RefreshableNode;

/**
 *
 * @author jackie
 */
@Messages({
    "Reset_Loading_Data=Loading data"
})
public class BrowserUtilities {

    public final static String PROJECT_BROWSER_ID = "ProjectBrowserTopComponent"; //NOI18N
    public final static String SOURCE_BROWSER_ID = "SourceBrowserTopComponent"; //NOI18N
    public final static String PROTOCOL_BROWSER_ID = "ProtocolBrowserTopComponent"; //NOI18N

    protected static Map<ExplorerManager, TreeFilter> registeredViewManagers = new HashMap<ExplorerManager, TreeFilter>();
    protected static QueryListener ql;
    protected static ExecutorService executorService = Executors.newFixedThreadPool(2);
    protected static BrowserCopyAction browserCopy = new BrowserCopyAction();

    protected static ConnectionListener cn = new ConnectionListener(new Runnable() {

        @Override
        public void run() {
            Lookup.getDefault().lookup(QueryProvider.class).setQuerySet(null);
            resetView();
        }

    });

    static void submit(Runnable runnable) {
        executorService.submit(runnable);
    }

    public static void initBrowser(final ExplorerManager em,
            final TreeFilter projectView) {
        registeredViewManagers.put(em, projectView);//TODO: don't need this. we should be able to look up the explorerManagers from TopComponents
        ConnectionProvider cp = Lookup.getDefault().lookup(ConnectionProvider.class);
        cp.addConnectionListener(cn);

        HeavyLoadManager.getDefault().register(em);

        if (ql == null) {
            final ExpressionTreeProvider etp = Lookup.getDefault().lookup(ExpressionTreeProvider.class);
            if (etp != null) {
                ql = new QueryListener(new Runnable() {

                    @Override
                    public void run() {
//                        browserMap.clear();
                        //ExpressionTree result = Lookup.getDefault().lookup(ExpressionTreeProvider.class).getExpressionTree();
                        //setTrees(result);
                    }
                });
                etp.addQueryListener(ql);
            }
        }
    }

    public static void resetView() {
        final DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        final QuerySet qs = Lookup.getDefault().lookup(QueryProvider.class).getQuerySet();

        final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Reset_Loading_Data());

        EventQueueUtilities.runOffEDT(() -> {
            if (ctx == null) {
                
                for (ExplorerManager mgr : registeredViewManagers.keySet()) {
                    TreeFilter filter = registeredViewManagers.get(mgr);
                    mgr.setRootContext(createRootNode(filter));
                }
                
            } else {
                ctx.getRepository().clear();
                
                if (qs == null) {
                    for (ExplorerManager mgr : registeredViewManagers.keySet()) {
                        TreeFilter filter = registeredViewManagers.get(mgr);
                        mgr.setRootContext(createRootNode(filter));
                    }
                } else {
                    qs.reset();
                }
            }
        }, ph);
    }

    public static ListenableFuture<Void> reloadView(String topComponendId) {
        TopComponent tc = WindowManager.getDefault().findTopComponent(topComponendId);
        if (!(tc instanceof ExplorerManager.Provider)) {
            throw new IllegalStateException();
        }

        final ExplorerManager explorerManager = ((ExplorerManager.Provider) tc).getExplorerManager();

        Node rootCtx = explorerManager.getRootContext();
        if (rootCtx instanceof RefreshableNode) {
            ((RefreshableNode) rootCtx).refresh();
        }

        return Futures.immediateFuture(null);
    }

    public static ListenableFuture<Void> resetView(String topComponendId) {
        TopComponent tc = WindowManager.getDefault().findTopComponent(topComponendId);
        if (!(tc instanceof ExplorerManager.Provider)) {
            throw new IllegalStateException();
        }

        final ExplorerManager explorerManager = ((ExplorerManager.Provider) tc).getExplorerManager();

        final DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        final QuerySet qs = Lookup.getDefault().lookup(QueryProvider.class).getQuerySet();

        final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Reset_Loading_Data());

        return EventQueueUtilities.runOffEDT(new Runnable() {

            @Override
            public void run() {
                ctx.getRepository().clear();

                if (qs == null) {
                    TreeFilter filter = registeredViewManagers.get(explorerManager);
                    explorerManager.setRootContext(createRootNode(filter));
                } else {
                    qs.reset();
                }
            }
        }, ph);
    }

    private static EntityNode createRootNode(final TreeFilter filter) {
        return new EntityRootNode(new EntityRootChildrenChildFactory(filter));
    }

    public static void switchToSourceView() {
        Set<TopComponent> components = TopComponent.getRegistry().getOpened();
        for (TopComponent c : components) {
            if (c instanceof SourceBrowserTopComponent) {
                c.toFront();
                break;
            }
        }
    }

    public static void switchToProjectView() {
        Set<TopComponent> components = TopComponent.getRegistry().getOpened();
        for (TopComponent c : components) {
            if (c instanceof ProjectBrowserTopComponent) {
                c.toFront();
                break;
            }
        }
    }

    public static void switchToProtocolView() {
        Set<TopComponent> components = TopComponent.getRegistry().getOpened();
        for (TopComponent c : components) {
            if (c instanceof ProtocolBrowserTopComponent) {
                c.toFront();
                break;
            }
        }
    }

    protected static void resetView(final ExplorerManager e, final TreeFilter filter) {
        final QuerySet qs = Lookup.getDefault().lookup(QueryProvider.class).getQuerySet();

        if (qs == null) {
            e.setRootContext(createRootNode(filter));
        } else {
            qs.reset(e, filter);
        }
    }

    //TODO: uncomment when we have query capabiliites
    /*protected static void setTrees(final ExpressionTree result)
     {
     if (result == null)
     return;

     Set<ExplorerManager> mgrs = new HashSet<ExplorerManager>();
     for (ExplorerManager em : registeredViewManagers.keySet())
     {
     em.setRootContext(new EntityNode(new QueryChildren(registeredViewManagers.get(em)), null));
     mgrs.add(em);
     }

     final DataStoreCoordinator dsc = Lookup.getDefault().lookup(ConnectionProvider.class).getConnection();
     Iterator itr = dsc.getContext().query(result);

     EntityWrapperUtilities.createNodesFromQuery(mgrs, itr);

     }*/
}
