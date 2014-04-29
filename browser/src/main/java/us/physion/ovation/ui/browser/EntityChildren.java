package us.physion.ovation.ui.browser;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.*;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.mixin.ProcedureElement;
import us.physion.ovation.ui.interfaces.LazyChildren;

/**
 *
 * @author huecotanks
 */
@Messages({
    "# {0} - parent display name",
    "Loading_Entity_Children=Loading data for {0}",
    "Loading_Epochs=Loading epochs",
    "Loading_Epochs_Done=Done loading epochs"
})
public class EntityChildren extends Children.Keys<EntityWrapper> implements LazyChildren {

    EntityWrapper parent;
    boolean projectView;
    private final TreeFilter filter;
    private boolean loadedKeys = false;
    private int childcount = 0;
    private final int UPDATE_FACTOR = 1;

    public EntityChildren(EntityWrapper e) {
        this(e, TreeFilter.NO_FILTER);
    }

    public EntityChildren(EntityWrapper e, TreeFilter filter) {
        if (e == null) {
            throw new OvationException("Pass in the list of Project/Source EntityWrappers, instead of null");
        }

        parent = e;
        this.filter = filter;
        //if its per user, we create
        if (e instanceof PerUserEntityWrapper) {
            loadedKeys = true;
            setKeys(((PerUserEntityWrapper) e).getChildren());
        } else {
            initKeys();
        }
    }

    public EntityChildren(List<EntityWrapper> children) {
        this(children, TreeFilter.NO_FILTER);
    }

    public EntityChildren(List<EntityWrapper> children, TreeFilter filter) {
        parent = null;
        this.filter = filter;
        updateWithKeys(children);
    }

    @Override
    public boolean isLoaded() {
        return super.isInitialized() && loadedKeys;
    }

    protected Callable<Children> getChildrenCallable(final EntityWrapper key) {
        return new Callable<Children>() {
            @Override
            public Children call() throws Exception {
                return new EntityChildren(key, filter);
            }
        };
    }

    @Override
    protected Node[] createNodes(final EntityWrapper key) {
        return new Node[]{EntityWrapperUtilities.createNode(key, Children.createLazy(getChildrenCallable(key)))};
    }

    protected void updateWithKeys(final List<EntityWrapper> list) {
        EventQueueUtilities.runOnEDT(new Runnable() {
            @Override
            public void run() {
                setKeys(list);
                addNotify();
                refresh();
                loadedKeys = true;
            }
        });
    }

    public void resetNode() {
        loadedKeys = false;
        initKeys();
    }

    protected void initKeys() {
        final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Loading_Entity_Children(parent.getDisplayName()));

        EventQueueUtilities.runOffEDT(new Runnable() {
            @Override
            public void run() {
                ph.switchToIndeterminate();
                createKeys(ph);
            }
        }, ph);
    }

    protected void createKeys(ProgressHandle ph) {
        DataContext c = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        if (c == null) {
            return;
        }
        updateWithKeys(createKeysForEntity(c, parent, ph));
    }

    private void absorbFilteredChildren(boolean isVisible, List<EntityWrapper> list, DataContext c, /* @Nullable*/ ProgressHandle ph, EntityComparator entityComparator) {
        if (!isVisible) {
            EntityWrapper pop = list.remove(list.size() - 1);
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
            list.addAll(children);
        } else {
            displayUpdatedList(list, entityComparator);
        }
    }

    private void displayUpdatedList(List<EntityWrapper> list, Comparator entityComparator) {
        if (childcount % UPDATE_FACTOR == 0) {
            for (int i = 0; i < Math.min(UPDATE_FACTOR, list.size()); i++) {
                EntityWrapper last = list.get(list.size() - 1);
                for (int j = 0; j < list.size(); j++) {
                    //if last element < jth element of the list
                    if (entityComparator.compare(last, list.get(j)) < 0) {
                        list.remove(list.size() - 1);
                        list.add(j, last);
                        break;
                    }
                }

            }
            setKeys(list);
            addNotify();
        }
        childcount++;
    }

    protected List<EntityWrapper> createKeysForEntity(DataContext c, EntityWrapper ew) {
        return createKeysForEntity(c, ew, null);
    }

    private List<EntityWrapper> createKeysForEntity(DataContext c, EntityWrapper ew, /* @Nullable*/ ProgressHandle ph) {
        List<EntityWrapper> list = new LinkedList<EntityWrapper>();

        if (ew instanceof PreloadedEntityWrapper) {
            EntityComparator entityComparator = new EntityComparator();
            for (EntityWrapper child : ((PreloadedEntityWrapper) ew).getChildren()) {
                list.add(child);
                absorbFilteredChildren(isVisible(child, filter), list, c, ph, entityComparator);
            }
            return list;
        }

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
        List<Epoch> epochs = Lists.newArrayList(entity.getEpochs());
        return sortEpochList(epochs);
    }

    private List<Epoch> sortedEpochs(EpochContainer entity) {
        List<Epoch> epochs = Lists.newArrayList(entity.getEpochs());
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
            List<EntityWrapper> l = Lists.newArrayList(Iterables.transform(entity.getAnalysisRecords(user),
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
            List<EntityWrapper> l = Lists.newArrayList(Iterables.transform(entity.getAnalysisRecords(user),
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
        EntityComparator entityComparator = new EntityComparator();
        for (DataElement d : entity.getOutputs().values()) {
            list.add(new EntityWrapper(d));
            displayUpdatedList(list, entityComparator);
        }
    }

    private void addExperiments(List<EntityWrapper> list, Project entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        List<Experiment> experiments = sortedExperiments(entity);

        int progressCounter = 0;
        if (ph != null) {
            ph.switchToDeterminate(experiments.size());
        }

        for (Experiment e : experiments) {
            list.add(new EntityWrapper(e));
            absorbFilteredChildren(filter.isExperimentsVisible(), list, entity.getDataContext(), ph, entityComparator);

            if (ph != null) {
                ph.progress(progressCounter++);
            }
        }
    }

    private void addEpochGroups(List<EntityWrapper> list, Experiment entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        for (EpochGroup eg : sortedEpochGroups(entity)) {
            list.add(new EntityWrapper(eg));
            absorbFilteredChildren(filter.isEpochGroupsVisible(), list, entity.getDataContext(), ph, entityComparator);
        }
    }

    private void addEpochGroups(List<EntityWrapper> list, EpochGroup entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        for (EpochGroup eg : entity.getEpochGroups()) {
            list.add(new EntityWrapper(eg));
            absorbFilteredChildren(filter.isEpochGroupsVisible(), list, entity.getDataContext(), ph, entityComparator);
        }
    }

    private void addEpochs(List<EntityWrapper> list, Experiment entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        for (Epoch e : sortedEpochs(entity)) {
            list.add(new EntityWrapper(e));
            absorbFilteredChildren(filter.isEpochsVisible(), list, entity.getDataContext(), ph, entityComparator);
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

                list.add(new EntityWrapper(e));
                absorbFilteredChildren(filter.isEpochsVisible(), list, c, ph, entityComparator);
            }
        } finally {
            c.commitTransaction();
            if (ph != null) {
                ph.progress(Bundle.Loading_Epochs_Done());
            }
        }
    }

    private void addMeasurements(List<EntityWrapper> list, Epoch entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        DataContext c = entity.getDataContext();
        c.beginTransaction();
        try {
            for (Measurement m : entity.getMeasurements()) {
                list.add(new EntityWrapper(m));
                displayUpdatedList(list, entityComparator);
            }
        } finally {
            c.commitTransaction();
        }
    }

    private void addChildrenSources(List<EntityWrapper> list, Source entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        for (Source e : entity.getChildrenSources()) {
            list.add(new EntityWrapper(e));
            displayUpdatedList(list, entityComparator);
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
                list.add(exp);
                absorbFilteredChildren(filter.isExperimentsVisible(), list, entity.getDataContext(), ph, entityComparator);
            }
        } else {
            for (Epoch e : sortedEpochs(entity)) {
                list.add(new EntityWrapper(e));
                absorbFilteredChildren(filter.isEpochsVisible(), list, entity.getDataContext(), ph, entityComparator);
            }

        }

    }

    private void addProcedureElements(List<EntityWrapper> list, Source entity, ProgressHandle ph) {
        EntityComparator entityComparator = new EntityComparator();
        //if Epoch's parents should be visible
        if (filter.isExperimentsVisible() || filter.isEpochGroupsVisible()) {
            List<? extends EntityWrapper> topLevelProcedureElements = getTopLevelProcedureElements(filter, entity.getEpochs());

            for (EntityWrapper exp : topLevelProcedureElements) {
                list.add(exp);
                absorbFilteredChildren(filter.isExperimentsVisible(), list, entity.getDataContext(), ph, entityComparator);
            }
        } else {
            for (Epoch e : sortedEpochs(entity)) {
                list.add(new EntityWrapper(e));
                absorbFilteredChildren(filter.isEpochsVisible(), list, entity.getDataContext(), ph, entityComparator);
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
