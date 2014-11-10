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
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.mixin.DataElement;
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
            List<URI> filteredParents = new ArrayList<URI>();
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
            for(EntityWrapper e : children) {
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
        return createKeysForEntity(new LinkedList<EntityWrapper>(), c, ew, ph);
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
        Collections.sort(experiments, new Comparator<Experiment>() {
            @Override
            public int compare(Experiment o1, Experiment o2) {
                if (o1 == null || o2 == null
                        || o1.getStart() == null || o2.getStart() == null) {
                    return 0;
                }

                return o1.getStart().compareTo(o2.getStart());
            }
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

    private List<? extends EntityWrapper> getTopLevelProcedureElements(TreeFilter filter, Iterable<? extends ProcedureElement> epochs) {
        HashMap<String, PreloadedEntityWrapper> visibleProcedureElements = new HashMap<String, PreloadedEntityWrapper>();
        for (ProcedureElement e : epochs) {
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
                    p = visibleProcedureElements.get(localParent.getURI().toString());
                } else {
                    p = new PreloadedEntityWrapper(localParent);
                }
                p.addChildren(filter, epochGroupChain, e);
                visibleProcedureElements.put(p.getURI(), p);
            }
        }

        //sort values, and add the
        List<PreloadedEntityWrapper> children = new ArrayList(visibleProcedureElements.values());
        Collections.sort(children, new EntityComparator<PreloadedEntityWrapper>());
        return children;
    }

    private void addAnalysisRecords(List<EntityWrapper> list, Project entity, ProgressHandle ph) {
        if (ph != null) {
            ph.switchToIndeterminate();
        }
        List<User> users = Lists.newArrayList(entity.getDataContext().getUsers());
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User t, User t1) {
                return t.getUsername().compareTo(t1.getUsername());
            }
        });
        for (User user : users) {
            List<EntityWrapper> l = Lists.newArrayList(Iterables.transform(entity.getUserAnalysisRecords(user).values(),
                    new Function<AnalysisRecord, EntityWrapper>() {
                        @Override
                        public EntityWrapper apply(AnalysisRecord f) {
                            return new EntityWrapper(f);
                        }
                    }));
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
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User t, User t1) {
                return t.getUsername().compareTo(t1.getUsername());
            }
        });
        for (User user : users) {
            List<EntityWrapper> l = Lists.newArrayList(Iterables.transform(entity.getUserAnalysisRecords(user).values(),
                    new Function<AnalysisRecord, EntityWrapper>() {
                        @Override
                        public EntityWrapper apply(AnalysisRecord f) {
                            return new EntityWrapper(f);
                        }
                    }));
            if (l.size() > 0) {
                list.add(new PerUserEntityWrapper(user.getUsername(), user.getURI().toString(), l));
            }

        }
    }

    private void addOutputs(List<EntityWrapper> list, AnalysisRecord entity, ProgressHandle ph) {
        for (DataElement d : entity.getOutputs().values()) {
            list.add(new EntityWrapper(d));
        }
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
        DataContext c = entity.getDataContext();
        EntityComparator entityComparator = new EntityComparator();
        if (ph != null) {
            ph.progress(Bundle.Loading_Epochs());
        }
        c.beginTransaction();//we wrap these in a transaction, because there may be a lot of epochs
        try {
            for (Epoch e : sortedEpochs(entity)) {
                absorbFilteredChildren(new EntityWrapper(e), filter.isEpochsVisible(), list, c, ph, entityComparator);
            }
        } finally {
            c.commitTransaction();
            if (ph != null) {
                ph.progress(Bundle.Loading_Epochs_Done());
            }
        }
    }

    private void addMeasurements(List<EntityWrapper> list, Epoch entity, ProgressHandle ph) {
        DataContext c = entity.getDataContext();
        c.beginTransaction();
        try {
            for (Measurement m : entity.getMeasurements()) {
                list.add(new EntityWrapper(m));
            }
        } finally {
            c.commitTransaction();
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

}
