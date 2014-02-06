package us.physion.ovation.ui.browser;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.*;
import java.util.concurrent.*;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.interfaces.ConnectionListener;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.ExpressionTreeProvider;
import us.physion.ovation.ui.interfaces.QueryListener;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.QueryService;

/**
 *
 * @author jackie
 */
public class BrowserUtilities{
    protected static Map<ExplorerManager, TreeFilter> registeredViewManagers = new HashMap<ExplorerManager, TreeFilter>();
    protected static QueryListener ql;
    protected static ExecutorService executorService = Executors.newFixedThreadPool(2);
    protected static BrowserCopyAction browserCopy = new BrowserCopyAction();

    protected static ConnectionListener cn = new ConnectionListener(new Runnable(){

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
                                   final TreeFilter projectView)
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
//                        browserMap.clear();
                        //ExpressionTree result = Lookup.getDefault().lookup(ExpressionTreeProvider.class).getExpressionTree();
                        //setTrees(result);
                    }
                });
                etp.addQueryListener(ql);
            }
        }
    }
    
    static List<EntityWrapper> getEntityList(TreeFilter projectView, DataContext ctx)
    {
        //check that I'm 
        QuerySet qs = Lookup.getDefault().lookup(QueryProvider.class).getQuerySet();
        if (projectView.isProjectView())
        {
            List<EntityWrapper> projects =  Lists.newArrayList(Iterables.transform(ctx.getProjects(), new Function<Project, EntityWrapper>() {

                @Override
                public EntityWrapper apply(Project input) {
                    return new EntityWrapper(input);
                }
            }));
            Collections.sort(projects, new EntityComparator());
            return projects;
        }
        else{
            List<EntityWrapper> sources = Lists.newArrayList(Iterables.transform(ctx.getTopLevelSources(), new Function<Source, EntityWrapper>() {

                @Override
                public EntityWrapper apply(Source input) {
                    return new EntityWrapper(input);
                }
            }));
            Collections.sort(sources, new EntityComparator());
            return sources;
        }
    }
    
    public static void resetView()
    {
        DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        ctx.getRepository().clear();
        
        QuerySet qs = Lookup.getDefault().lookup(QueryProvider.class).getQuerySet();
        
        if (qs == null) {
            for (ExplorerManager mgr : registeredViewManagers.keySet()) {
                TreeFilter filter = registeredViewManagers.get(mgr);
                mgr.setRootContext(createRootNode(filter));
            }
        } else {
            qs.reset();
        }
    }

    private static EntityNode createRootNode(final TreeFilter filter) {
        return new EntityRootNode(new Callable<List<EntityWrapper>>() {
            @Override
            public List<EntityWrapper> call() throws Exception {
                DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
                return getEntityList(filter, ctx);
            }
        }, filter);
    }

    public static void switchToSourceView() {
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

    protected static void resetView(ExplorerManager e, TreeFilter projectView)
    {
        QuerySet qs = Lookup.getDefault().lookup(QueryProvider.class).getQuerySet();
        
        if (qs == null) {
            e.setRootContext(createRootNode(projectView));
        }else{
            qs.reset(e, projectView);
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
