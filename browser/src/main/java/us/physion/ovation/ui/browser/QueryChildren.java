/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.util.*;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Source;

import us.physion.ovation.ui.interfaces.IEntityWrapper;


/**
 *
 * @author huecotanks
 */
public class QueryChildren extends Children.Keys<IEntityWrapper> {

    Set<IEntityWrapper> keys = new HashSet<IEntityWrapper>();
    private boolean projectView;
    private HashMap<String, Set<List<IEntityWrapper>>> pathMap = new HashMap<String, Set<List<IEntityWrapper>>>();

    protected QueryChildren(boolean pView) {
        projectView = pView;
    }

    protected QueryChildren(Set<List<IEntityWrapper>> paths, boolean pView) {
        this(pView);

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
            children = new EntityChildren((EntityWrapper)child);
        }else{
            children = new QueryChildren(childPaths, projectView);
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
        if (projectView) {
            if (e.getType().isAssignableFrom(Source.class)) {
                return false;
            }
        } else {
            if (e.getType().isAssignableFrom(Project.class)) {
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
            Set<List<IEntityWrapper>> paths = pathMap.get(child.getURI());
            boolean childIsNew = paths == null;
            if (childIsNew)
            {
                paths = new HashSet<List<IEntityWrapper>>();
            }
            
            List<IEntityWrapper> childPath = path.subList(0, path.size()-1);
            if (!childPath.isEmpty())
                paths.add(childPath);
            pathMap.put(child.getURI(), paths);
            if (childIsNew){
                keys.add(child);
                addNotify();
                refresh();//in case the node is already created
            }
        }
    }
}
