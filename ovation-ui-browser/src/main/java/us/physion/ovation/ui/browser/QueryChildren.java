/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.util.*;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import ovation.Project;
import ovation.Source;

import us.physion.ovation.ui.interfaces.IEntityWrapper;


/**
 *
 * @author huecotanks
 */
public class QueryChildren extends Children.Keys<IEntityWrapper> {

    Set<IEntityWrapper> keys = new HashSet<IEntityWrapper>();
    private boolean projectView;
    private HashMap<String, Set<Stack<IEntityWrapper>>> pathMap = new HashMap<String, Set<Stack<IEntityWrapper>>>();

    protected QueryChildren(boolean pView) {
        projectView = pView;
    }

    protected QueryChildren(Set<Stack<IEntityWrapper>> paths, boolean pView) {
        this(pView);

        if (paths == null) {
            return;
        }
        for (Stack<IEntityWrapper> path : paths) {
            addPath(path);
        }
    }
  
    @Override
    protected Node[] createNodes(IEntityWrapper child) {
        Children children;
        Set<Stack<IEntityWrapper>> childPaths = pathMap.get(child.getURI());
        if (childPaths == null || childPaths.isEmpty())
        {
            children = new EntityChildren((EntityWrapper)child, projectView, null);
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

    protected void addPath(Stack<IEntityWrapper> path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        IEntityWrapper child = path.pop();

        if (shouldAdd(child)) {// projects don't belong in source view, and vice versa
            
            Set<Stack<IEntityWrapper>> paths = pathMap.get(child.getURI());
            boolean childIsNew = paths == null;
            if (paths == null)
            {
                paths = new HashSet<Stack<IEntityWrapper>>();
            }
            
            if (!path.isEmpty())
                paths.add(path);
            pathMap.put(child.getURI(), paths);
            if (childIsNew){
                keys.add(child);
                addNotify();
                refresh();//in case the node is already created
            }
        }
    }
}
