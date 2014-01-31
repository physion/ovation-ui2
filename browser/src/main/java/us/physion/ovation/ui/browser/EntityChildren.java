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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle.Messages;
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
        if (e == null)
            throw new OvationException("Pass in the list of Project/Source EntityWrappers, instead of null");
        
        parent = e;
        this.filter = filter;
        //if its per user, we create 
        if (e instanceof PerUserEntityWrapper)
        {
            loadedKeys = true;
            setKeys(((PerUserEntityWrapper)e).getChildren());
        }else{
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
    
    protected Callable<Children> getChildrenCallable(final EntityWrapper key)
    {
        return new Callable<Children>() {

            @Override
            public Children call() throws Exception {
                return new EntityChildren(key, filter);
            }
        };
    }

    @Override
    protected Node[] createNodes(final EntityWrapper key) 
    {
        return new Node[]{EntityWrapperUtilities.createNode(key, Children.createLazy(getChildrenCallable(key)))};
    }

   
    protected void updateWithKeys(final List<EntityWrapper> list)
    {
        EventQueueUtilities.runOnEDT(new Runnable(){

            @Override
            public void run() {
                setKeys(list);
                addNotify();
                refresh();
                loadedKeys = true;
            }
        });
    }
    
    public void resetNode()
    {
        loadedKeys = false;
        initKeys();
    }
    
    protected void initKeys()
    {
        final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Loading_Entity_Children(parent.getDisplayName()));

        EventQueueUtilities.runOffEDT(new Runnable(){
            

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

    private void absorbFilteredChildren(boolean isVisible, List<EntityWrapper> list, DataContext c, /* @Nullable*/ ProgressHandle ph) {
        if (!isVisible) {
            EntityWrapper pop = list.remove(list.size() - 1);
            List<URI> uris = new ArrayList<URI>();
            uris.addAll(pop.getFilteredParentURIs());
            URI u = null;
            try {
                u = pop.getURI() == null ? null : new URI(pop.getURI());
            } catch (URISyntaxException ex) {
            }
            uris.add(u);
            
            List<EntityWrapper> children = createKeysForEntity(c, pop, ph);
            for(EntityWrapper e : children){
                e.addFilteredParentURIs(uris);
            }
            list.addAll(children);
        }else{
            displayUpdatedList(list);
        }
    }
    
    private void displayUpdatedList(List<EntityWrapper> list)
    {
        if (childcount % UPDATE_FACTOR == 0) {
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
        Class entityClass = ew.getType();
        if (Project.class.isAssignableFrom(entityClass)) {
            Project entity = (Project) ew.getEntity();

            List<Experiment> experiments = sortedExperiments(entity);

            int progressCounter = 0;
            if (ph != null) {
                ph.switchToDeterminate(experiments.size());
            }
            
            for (Experiment e : experiments) {
                list.add(new EntityWrapper(e));
                absorbFilteredChildren(filter.isExperimentsVisible(), list, c, ph);
                
                if (ph != null) {
                    ph.progress(progressCounter++);
                }
            }

            if (ph != null) {
                ph.switchToIndeterminate();
            }
            List<User> users = Lists.newArrayList(c.getUsers());
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

            return list;
        }
        if (Source.class.isAssignableFrom(entityClass)) {
            Source entity = (Source) ew.getEntity();
            for (Source e : entity.getChildrenSources()) {
                list.add(new EntityWrapper(e));
                displayUpdatedList(list);
            }
            for (Epoch e : sortedEpochs(entity)) {
                list.add(new EntityWrapper(e));
                absorbFilteredChildren(filter.isEpochsVisible(), list, c, ph);
            }
            return list;
        }
        if (Experiment.class.isAssignableFrom(entityClass)) {
            Experiment entity = (Experiment) ew.getEntity();

            for (EpochGroup eg : sortedEpochGroups(entity)) {
                list.add(new EntityWrapper(eg));
                absorbFilteredChildren(filter.isEpochGroupsVisible(), list, c, ph);
            }
            for (Epoch e : sortedEpochs(entity)) {
                list.add(new EntityWrapper(e));
                absorbFilteredChildren(filter.isEpochsVisible(), list, c, ph);
            }
            return list;
        } else if (EpochGroup.class.isAssignableFrom(entityClass)) {
            EpochGroup entity = (EpochGroup) ew.getEntity();

            for (EpochGroup eg : entity.getEpochGroups()) {
                list.add(new EntityWrapper(eg));
                absorbFilteredChildren(filter.isEpochGroupsVisible(), list, c, ph);
            }

            if(ph!=null){
                ph.progress(Bundle.Loading_Epochs());
            }
            c.beginTransaction();//we wrap these in a transaction, because there may be a lot of epochs
            try {
                for (Epoch e : sortedEpochs(entity)) {
                    
                    list.add(new EntityWrapper(e));
                    absorbFilteredChildren(filter.isEpochsVisible(), list, c, ph);
                }
            } finally {
                c.commitTransaction();
                if (ph != null) {
                    ph.progress(Bundle.Loading_Epochs_Done());
                }
            }
            return list;
        } else if (Epoch.class.isAssignableFrom(entityClass)) {
            c.beginTransaction();//we wrap this in a transaction, because there may be a lot of epochs
            try {
                Epoch entity = (Epoch) ew.getEntity();
                for (Measurement m : entity.getMeasurements()) {
                    list.add(new EntityWrapper(m));
                    displayUpdatedList(list);
                }
                
                Collections.sort(list, new EntityComparator());

                for (User user : c.getUsers()) {
                    List<EntityWrapper> l = Lists.newArrayList(Iterables.transform(entity.getAnalysisRecords(user),
                            new Function<AnalysisRecord, EntityWrapper>() {

                                @Override
                                public EntityWrapper apply(AnalysisRecord f) {
                                    return new EntityWrapper(f);
                                }
                            }));
                    
                    Collections.sort(l, new EntityComparator());

                    if (l.size() > 0)
                        list.add(new PerUserEntityWrapper(user.getUsername(), user.getURI().toString(), l));    
                }
            } finally {
                c.commitTransaction();
            }
        } else if(AnalysisRecord.class.isAssignableFrom(entityClass))
        {
            AnalysisRecord entity = (AnalysisRecord) ew.getEntity();
            for(DataElement d : entity.getOutputs().values())
            {
                list.add(new EntityWrapper(d));
                displayUpdatedList(list);
            }
            
            Collections.sort(list, new EntityComparator());
        }
        return list;
    }

    private List<Experiment> sortedExperiments(Project entity) {
        List<Experiment> experiments = Lists.newArrayList(entity.getExperiments());
        Collections.sort(experiments, new Comparator<Experiment>()
        {
            @Override
            public int compare(Experiment o1, Experiment o2) {
                if (o1 == null || o2 == null ||
                        o1.getStart() == null || o2.getStart() == null) {
                    return 0;
                }

                return o1.getStart().compareTo(o2.getStart());
            }
        });
        return experiments;
    }

    private List<EpochGroup> sortedEpochGroups(Experiment entity) {
        List<EpochGroup> epochGroups = Lists.newArrayList(entity.getEpochGroups());
        Collections.sort(epochGroups, new Comparator<EpochGroup>()
        {
            @Override
            public int compare(EpochGroup o1, EpochGroup o2) {
                if (o1 == null || o2 == null ||
                        o1.getStart() == null || o2.getStart() == null) {
                    return 0;
                }

                return o1.getStart().compareTo(o2.getStart());
            }
        });
        return epochGroups;
    }

    private List<Epoch> sortedEpochs(Source entity) {
        List<Epoch> epochs = Lists.newArrayList(entity.getEpochs());
        Collections.sort(epochs, new Comparator<Epoch>()
        {
            @Override
            public int compare(Epoch o1, Epoch o2) {
                if (o1 == null || o2 == null ||
                        o1.getStart() == null || o2.getStart() == null) {
                    return 0;
                }

                return o1.getStart().compareTo(o2.getStart());
            }
        });
        return epochs;
    }

    private List<Epoch> sortedEpochs(EpochContainer entity) {
        List<Epoch> epochs = Lists.newArrayList(entity.getEpochs());
        Collections.sort(epochs, new Comparator<Epoch>()
        {
            @Override
            public int compare(Epoch o1, Epoch o2) {
                if (o1 == null || o2 == null ||
                        o1.getStart() == null || o2.getStart() == null) {
                    return 0;
                }

                return o1.getStart().compareTo(o2.getStart());
            }
        });
        return epochs;
    }
}
