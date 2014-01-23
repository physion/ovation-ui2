package us.physion.ovation.ui.editor;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Messages({
    "# {0} - node display name",
    "Expanding_Node=Expanding {0}"
})
public abstract class SelectInTreeViewRunnable<Path> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(SelectInTreeViewRunnable.class);
    private final Node root;
    private final Provider em;
    private final List<Path> path;
    private final TreeView view;
    private final int entityPathIndex;
    private final ProgressHandle ph;
    private boolean asyncPosted = false;
    private final int id;
    private final boolean rootIsLoading;
    private final AtomicBoolean cancelled;

    protected SelectInTreeViewRunnable(int id, Node root, Provider em, List<Path> entityPath, int entityPathIndex, TreeView view, ProgressHandle ph, boolean rootIsLoading, AtomicBoolean cancelled) {
        this.id = id;
        this.root = root;
        this.rootIsLoading = rootIsLoading;
        this.em = em;
        this.path = entityPath;
        this.view = view;
        this.entityPathIndex = entityPathIndex;
        this.ph = ph;
        this.cancelled = cancelled;
        
    }

    @Override
    public void run() {
        expandNodeByFilterablePath(root, path, entityPathIndex, view);

        if (!asyncPosted) {
            ph.finish();
        }
    }

    /**
     *
     * @param n the root node to search
     * @param path A URI path that may be collapsed or filtered. Only the final
     * URI need to be found, the rest are optional.
     * @return the node with the path final URL
     */
    private boolean expandNodeByFilterablePath(final Node root, final List<Path> path, final int pathIndex, final TreeView view) {
        log.info(id + " findNodeByFilterablePath " + path + " " + pathIndex + " in " + root);
        if (cancelled.get() || root == null || path == null || path.isEmpty() || path.size() <= pathIndex) {
            return false;
        }
        List<Path> rootSubPath = getTreeSubPath(root);
        log.info(id + " comparing to " + rootSubPath + " with " + pathIndex + " from " + path);
        if (!matchesSubPath(path, pathIndex, rootSubPath)) {
            return false;
        }

        if (path.size() == pathIndex + rootSubPath.size()) {
            try {
                log.info(id + " Setting selection " + root);
                em.getExplorerManager().setSelectedNodes(new Node[]{root});
            } catch (PropertyVetoException ex) {
                log.warn(id + " Cannot set selection", ex);
            }
            //found it
            return true;
        }

        Children c = root.getChildren();
        boolean lazy = areChildrenLazy(c);
        boolean loaded = areChildrenLoaded(c);
        if (lazy && !loaded) {
            if (rootIsLoading && this.root == root) {
                log.info(id + " root didn't finish loading, busy waiting...");
            }
            
            log.info(id + " Async expanding " + root);
            //expand, may be called outside AWT
            view.expandNode(root);
            //continue from where we left...
            ph.progress(Bundle.Expanding_Node(root.getDisplayName()));

            asyncPosted = true;
            runAsync(reinstantiate(id + 1, root, em, path, pathIndex, view, ph, true, cancelled));

            return false;
        } else {
            Node[] children = c.getNodes(true);
            log.info(id + " Expanding " + root + " found " + children.length + " children, isLazy " + lazy);
            return expandNodeByFilterablePath(children, path, pathIndex + rootSubPath.size(), view);
        }
    }

    private boolean expandNodeByFilterablePath(Node[] nodes, List<Path> path, int pathIndex, TreeView view) {
        for (Node n : nodes) {
            boolean found = expandNodeByFilterablePath(n, path, pathIndex, view);
            if (found) {
                return true;
            }
            if (asyncPosted) {
                return false;
            }
        }

        return false;
    }

    private static <Path> boolean matchesSubPath(List<Path> path, int pathIndex, List<Path> subPath) {
        for (int i = 0; i < subPath.size(); i++) {
            Path uri = path.get(pathIndex + i);
            Path other = subPath.get(i);
            if (uri == null) {
                if (other != null) {
                    return false;
                }
            } else {
                if (other == null) {
                    return false;
                }
                if (!uri.equals(other)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected abstract boolean areChildrenLazy(Children c);

    protected abstract boolean areChildrenLoaded(Children c);
    
    protected abstract SelectInTreeViewRunnable reinstantiate(int id, Node root, Provider em, List<Path> entityPath, int entityPathIndex, TreeView view, ProgressHandle ph, boolean rootIsLoading, AtomicBoolean cancelled);

    protected abstract List<Path> getTreeSubPath(Node root);
    
    protected abstract void runAsync(Runnable r);

}
