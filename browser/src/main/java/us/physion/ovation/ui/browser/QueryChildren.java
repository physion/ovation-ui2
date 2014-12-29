package us.physion.ovation.ui.browser;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import java.util.*;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
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
    private final HashMultimap<String,List<IEntityWrapper>> pathMap = HashMultimap.create();

    protected QueryChildren(TreeFilter filter) {
        this.filter = filter;
    }

    protected QueryChildren(Set<List<IEntityWrapper>> paths, TreeFilter filter) {
        this(filter);

        if (paths == null) {
            return;
        }
        for (List<IEntityWrapper> path : paths) {
            addPath(path);
        }
    }

    @Override
    protected Node[] createNodes(IEntityWrapper child) {
        Children children;
        Set<List<IEntityWrapper>> childPaths = pathMap.get(child.getURI());
        if (childPaths == null || childPaths.isEmpty())
        {
            return new Node[]{
                EntityWrapperUtilities.createNewNode(child, new EntityChildrenChildFactory((EntityWrapper) child, filter))
            };
        }else{
            children = new QueryChildren(childPaths, filter);
        }
        return new Node[]{EntityWrapperUtilities.createNode(child, children)};
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

    @SuppressWarnings("null")
    protected void addPath(List<IEntityWrapper> path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        IEntityWrapper child = path.get(path.size()-1);

        if (shouldAdd(child)) {// projects don't belong in source view, and vice versa

            List<IEntityWrapper> childPath = path.subList(0, path.size()-1);
            if (hide(child))
            {
                //TODO: instead of just adding the hidden entity's children to the children set,
                //we should be creating some sort of intermediate hidden node, or some representation
                //of the hidden element in the child key
                addPath(childPath);
            } else {
                boolean childIsNew = pathMap.containsKey(child.getURI());
                
                if (!childPath.isEmpty()) {
                    pathMap.put(child.getURI(), childPath);
                }
                
//                if(childIsNew) {
                    keys.add(child);
                    addNotify();
                    refresh();//in case the node is already created
//                }
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
