package us.physion.ovation.ui.browser;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
public final class QueryChildren extends Children.Keys<IEntityWrapper> {

    private final Set<IEntityWrapper> keys = Sets.newHashSet();
    private final TreeFilter filter;
    private final java.util.Map<String,QueryChildren> children = Maps.newHashMap();

    protected QueryChildren(TreeFilter filter) {
        this.filter = filter;
    }

    protected QueryChildren(Set<List<IEntityWrapper>> paths, TreeFilter filter) {
        this(filter);

        if (paths == null) {
            return;
        }
        
        addPaths(paths);
    }

    Logger logger = LoggerFactory.getLogger(QueryChildren.class);
    @Override
    protected Node[] createNodes(IEntityWrapper key) {
        
        if(children.containsKey(key.getURI())) {
            QueryChildren nodeChildren = children.get(key.getURI());
            
            logger.debug("Creating children node for " + key.getEntity().getClass().getSimpleName() + "(" + key.getURI() + ")");

            return new Node[]{EntityWrapperUtilities.createNode(key, nodeChildren)};
        } else {
            logger.debug("Creating leaf node for " + key.getEntity().getClass().getSimpleName() + "(" + key.getURI() + ")");

            return new Node[]{
                EntityWrapperUtilities.createNewNode(key, new EntityChildrenChildFactory((EntityWrapper) key, filter))
            };
        }        
    }

    @Override
    protected void addNotify() {
        setKeys(keys);
    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }

    protected boolean shouldAdd(IEntityWrapper e) {

        switch (filter.getNavigatorType()) {
            case PROJECT:
                if (Source.class.isAssignableFrom(e.getType())
                        || Protocol.class.isAssignableFrom(e.getType())) {
                    return false;
                }
                break;
            case SOURCE:
                if (Project.class.isAssignableFrom(e.getType())
                        || Protocol.class.isAssignableFrom(e.getType())) {
                    return false;
                }
                break;
            case PROTOCOL:
                if (Project.class.isAssignableFrom(e.getType())
                        || Source.class.isAssignableFrom(e.getType())) {
                    return false;
                }
                break;
        }

        return true;

    }

    protected void addPaths(Collection<List<IEntityWrapper>> paths) {
        for (List<IEntityWrapper> path : paths) {
            addPath(path);
        }
        
        addNotify();
        refresh();//in case the node is already created
    }
    
    @SuppressWarnings("null")
    private void addPath(List<IEntityWrapper> path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        IEntityWrapper root = path.get(path.size() - 1);

        if (shouldAdd(root)) {// projects don't belong in source view, and vice versa

            List<IEntityWrapper> childPath = path.subList(0, path.size() - 1);
            if (hide(root)) {
                //TODO: instead of just adding the hidden entity's children to the children set,
                //we should be creating some sort of intermediate hidden node, or some representation
                //of the hidden element in the child key
                addPath(childPath);
            } else {
                
                if (!childPath.isEmpty()) {
                    if(!children.containsKey(root.getURI())) {
                        children.put(root.getURI(), new QueryChildren(filter));
                    }
                    
                    Collection<List<IEntityWrapper>> childPaths = Sets.newHashSet();
                    childPaths.add(childPath);
                    children.get(root.getURI()).addPaths(childPaths);
                }

                keys.add(root);
            }
        }
    }

    private boolean hide(IEntityWrapper child) {
        if (Experiment.class.isAssignableFrom(child.getType()) && !filter.isExperimentsVisible()) {
            return true;
        } else if (EpochGroup.class.isAssignableFrom(child.getType()) && !filter.isEpochGroupsVisible()) {
            return true;
        } else if (Epoch.class.isAssignableFrom(child.getType()) && !filter.isEpochsVisible()) {
            return true;
        }
        return false;
    }
}
