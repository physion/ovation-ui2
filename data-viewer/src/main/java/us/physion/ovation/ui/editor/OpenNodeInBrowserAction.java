package us.physion.ovation.ui.editor;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.ui.browser.BrowserUtilities;
import us.physion.ovation.ui.interfaces.LazyChildren;
import us.physion.ovation.ui.interfaces.TreeViewProvider;
import us.physion.ovation.ui.interfaces.URINode;
import us.physion.ovation.ui.jumpto.api.JumpHistory;

@NbBundle.Messages({
    "OpenNodeInBrowserAction=Select in Project Navigator",
    "SelectingProgress=Selecting"
})
public class OpenNodeInBrowserAction extends AbstractAction {

    private final static Logger log = LoggerFactory.getLogger(OpenNodeInBrowserAction.class);
    private final Provider em;
    private final TopComponent tc;
    private final List<URI> entityPath;
    private final TreeView view;
    private final String nodeDisplayName;
    private final boolean addToHistory;
    private final List<URI> historySource;

    public OpenNodeInBrowserAction(String explorerTopComponentID, List<URI> entityURI) {
        this(entityURI, null, false, null, explorerTopComponentID);
    }

    public OpenNodeInBrowserAction(List<URI> entityURI, /* @Nullable */ String nodeDisplayName) {
        this(entityURI, nodeDisplayName, false, null, BrowserUtilities.PROJECT_BROWSER_ID);
    }

    public OpenNodeInBrowserAction(List<URI> entityURI, /* @Nullable */ String nodeDisplayName, boolean addToHistory, List<URI> source, String explorerTopComponentID) {
        super(Bundle.OpenNodeInBrowserAction());
        tc = WindowManager.getDefault().findTopComponent(explorerTopComponentID);
        if (!tc.isOpened()) {
            tc.open();
        }

        if (!(tc instanceof ExplorerManager.Provider) || !(tc instanceof TreeViewProvider)) {
            throw new IllegalStateException();
        }

        em = (ExplorerManager.Provider) tc;
        view = (TreeView) ((TreeViewProvider) tc).getTreeView();

        this.entityPath = entityURI;
        this.nodeDisplayName = nodeDisplayName;
        this.addToHistory = addToHistory;
        this.historySource = source;
    }

    SelectInTreeViewRunnable<URI> createSelectRunnable(int id, Node root, Provider em, List<URI> entityPath, int entityPathIndex, TreeView view, ProgressHandle ph, boolean rootIsLoading, AtomicBoolean cancelled){
        return new SelectInTreeViewRunnable<URI>(id, root, em, entityPath, entityPathIndex, view, ph, rootIsLoading, cancelled) {

            @Override
            protected boolean areChildrenLazy(Node n) {
                LazyChildren c = n.getLookup().lookup(LazyChildren.class);
                return c != null;
            }

            @Override
            protected boolean areChildrenLoaded(Node n) {
                LazyChildren c = n.getLookup().lookup(LazyChildren.class);
                return c != null && c.isLoaded();
            }

            @Override
            protected SelectInTreeViewRunnable<URI> reinstantiate(int id, Node root, Provider em, List<URI> entityPath, int entityPathIndex, TreeView view, ProgressHandle ph, boolean rootIsLoading, AtomicBoolean cancelled) {
                return createSelectRunnable(id, root, em, entityPath, entityPathIndex, view, ph, rootIsLoading, cancelled);
            }

            @Override
            protected List<URI> getTreeSubPath(Node n) {
                if (n instanceof URINode) {
                    //XXX: Also see EntityNode.buildURITreePath

                    List<URI> paths = new ArrayList<URI>(((URINode) n).getFilteredParentURIs());
                    Collections.reverse(paths);

                    paths.add(((URINode) n).getURI());
                    return paths;
                } else {
                    log.warn("Cannot find subpath for non-URINode " + n);
                    return Collections.EMPTY_LIST;
                }
            }

            @Override
            protected void runAsync(Runnable r) {
                RequestProcessor.getDefault().post(r, 250);
            }

        };
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (addToHistory) {
            JumpHistory history = Lookup.getDefault().lookup(JumpHistory.class);
            if (history != null) {
                history.add(nodeDisplayName, entityPath, historySource);
            }
        }
        try {
            //search without expanding
            Node node = findNodeByURI(em.getExplorerManager().getRootContext(), entityPath.get(entityPath.size() - 1));
            if (node != null) {
                em.getExplorerManager().setSelectedNodes(new Node[]{node});
                tc.requestActive();
            } else {
                //some expanding is needed
                final AtomicBoolean cancelled = new AtomicBoolean(false);
                ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.SelectingProgress(), new Cancellable() {

                    @Override
                    public boolean cancel() {
                        cancelled.set(true);

                        return true;
                    }
                });
                ph.start();

                SelectInTreeViewRunnable<URI> r = createSelectRunnable(0, em.getExplorerManager().getRootContext(), em, entityPath, 0, view, ph, false, cancelled);
                r.runAsync(r);
            }
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private Node findNodeByURI(Node n, URI uri) {
        if (uri == null) {
            return null;
        }

        if (n instanceof URINode) {
            if (uri.equals(((URINode) n).getURI())) {
                return n;
            }
        }
        Children c = n.getChildren();
        LazyChildren lazy = n.getLookup().lookup(LazyChildren.class);
        Node[] children = new Node[0];
        if (lazy != null && !lazy.isLoaded()) {
                //nothing
        } else {
            children = c.getNodes();
        }
        for (Node child : children) {
            Node found = findNodeByURI(child, uri);
            if (found != null) {
                return found;
            }
        }

        return null;
    }
}
