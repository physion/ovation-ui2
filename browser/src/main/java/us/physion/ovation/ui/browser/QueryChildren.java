/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.util.*;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Source;

import us.physion.ovation.ui.interfaces.IEntityWrapper;


/**
 *
 * @author huecotanks
 */
public class QueryChildren extends Children.Keys<IEntityWrapper> {

    Set<IEntityWrapper> keys = new HashSet<IEntityWrapper>();
    private TreeFilter filter;
    private HashMap<String, Set<List<IEntityWrapper>>> pathMap = new HashMap<String, Set<List<IEntityWrapper>>>();

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
            children = new EntityChildren((EntityWrapper)child, filter);
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
        if (filter.isProjectView()) {
            if (Source.class.isAssignableFrom(e.getType())) {
                return false;
            }
        } else {
            if (Project.class.isAssignableFrom(e.getType())) {
                return false;
            }
        }
        return true;
    }

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
                Set<List<IEntityWrapper>> paths = pathMap.get(child.getURI());
                boolean childIsNew = paths == null;
                if (childIsNew) {
                    paths = new HashSet<List<IEntityWrapper>>();
                }

                if (!childPath.isEmpty()) {
                    paths.add(childPath);
                }
                pathMap.put(child.getURI(), paths);
                if (childIsNew) {
                    keys.add(child);
                    addNotify();
                    refresh();//in case the node is already created
                }
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
