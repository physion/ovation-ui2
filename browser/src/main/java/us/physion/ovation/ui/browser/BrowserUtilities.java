/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

//import com.sun.source.tree.ExpressionTree;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.ActionMap;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.interfaces.ConnectionListener;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.ExpressionTreeProvider;
import us.physion.ovation.ui.interfaces.QueryListener;

/**
 *
 * @author jackie
 */
public class BrowserUtilities{
    protected static Map<String, Node> browserMap = new ConcurrentHashMap<String, Node>();
    protected static Map<ExplorerManager, Boolean> registeredViewManagers = new HashMap<ExplorerManager, Boolean>();
    protected static QueryListener ql;
    protected static ExecutorService executorService = Executors.newFixedThreadPool(2);
    protected static BrowserCopyAction browserCopy = new BrowserCopyAction();
    
    protected static ConnectionListener cn = new ConnectionListener(new Runnable(){

            @Override
            public void run() {
                resetView();
            }
            
        });
    
    public static Map<String, Node> getNodeMap()
    {
        return browserMap;
    } 
    
    
    static void submit(Runnable runnable) {
        executorService.submit(runnable);
    }
    
    public static void initBrowser(final ExplorerManager em, 
                                   final boolean projectView)
    {
        registeredViewManagers.put(em, projectView);//TODO: don't need this. we should be able to look up the explorerManagers from TopComponents
        ConnectionProvider cp = Lookup.getDefault().lookup(ConnectionProvider.class);
        cp.addConnectionListener(cn);
        
        if (ql == null)
        {
            final ExpressionTreeProvider etp = Lookup.getDefault().lookup(ExpressionTreeProvider.class);
            if (etp != null) {
                ql = new QueryListener(new Runnable() {

                    @Override
                    public void run() {
                        browserMap.clear();
                        //ExpressionTree result = Lookup.getDefault().lookup(ExpressionTreeProvider.class).getExpressionTree();
                        //setTrees(result);
                    }
                });
                etp.addQueryListener(ql);
            }
        }
    }
    
    static List<EntityWrapper> getEntityList(boolean projectView, DataContext ctx)
    {
        if (projectView)
        {
            return Lists.newArrayList(Iterables.transform(ctx.getProjects(), new Function<Project, EntityWrapper>() {

                @Override
                public EntityWrapper apply(Project input) {
                    return new EntityWrapper(input);
                }
            }));
        }
        else{
            return Lists.newArrayList(Iterables.transform(ctx.getTopLevelSources(), new Function<Source, EntityWrapper>() {

                @Override
                public EntityWrapper apply(Source input) {
                    return new EntityWrapper(input);
                }
            }));
        }
    }
    
    public static void resetView()
    {
        browserMap.clear();
        DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        
        for (ExplorerManager mgr : registeredViewManagers.keySet()) {
            List<EntityWrapper> list = getEntityList(registeredViewManagers.get(mgr), ctx);
            mgr.setRootContext(new EntityNode(new EntityChildren(list), null));
        }
    }
    
    public static void switchToSourceView()
    {
        Set<TopComponent> components = TopComponent.getRegistry().getOpened();
        for (TopComponent c : components)
        {
            if (c instanceof SourceBrowserTopComponent)
            {
                c.toFront();
                break;
            }
        }
    }
    public static void switchToProjectView()
    {
        Set<TopComponent> components = TopComponent.getRegistry().getOpened();
        for (TopComponent c : components)
        {
            if (c instanceof BrowserTopComponent)
            {
                c.toFront();
                break;
            }
        }
    }

    protected static void resetView(ExplorerManager e, boolean projectView)
    {
        DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        e.setRootContext(new EntityNode(new EntityChildren(getEntityList(projectView, ctx)), null));
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

    public static void runOnEDT(Runnable r)
    {
        if (EventQueue.isDispatchThread())
        {
            r.run();
        }
        else{
            SwingUtilities.invokeLater(r);
        }
    }
    
}
