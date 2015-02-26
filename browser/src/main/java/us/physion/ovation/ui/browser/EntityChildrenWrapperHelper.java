/*
 * Copyright (C) 2014 Physion LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package us.physion.ovation.ui.browser;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochContainer;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Folder;
import us.physion.ovation.domain.FolderContainer;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.domain.Revision;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.mixin.AnalysisAnnotatable;
import us.physion.ovation.domain.mixin.ProcedureElement;

public class EntityChildrenWrapperHelper {

    private final TreeFilter filter;
    private final BusyCancellable cancel;

    public EntityChildrenWrapperHelper(TreeFilter filter, BusyCancellable cancel) {
        this.filter = filter;
        this.cancel = cancel;
    }

    private void absorbFilteredChildren(EntityWrapper pop, boolean isVisible, List<EntityWrapper> list, DataContext c, /* @Nullable*/ ProgressHandle ph, EntityComparator entityComparator) {
        if (!isVisible) {
            List<URI> filteredParents = new ArrayList<>();
            filteredParents.addAll(pop.getFilteredParentURIs());
            URI u = null;
            try {
                u = pop.getURI() == null ? null : new URI(pop.getURI());
            } catch (URISyntaxException ex) {
            }
            filteredParents.add(u);

            List<EntityWrapper> children;
            if (pop instanceof PreloadedEntityWrapper) {
                children = filterVisible(((PreloadedEntityWrapper) pop).getChildren(), ph);
            } else {
                children = createKeysForEntity(c, pop, ph);
            }
            for (EntityWrapper e : children) {
                e.addFilteredParentURIs(filteredParents);
            }
            //XXX: This is basically list.addAll(children). It seems list.addAll doesn't refresh the UI (bug in the org.openide.nodes.AsynchChildren LinkedList subclass?) but list.add() does.
            for (EntityWrapper e : children) {
                list.add(e);
            }
        } else {
            list.add(pop);
        }
    }

    protected List<EntityWrapper> createKeysForEntity(DataContext c, EntityWrapper ew) {
        return createKeysForEntity(c, ew, null);
    }

    private List<EntityWrapper> createKeysForEntity(DataContext c, EntityWrapper ew, /* @Nullable*/ ProgressHandle ph) {
        return createKeysForEntity(new LinkedList<>(), c, ew, ph);
    }

    public List<EntityWrapper> createKeysForEntity(List<EntityWrapper> list, DataContext c, EntityWrapper ew, /* @Nullable*/ ProgressHandle ph) {
        if (ew instanceof PreloadedEntityWrapper) {
            EntityComparator entityComparator = new EntityComparator();
            for (EntityWrapper child : ((PreloadedEntityWrapper) ew).getChildren()) {
                absorbFilteredChildren(child, isVisible(child, filter), list, c, ph, entityComparator);
            }
            return list;
        }

        if (cancel.isCancelled()) {
            return list;
        }

        if (ew != null) {
            Class entityClass = ew.getType();
            if (Project.class.isAssignableFrom(entityClass)) {
                Project entity = (Project) ew.getEntity();
                addFolders(list, entity, ph);
                addExperiments(list, entity, ph);
                addAnalysisRecords(list, entity, ph);
            } else if (Source.class.isAssignableFrom(entityClass)) {
                Source entity = (Source) ew.getEntity();
                addChildrenSources(list, entity, ph);
                addProcedureElements(list, entity, ph);
            } else if (Experiment.class.isAssignableFrom(entityClass)) {
                Experiment entity = (Experiment) ew.getEntity();
                addEpochGroups(list, entity, ph);
                addEpochs(list, entity, ph);
            } else if (EpochGroup.class.isAssignableFrom(entityClass)) {
                EpochGroup entity = (EpochGroup) ew.getEntity();
                addEpochGroups(list, entity, ph);
                addEpochs(list, entity, ph);
            } else if (Epoch.class.isAssignableFrom(entityClass)) {
                Epoch entity = (Epoch) ew.getEntity();
                addMeasurements(list, entity, ph);
                addAnalysisRecords(list, entity, ph);
            } else if (AnalysisRecord.class.isAssignableFrom(entityClass)) {
                AnalysisRecord entity = (AnalysisRecord) ew.getEntity();
                addOutputs(list, entity, ph);
            } else if (Protocol.class.isAssignableFrom(entityClass)) {
                Protocol entity = ew.getEntity(Protocol.class);
                addProcedureElements(list, entity, ph);
            } else if (Folder.class.isAssignableFrom(entityClass)) {
                Folder f = ew.getEntity(Folder.class);
                addContents(list, f, ph);
                addAnalysisRecords(list, f, ph);
            } else if (Resource.class.isAssignableFrom(entityClass)) {
                Resource r = (Resource)ew.getEntity();
                addRevisions(list, r, ph);
            }
        }
        if (cancel.isCancelled()) {
            //dummy value
            if (!list.contains(EntityWrapper.EMPTY)) {
                list.add(EntityWrapper.EMPTY);
            }
        }
        return list;
    }

    private List<Experiment> sortedExperiments(Project entity) {
        List<Experiment> experiments = Lists.newArrayList(entity.getExperiments());
        Collections.sort(experiments, (Experiment o1, Experiment o2) -> {
            if (o1 == null || o2 == null
                    || o1.getStart() == null || o2.getStart() == null) {
                return 0;
            }

            return o1.getStart().compareTo(o2.getStart());
        });
        return experiments;
    }

    private List<EpochGroup> sortedEpochGroups(Experiment entity) {
        List<EpochGroup> epochGroups = Lists.newArrayList(entity.getEpochGroups());
        Collections.sort(epochGroups, new Comparator<EpochGroup>() {
            @Override
            public int compare(EpochGroup o1, EpochGroup o2) {
                if (o1 == null || o2 == null
                        || o1.getStart() == null || o2.getStart() == null) {
                    return 0;
                }

                return o1.getStart().compareTo(o2.getStart());
            }
        });
        return epochGroups;
    }

    private List<Epoch> sortedEpochs(Protocol entity) {
        List<Epoch> epochs = Lists.newArrayList(Iterables.filter(entity.getProcedures(),
                Epoch.class));

        return sortEpochList(epochs);
    }

    private List<Epoch> sortEpochList(List<Epoch> epochs) {
        Collections.sort(epochs, new Comparator<Epoch>() {
            @Override
            public int compare(Epoch o1, Epoch o2) {
                if (o1 == null || o2 == null
                        || o1.getStart() == null || o2.getStart() == null) {
                    return 0;
                }

                return o1.getStart().compareTo(o2.getStart());
            }
        });
        return epochs;
    }

    private List<Epoch> sortedEpochs(Source entity) {
        List<Epoch> epochs = Lists.newArrayList(Lists.newLinkedList(entity.getEpochs()));
        return sortEpochList(epochs);
    }

    private List<Epoch> sortedEpochs(EpochContainer entity) {
        List<Epoch> epochs = Lists.newArrayList(Lists.newLinkedList(entity.getEpochs()));
        return sortEpochList(epochs);
    }

    private List<? extends EntityWrapper> getTopLevelProcedureElements(TreeFilter filter, Iterable<? extends ProcedureElement> procedures) {
        HashMap<String, EntityWrapper> visibleProcedureElements = Maps.newHashMap();
        List<EntityWrapper> experiments = Lists.newLinkedList();
        for (ProcedureElement e : procedures) {
            ProcedureElement localParent;
            if (e instanceof EpochGroup) { //TODO getParent() should be a ProcedureElement method
                localParent = ((EpochGroup) e).getParent();
            } else if (e instanceof Epoch) {
                localParent = ((Epoch) e).getParent();
            } else {
                localParent = null;
            }

            List<OvationEntity> epochGroupChain = Lists.newArrayList();

            if (localParent != null) {
                while (localParent instanceof EpochGroup) {
                    epochGroupChain.add(localParent);
                    localParent = ((EpochGroup) localParent).getParent();
                }
                PreloadedEntityWrapper p;
                if (visibleProcedureElements.containsKey(localParent.getURI().toString())) {
                    p = (PreloadedEntityWrapper) visibleProcedureElements.get(localParent.getURI().toString());
                } else {
                    p = new PreloadedEntityWrapper(localParent);
                }
                p.addChildren(filter, epochGroupChain, e);
                visibleProcedureElements.put(p.getURI(), p);
            } else { //Experiment
                EntityWrapper p = new EntityWrapper(e);
                experiments.add(p);
            }
        }

        //sort values, and add the
        List<EntityWrapper> children = Lists.newArrayList(visibleProcedureElements.values());
        children.addAll(experiments);
        Collections.sort(children, new EntityComparator<EntityWrapper>());
        return children;
    }

    private void addAnalysisRecords(List<EntityWrapper> list, AnalysisAnnotatable entity, ProgressHandle ph) {
        if (ph != null) {
            ph.switchToIndeterminate();
        }
        List<User> users = Lists.newArrayList(((OvationEntity)entity).getDataContext().getUsers());
        Collections.sort(users,
                (User t, User t1) -> t.getUsername().compareTo(t1.getUsername()));

        for (User user : users) {
            List<EntityWrapper> l = Lists.newArrayList(
                    Iterables.transform(entity.getUserAnalysisRecords(user).values(),
                            (AnalysisRecord f) -> new EntityWrapper(f)));
            if (l.size() > 0) {
                list.add(new PerUserEntityWrapper(user.getUsername(), user.getURI().toString(), l));
            }

        }
    }

    private void addAnalysisRecords(List<EntityWrapper> list, Epoch entity, ProgressHandle ph) {
        if (ph != null) {
            ph.switchToIndeterminate();
        }
        List<User> users = Lists.newArrayList(entity.getDataContext().getUsers());
        
        Collections.sort(users, (User t, User t1) -> t.getUsername().compareTo(t1.getUsername()));
        
        for (User user : users) {
            List<EntityWrapper> l = Lists.newArrayList(Iterables.transform(entity.getUserAnalysisRecords(user).values(), 
                    (AnalysisRecord f) -> new EntityWrapper(f)));
            if (l.size() > 0) {
                list.add(new PerUserEntityWrapper(user.getUsername(), user.getURI().toString(), l));
            }

        }
    }

    private void addOutputs(List<EntityWrapper> list, AnalysisRecord entity, ProgressHandle ph) {
        for (Revision d : entity.getOutputs().values()) {
            list.add(new EntityWrapper(d));
        }
    }

    private void addFolders(List<EntityWrapper> list, Project entity, ProgressHandle ph) {
        if (cancel.isCancelled()) {
            return;
        }

        EntityComparator entityComparator = new EntityComparator();
        List<Folder> folders = Lists.newArrayList(((FolderContainer) entity).getFolders());

        int progressCounter = 0;
        if (ph != null) {
            ph.switchToDeterminate(folders.size());
        }

        for (Folder f : folders) {
            if (cancel.isCancelled()) {
                return;
            }

            list.add(new EntityWrapper(f));

            if (ph != null) {
                ph.progress(progressCounter++);
            }
        }

        list.sort(entityComparator);
    }

    private void addExperiments(List<EntityWrapper> list, Project entity, ProgressHandle ph) {
        if (cancel.isCancelled()) {
            return;
        }

        EntityComparator entityComparator = new EntityComparator();
        List<Experiment> experiments = sortedExperiments(entity);

        int progressCounter = 0;
        if (ph != null) {
            ph.switchToDeterminate(experiments.size());
        }

        for (Experiment e : experiments) {
            if (cancel.isCancelled()) {
                return;
            }
            absorbFilteredChildren(new EntityWrapper(e), filter.isExperimentsVisible(), list, entity.getDataContext(), ph, entityComparator);

            if (ph != null) {
                ph.progress(progressCounter++);
            }
        }
    }

    private void addEpochGroups(List<EntityWrapper> list, Experiment entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        for (EpochGroup eg : sortedEpochGroups(entity)) {
            absorbFilteredChildren(new EntityWrapper(eg), filter.isEpochGroupsVisible(), list, entity.getDataContext(), ph, entityComparator);
        }
    }

    private void addEpochGroups(List<EntityWrapper> list, EpochGroup entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        for (EpochGroup eg : entity.getEpochGroups()) {
            absorbFilteredChildren(new EntityWrapper(eg), filter.isEpochGroupsVisible(), list, entity.getDataContext(), ph, entityComparator);
        }
    }

    private void addEpochs(List<EntityWrapper> list, Experiment entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        for (Epoch e : sortedEpochs(entity)) {
            absorbFilteredChildren(new EntityWrapper(e), filter.isEpochsVisible(), list, entity.getDataContext(), ph, entityComparator);
        }
    }

    private void addEpochs(List<EntityWrapper> list, EpochGroup entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        if (ph != null) {
            ph.progress(Bundle.Loading_Epochs());
        }
        try {
            for (Epoch e : sortedEpochs(entity)) {
                absorbFilteredChildren(new EntityWrapper(e), filter.isEpochsVisible(), list, entity.getDataContext(), ph, entityComparator);
            }
        } finally {
            if (ph != null) {
                ph.progress(Bundle.Loading_Epochs_Done());
            }
        }
    }

    private void addMeasurements(List<EntityWrapper> list, Epoch entity, ProgressHandle ph) {
        for (Measurement m : entity.getMeasurements()) {
            list.add(new EntityWrapper(m));
        }
    }

    private void addChildrenSources(List<EntityWrapper> list, Source entity, ProgressHandle ph) {
        for (Source e : entity.getChildren()) {
            list.add(new EntityWrapper(e));
        }
    }

    private void addProcedureElements(List<EntityWrapper> list, Protocol entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        //if Epoch's parents should be visible
        if (filter.isExperimentsVisible() || filter.isEpochGroupsVisible()) {
            List<? extends EntityWrapper> topLevelProcedureElements = getTopLevelProcedureElements(filter,
                    Iterables.transform(entity.getProcedures(), new Function<OvationEntity, ProcedureElement>() {

                        @Override
                        public ProcedureElement apply(OvationEntity f) {
                            return f == null ? null : (ProcedureElement) f;
                        }
                    }));

            for (EntityWrapper exp : topLevelProcedureElements) {
                absorbFilteredChildren(exp, filter.isExperimentsVisible(), list, entity.getDataContext(), ph, entityComparator);
            }
        } else {
            for (Epoch e : sortedEpochs(entity)) {
                absorbFilteredChildren(new EntityWrapper(e), filter.isEpochsVisible(), list, entity.getDataContext(), ph, entityComparator);
            }

        }

    }

    private void addProcedureElements(List<EntityWrapper> list, Source entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        //if Epoch's parents should be visible
        if (filter.isExperimentsVisible() || filter.isEpochGroupsVisible()) {
            List<? extends EntityWrapper> topLevelProcedureElements = getTopLevelProcedureElements(filter, entity.getEpochs());

            for (EntityWrapper exp : topLevelProcedureElements) {
                absorbFilteredChildren(exp, filter.isExperimentsVisible(), list, entity.getDataContext(), ph, entityComparator);
            }
        } else {
            for (Epoch e : sortedEpochs(entity)) {
                absorbFilteredChildren(new EntityWrapper(e), filter.isEpochsVisible(), list, entity.getDataContext(), ph, entityComparator);
            }
        }
    }

    private List<EntityWrapper> filterVisible(List<EntityWrapper> children, ProgressHandle ph) {
        List<EntityWrapper> visible = new LinkedList();
        for (EntityWrapper child : children) {
            if (isVisible(child, filter)) {
                visible.add(child);
            } else {
                if (child instanceof PreloadedEntityWrapper) {
                    visible.addAll(filterVisible(((PreloadedEntityWrapper) child).getChildren(), ph));
                } else {
                    visible.addAll(createKeysForEntity(child.getEntity().getDataContext(), child, ph));
                }
            }
        }
        return visible;
    }

    private boolean isVisible(EntityWrapper ew, TreeFilter filter) {
        Class entityClass = ew.getType();
        if (Experiment.class.isAssignableFrom(entityClass) && !filter.isExperimentsVisible()) {
            return false;
        } else if (EpochGroup.class.isAssignableFrom(entityClass) && !filter.isEpochGroupsVisible()) {
            return false;
        } else if (Epoch.class.isAssignableFrom(entityClass) && !filter.isEpochsVisible()) {
            return false;
        }
        return true;
    }

    private void addContents(List<EntityWrapper> list, Folder entity, ProgressHandle ph) {
        if (cancel.isCancelled()) {
            return;
        }

        EntityComparator entityComparator = new EntityComparator();
        List<Folder> folders = Lists.newArrayList(entity.getFolders());
        List<Resource> resources = Lists.newArrayList(entity.getResources());
        List<OvationEntity> contents = Lists.newArrayList();
        contents.addAll(folders);
        contents.addAll(resources);

        int progressCounter = 0;
        if (ph != null) {
            ph.switchToDeterminate(folders.size());
        }

        for (OvationEntity e : contents) {
            if (cancel.isCancelled()) {
                return;
            }

            list.add(new EntityWrapper(e));

            if (ph != null) {
                ph.progress(progressCounter++);
            }
        }

        list.sort(entityComparator);
    }

    private void addRevisions(List<EntityWrapper> list, Resource r, ProgressHandle ph) {
        if (cancel.isCancelled()) {
            return;
        }

        EntityComparator entityComparator = new EntityComparator();
        List<Revision> revs = Lists.newArrayList(r.getRevisions());
        
        int progressCounter = 0;
        if (ph != null) {
            ph.switchToDeterminate(revs.size());
        }

        revs.sort((Revision r1, Revision r2) -> {
            if (r1.getVersion() != null && r2.getVersion() != null) {
                return r1.getVersion().compareTo(r2.getVersion());
            }
            
            return r1.getURI().compareTo(r2.getURI());
        });
        
        for (OvationEntity e : revs) {
            if (cancel.isCancelled()) {
                return;
            }

            list.add(new EntityWrapper(e));

            if (ph != null) {
                ph.progress(progressCounter++);
            }
        }

    }

}
