/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.mixin.Owned;
import us.physion.ovation.domain.mixin.ProcedureElement;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author jackie
 */
public class QuerySet {
    Set<IEntityWrapper> results;
    Map<IEntityWrapper, Node> nodeCache;
    public QuerySet()
    {
        results = new HashSet();
        nodeCache = new HashMap();
        for(ExplorerManager em : BrowserUtilities.registeredViewManagers.keySet())
        {
            em.setRootContext(new EntityNode(new QueryChildren(BrowserUtilities.registeredViewManagers.get(em).isProjectView()), null));
        }
    }
    public void add(OvationEntity e)
    {
        EntityWrapper ew = new EntityWrapper(e);
        results.add(ew);
        
        List<IEntityWrapper> p = Lists.<IEntityWrapper>newArrayList(ew);
        Set<List<IEntityWrapper>> paths = getPathsToEntity(e, p);// set of paths from this entity wrapper to a parent that has already been created in the tree


        for (List<IEntityWrapper> path : paths) {
            //walk up each path, till we get to a node that already exists

            int parentIndex = findParent(path);
            if (parentIndex < path.size())//parent exists
            {
                Node parent = nodeCache.get(path.get(parentIndex));
                QueryChildren q = (QueryChildren) (parent.getChildren());
                q.addPath(path.subList(0, parentIndex + 1));
            } else {
                for (ExplorerManager mgr : BrowserUtilities.registeredViewManagers.keySet()) {
                    Node parent = mgr.getRootContext();
                    QueryChildren q = (QueryChildren) (parent.getChildren());
                    q.addPath(path);
                }
            }
        }
    }
    
    protected int findParent(List<IEntityWrapper> path)
    {
        int found = path.size();
        for (int i=(path.size()-1); i>= 0; i--)
        {
            if (!nodeCache.containsKey(path.get(i)))
                break;
            else
                found = i;
        }
        return found;
    }

    protected static Set<List<IEntityWrapper>> getPathsToEntity(OvationEntity e, List<IEntityWrapper> path) {
        Set<List<IEntityWrapper>> paths = new HashSet<List<IEntityWrapper>>();

        if (isPerUser(e)) {
            path.add(new PerUserEntityWrapper(((Owned) e).getOwner().getUsername(), ((Owned) e).getOwner().getURI().toString()));
        }

        Set<OvationEntity> parents = getParents(e, path);
        if (parents.isEmpty()) {
            paths.add(path);
            return paths;
        }

        for (OvationEntity parent : parents) {
            List newPath = Lists.newLinkedList(path);
            newPath.add(new EntityWrapper(parent));
            paths.addAll(getPathsToEntity(parent, newPath));
        }

        return paths;

    }

    private static Set<OvationEntity> getParents(OvationEntity entity, List<IEntityWrapper> path) {
        Set<OvationEntity> parents = new HashSet();
        Class type = entity.getClass();
        if (Source.class.isAssignableFrom(type)) {
            parents.addAll(Sets.newHashSet(((Source) entity).getParentSources()));
        } else if (Experiment.class.isAssignableFrom(type)) {
            for (Project p : ((Experiment) entity).getProjects()) {
                parents.add(p);
            }
        } else if (EpochGroup.class.isAssignableFrom(type)) {
            parents.add(((EpochGroup) entity).getParent());
        } else if (Epoch.class.isAssignableFrom(type)) {
            parents.add(((Epoch) entity).getParent());
        } else if (Measurement.class.isAssignableFrom(type)) {
            Epoch epoch = ((Measurement) entity).getEpoch();
            parents.add(epoch);

            for (String source : ((Measurement) entity).getSourceNames()) {
                parents.add(epoch.getInputSources().get(source));
            }
        } else if (AnalysisRecord.class.isAssignableFrom(type)) {
            parents.add(((AnalysisRecord) entity).getParent());
        }
        return parents;
    }

    private static boolean isPerUser(OvationEntity e) {
        if (e instanceof AnalysisRecord) {
            return true;
        }
        return false;
    }
}
