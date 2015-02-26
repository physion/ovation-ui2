/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Folder;
import us.physion.ovation.domain.FolderContainer;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.domain.Revision;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.mixin.Owned;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author jackie
 */
public class QuerySet {

    Set<IEntityWrapper> results;
    Map<IEntityWrapper, Node> nodeCache;

    public QuerySet() {
        results = new HashSet();
        nodeCache = new HashMap();
        for (ExplorerManager em : BrowserUtilities.registeredViewManagers.keySet()) {
            em.setRootContext(new EntityNode(new QueryChildren(BrowserUtilities.registeredViewManagers.get(em)), null));
        }
    }

    public void reset() {
        Set<IEntityWrapper> oldResults = results;
        results = Sets.newHashSet();

        nodeCache = Maps.newHashMap();
        for (ExplorerManager em : BrowserUtilities.registeredViewManagers.keySet()) {
            em.setRootContext(new EntityNode(new QueryChildren(BrowserUtilities.registeredViewManagers.get(em)), null));
        }
        for (IEntityWrapper entity : oldResults) {
            add(entity.getEntity());
        }
    }

    public void reset(ExplorerManager em, TreeFilter filter) {
        Set<IEntityWrapper> oldResults = results;
        results = Sets.newHashSet();

        em.setRootContext(new EntityNode(new QueryChildren(filter), null));
        Set<ExplorerManager> mgrs = Sets.newHashSet(em);
        for (IEntityWrapper entity : oldResults) {
            add(entity.getEntity(), mgrs);
        }
    }

    public void add(OvationEntity e) {
        add(e, BrowserUtilities.registeredViewManagers.keySet());
    }

    Logger logger = LoggerFactory.getLogger(QuerySet.class);

    public void add(OvationEntity e, Set<ExplorerManager> mgrs) {
        logger.info("Adding {} to query set", e); //QUERY: all three Epochs are added
        if (e == null || e.isTrashed()) {
            return;
        }

        EntityWrapper ew = new EntityWrapper(e);
        results.add(ew);

        List<IEntityWrapper> p = Lists.<IEntityWrapper>newArrayList(ew);
        Set<List<IEntityWrapper>> paths = getPathsToEntity(e, p);// set of paths from this entity wrapper to a parent that has already been created in the tree

        for (ExplorerManager mgr : mgrs) {
            Node parent = mgr.getRootContext();
            QueryChildren q = (QueryChildren) (parent.getChildren());

            q.addPaths(paths);

        }
    }

    public static Set<List<IEntityWrapper>> getPathsToEntity(OvationEntity e, List<IEntityWrapper> path) {
        Set<List<IEntityWrapper>> paths = Sets.newHashSet();

        if (isPerUser(e)) {
            path.add(new PerUserEntityWrapper(((Owned) e).getOwner().getUsername(), ((Owned) e).getOwner().getURI().toString()));
        } else {
            path.add(new EntityWrapper(e));
        }

        Set<OvationEntity> parents = getParents(e, path);
        if (parents.isEmpty()) {
            paths.add(path);
        }

        for (OvationEntity parent : parents) {
            List<IEntityWrapper> newPath = Lists.newLinkedList(path);
            newPath.add(new EntityWrapper(parent));
            paths.addAll(getPathsToEntity(parent, newPath));
        }

        return paths;

    }

    private static Set<OvationEntity> getParents(OvationEntity entity, List<IEntityWrapper> path) {
        Set<OvationEntity> parents = Sets.newHashSet();
        Class type = entity.getClass();
        if (Source.class.isAssignableFrom(type)) {
            parents.addAll(Sets.newHashSet(((Source) entity).getParents()));
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
            parents.add(epoch); // QUERY: Does Measurement have an Epoch?

            for (String source : ((Measurement) entity).getSourceNames()) {
                for (Source s : epoch.getInputSources().get(source)) {
                    parents.add(s);
                }
            }
        } else if (AnalysisRecord.class.isAssignableFrom(type)) {
            parents.add(((AnalysisRecord) entity).getParent());
        } else if (Folder.class.isAssignableFrom(type)) {
            for (FolderContainer c : ((Folder) entity).getParents()) {
                parents.add(c);
            }
        } else if (Resource.class.isAssignableFrom(type)) {
            Resource r = ((Resource) entity);
            addResourceParents(r, parents);
        } else if (Revision.class.isAssignableFrom(type)) {
            Revision rev = (Revision) entity;
            parents.addAll(Lists.newLinkedList(rev.getEntities(Revision.RelationshipKeys.UPSTREAM_ANALYSES)));
            parents.add(rev.getResource());
        }
        
        return parents;
    }

    private static void addResourceParents(Resource r, Set<OvationEntity> parents) {
        OvationEntity e = r.getContainingEntity();
        if(e != null) {
            parents.add(e);
        }
        
        parents.addAll(Sets.newHashSet(r.getFolders()));
    }

    private static boolean isPerUser(OvationEntity e) {
        if (e instanceof AnalysisRecord) {
            return true;
        }
        return false;
    }
}
