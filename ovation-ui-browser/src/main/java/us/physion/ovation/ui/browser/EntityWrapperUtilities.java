/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import us.physion.ovation.ui.browser.insertion.InsertProject;
import java.beans.PropertyVetoException;
import java.util.*;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import ovation.*;
import us.physion.ovation.ui.browser.insertion.InsertSource;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
public class EntityWrapperUtilities {

    private static String SEPARATOR = ";";

    protected static Set<IEntityWrapper> createNodesFromQuery(Set<ExplorerManager> mgrs, Iterator<IEntityBase> itr) {
        Map<String, Node> treeMap = BrowserUtilities.getNodeMap();
        Set<IEntityWrapper> resultSet = new HashSet<IEntityWrapper>();
        while (itr.hasNext()) {
            IEntityBase e = itr.next();
            IEntityWrapper ew = new EntityWrapper(e);
            resultSet.add(ew);

            Stack<IEntityWrapper> p = new Stack();
            p.push(ew);
            //TODO: getParentsInTree could stop before hitting the top-level projects and sources
            Set<Stack<IEntityWrapper>> paths = getParentsInTree(e, p);// set of paths from this entity wrapper to a parent that has already been created in the tree

            for (Stack<IEntityWrapper> path : paths) {
                Node parentInTree = treeMap.get(path.peek());
                if (parentInTree == null) {
                    for (ExplorerManager mgr : mgrs) {
                        Stack<IEntityWrapper> copiedPath;
                        //QueryChildren.addPath() modifies path
                        if (mgrs.size() == 1) {
                            copiedPath = path;
                        } else {
                            copiedPath = new Stack<IEntityWrapper>();
                            for (int i = 0; i < path.size(); i++) {
                                copiedPath.push(path.get(i));
                            }
                        }
                        parentInTree = mgr.getRootContext();
                        QueryChildren q = (QueryChildren) (parentInTree.getChildren());
                        q.addPath(copiedPath);
                    }
                } else {
                    path.pop();
                    QueryChildren q = (QueryChildren) (parentInTree.getChildren());
                    q.addPath(path);
                }

            }
        }
        return resultSet;
    }

    //TODO use the BrowserUtilites.getNodeMap() to stop early
    protected static Set<Stack<IEntityWrapper>> getParentsInTree(IEntityBase e, Stack<IEntityWrapper> path) {
        Set<Stack<IEntityWrapper>> paths = new HashSet<Stack<IEntityWrapper>>();

        if (isPerUser(e)) {
            path.push(new PerUserEntityWrapper(e.getOwner().getUsername(), e.getOwner().getURIString()));
        }

        Set<IEntityBase> parents = getParents(e, path);
        if (parents.isEmpty()) {
            paths.add(path);
            return paths;
        }

        for (IEntityBase parent : parents) {
            Stack newPath = new Stack();
            for (int i = 0; i < path.size(); i++) {
                newPath.push(path.get(i));
            }
            newPath.push(new EntityWrapper(parent));
            paths.addAll(getParentsInTree(parent, newPath));
        }

        return paths;

    }

    //entity....... the entity whose parents we find
    //path......... the path from result set object to entity (for getting Sources from EpochGroups)
    private static Set<IEntityBase> getParents(IEntityBase entity, Stack<IEntityWrapper> path) {
        Set<IEntityBase> parents = new HashSet();
        Class type = entity.getClass();
        if (type.isAssignableFrom(Source.class)) {
            Source parent = ((Source) entity).getParent();
            if (parent != null) {
                parents.add(parent);
            }
        } else if (type.isAssignableFrom(Experiment.class)) {
            for (Project p : ((Experiment) entity).getProjects()) {
                parents.add(p);
            }

            boolean epochGroupsInPath = false;
            for (IEntityWrapper ew : path)
            {
                if (ew.getType().isAssignableFrom(EpochGroup.class))
                {
                    epochGroupsInPath = true;
                    Source s = ((EpochGroup)ew.getEntity()).getSource();
                    if (s != null)
                        parents.add(s);
                }
            }
            if (!epochGroupsInPath)
            {
                for (Source p : ((Experiment) entity).getSources()) {
                    parents.add(p);
                }
            }
        } else if (type.isAssignableFrom(EpochGroup.class)) {
            EpochGroup parent = ((EpochGroup) entity).getParent();
            if (parent == null) {
                parents.add(((EpochGroup) entity).getExperiment());
            } else {
                parents.add(parent);
            }
        } else if (type.isAssignableFrom(Epoch.class)) {
            parents.add(((Epoch) entity).getEpochGroup());
        } else if (type.isAssignableFrom(Response.class)) {
            parents.add(((Response) entity).getEpoch());
        } else if (type.isAssignableFrom(Stimulus.class)) {
            parents.add(((Stimulus) entity).getEpoch());
        } else if (type.isAssignableFrom(DerivedResponse.class)) {
            parents.add(((DerivedResponse) entity).getEpoch());
        } else if (type.isAssignableFrom(AnalysisRecord.class)) {
            parents.add(((AnalysisRecord) entity).getProject());
        }
        return parents;
    }

    private static boolean isPerUser(IEntityBase e) {
        if (e instanceof AnalysisRecord
                || e instanceof DerivedResponse) {
            return true;
        }
        return false;
    }

    protected static Node createNode(IEntityWrapper key, Children c) {
        
        boolean forceCreateNode = false;
        if (key instanceof PerUserEntityWrapper)
        {
            //per user entity wrappers are "user" nodes
            //their uri's correspond to the "User" object they represent, and therefore
            //are not unique nodes
            forceCreateNode = true;
        }
        
        Map<String, Node> treeMap = BrowserUtilities.getNodeMap();
        String uri = key.getURI();
        if (!forceCreateNode) {//If node with uri exists, create a node that just proxies the existing node
            if (uri != null && treeMap.containsKey(uri)) {
                return new FilterNode(treeMap.get(uri));
            }
        }

        //otherwise, create an AbstractNode representing this object
        EntityNode n = new EntityNode(c, Lookups.singleton(key), key);
        n.setDisplayName(key.getDisplayName());
        setIconForType(n, key.getType());
        if (uri != null) {//TODO: test this for nodes that don't have uris
            treeMap.put(key.getURI(), n);
        }
        return n;
    }

    protected static void setIconForType(AbstractNode n, Class entityClass) {
        if (entityClass.isAssignableFrom(Source.class)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/source.png");
        } else if (entityClass.isAssignableFrom(Project.class)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/project.png");
        } else if (entityClass.isAssignableFrom(Experiment.class)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/experiment.png");
        } else if (entityClass.isAssignableFrom(EpochGroup.class)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/epochGroup.png");
        } else if (entityClass.isAssignableFrom(Epoch.class)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/epoch.png");
        } else if (entityClass.isAssignableFrom(AnalysisRecord.class)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/analysis-record.png");
        } else if (entityClass.isAssignableFrom(Response.class) || entityClass.isAssignableFrom(URLResponse.class)){
           n.setIconBaseWithExtension("us/physion/ovation/ui/browser/response.png"); 
        } else if (entityClass.isAssignableFrom(Stimulus.class) ){
           n.setIconBaseWithExtension("us/physion/ovation/ui/browser/stimulus.png"); 
        } else if (entityClass.isAssignableFrom(User.class) ){
           n.setIconBaseWithExtension("us/physion/ovation/ui/browser/user.png"); 
        }
    }
}
