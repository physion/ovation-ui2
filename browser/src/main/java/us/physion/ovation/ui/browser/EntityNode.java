package us.physion.ovation.ui.browser;

import com.google.common.collect.Maps;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.swing.Action;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.*;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.ui.actions.AddFolderAction;
import us.physion.ovation.ui.actions.OpenInSeparateViewAction;
import us.physion.ovation.ui.actions.RevealElementAction;
import us.physion.ovation.ui.actions.UpdateResourceAction;
import static us.physion.ovation.ui.browser.ActionUtils.appendToArray;
import us.physion.ovation.ui.browser.dnd.ResourceFlavor;
import us.physion.ovation.ui.interfaces.*;

/**
 *
 * @author huecotanks
 */
public class EntityNode extends AbstractNode implements RefreshableNode, URINode, IEntityNode {

    private Action[] actionList;
    private final IEntityWrapper entityWrapper;
    private static Map<String, Class> insertableMap = createMap();
    private URI uri;
    private EntityChildrenChildFactory childFactory;

    Logger logger = LoggerFactory.getLogger(EntityNode.class);

    public EntityNode(Children c, Lookup l, IEntityWrapper entity) {
        this(c, l, new URITreePathProviderImpl(), entity);
    }

    private EntityNode(Children c, Lookup l, URITreePathProviderImpl pathProvider, IEntityWrapper entity) {
        super(c, new ProxyLookup(l, Lookups.singleton(pathProvider)));

        pathProvider.setDelegate(this);
        this.entityWrapper = entity;

        loadURI();

        setDisplayName(entityWrapper.getDisplayName());

        entityWrapper.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                EventQueueUtilities.runOnEDT(new Runnable() {

                    @Override
                    public void run() {
                        setDisplayName(entityWrapper.getDisplayName());
                        fireDisplayNameChange(null, getDisplayName());
                        EntityNode.this.refresh();
                    }
                });
            }
        });
    }

    public EntityNode(EntityChildrenChildFactory cf, IEntityWrapper key) {
        this(cf, new InstanceContent(), key);
    }

    private EntityNode(final EntityChildrenChildFactory cf, InstanceContent ic, IEntityWrapper key) {
        this(key.isLeaf() ? Children.LEAF : Children.create(cf, true), new AbstractLookup(ic), key);

        ic.add(key);

        if (!key.isLeaf()) {
            ic.add(new LazyChildren() {

                @Override
                public boolean isLoaded() {
                    return cf.isLoaded();
                }
            });
        }

        this.childFactory = cf;
    }

    @Override
    public OvationEntity getEntity() {
        return getEntityWrapper().getEntity();
    }

    @Override
    public OvationEntity getEntity(boolean includingTrash) {
        return getEntityWrapper().getEntity(includingTrash);
    }

    @Override
    public <T extends OvationEntity> T getEntity(Class<T> clazz) {
        return getEntityWrapper() == null ? null : getEntityWrapper().getEntity(clazz);
    }

    @Override
    public IEntityWrapper getEntityWrapper() {
        return entityWrapper;
    }

    private static class URITreePathProviderImpl implements URITreePathProvider {

        private EntityNode delegate;

        public void setDelegate(EntityNode delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<URI> getTreePath() {
            if (delegate != null) {
                return delegate.buildURITreePath();
            } else {
                return Collections.EMPTY_LIST;
            }
        }
    }

    @Override
    public boolean canRename() {
        return entityWrapper != null ? entityWrapper.canRename() : super.canRename();
    }

    @Override
    public void setName(String s) {
        super.setName(s);
        if (entityWrapper != null) {
            entityWrapper.setName(s);

            setDisplayName(entityWrapper.getDisplayName());
            fireDisplayNameChange(null, getDisplayName());
        }
    }

    @Override
    public String getName() {
        return entityWrapper != null ? entityWrapper.getName() : super.getName();
    }

    @Override
    public String getHtmlDisplayName() {
        if (entityWrapper != null && entityWrapper.getDisplayColor() != null) {
            String html = "<font color=\"" + EntityColors.colorToHex(entityWrapper.getDisplayColor()) + "\">" + getDisplayName() + "</font>";
            return html;
        }

        return getDisplayName();
    }

    public EntityNode(Children c, Lookup lookup) {
        super(c, lookup);
        this.entityWrapper = null;
    }

    public EntityNode(Children c) {
        this(c, (Lookup) null);
    }

    private void loadURI() {
        try {
            this.uri = (entityWrapper != null && entityWrapper.getURI() != null) ? new URI(entityWrapper.getURI()) : null;
        } catch (URISyntaxException ex) {
            //XXX: Log?
            this.uri = null;
        }
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public List<URI> getFilteredParentURIs() {
        return entityWrapper == null ? Collections.EMPTY_LIST : entityWrapper.getFilteredParentURIs();
    }

    private List<URI> buildURITreePath() {
        List<URI> paths = new ArrayList<>();

        Node n = this;
        while (n != null) {
            if (n instanceof URINode) {
                //put in reverse
                paths.add(((URINode) n).getURI());

                paths.addAll(((URINode) n).getFilteredParentURIs());
            }
            n = n.getParentNode();
        }

        Collections.reverse(paths);

        return paths;
    }

    @Override
    public void refresh() {
        if (childFactory != null) {
            childFactory.refresh();
        }
    }

    private boolean isRefreshable() {
        return childFactory != null;
    }

    protected void setActionList(Action[] actions) {
        actionList = actions;
    }

    @Override
    public Action getPreferredAction() {
        if (!Resource.class.isAssignableFrom(entityWrapper.getType())) {
            return super.getPreferredAction();
        }

        Resource data = (Resource) entityWrapper.getEntity();
        return new OpenInSeparateViewAction(data, buildURITreePath());
    }

    @Override
    public Action[] getActions(boolean popup) {
        if (actionList == null) {
            if (entityWrapper == null)// root node
            {
                Collection<? extends RootInsertable> insertables = Lookup.getDefault().lookupAll(RootInsertable.class);
                List<RootInsertable> l = new ArrayList(insertables);
                Collections.sort(l);
                actionList = l.toArray(new RootInsertable[l.size()]);
            } else {
                Class entityClass = entityWrapper.getType();
                Class insertableClass = insertableMap.get(entityClass.getSimpleName());
                if (insertableClass == null) {
                    actionList = new Action[0];
                } else {
                    Collection insertables = Lookup.getDefault().lookupAll(insertableClass);
                    List<? extends Comparable> l = new ArrayList(insertables);
                    Collections.sort(l);
                    actionList = l.toArray(new EntityInsertable[l.size()]);
                }

                if (Resource.class.isAssignableFrom(entityClass)) {
                    actionList = appendToArray(actionList, new RevealElementAction((Resource) entityWrapper.getEntity()));
                    actionList = appendToArray(actionList, new UpdateResourceAction((Resource)entityWrapper.getEntity()));
                    //actionList = appendToArray(actionList, CutAction.get(CutAction.class));
                }

                if (AnalysisRecord.class.isAssignableFrom(entityClass)) {
                    actionList = appendToArray(actionList, SystemAction.get(AnalysisRecordInputsAction.class));
                }

                if (Epoch.class.isAssignableFrom(entityClass)) {
                    actionList = appendToArray(actionList, SystemAction.get(EpochInputSourcesAction.class));
                }

                if (Measurement.class.isAssignableFrom(entityClass)) {
                    actionList = appendToArray(actionList, SystemAction.get(MeasurementInputSourcesAction.class));
                }

                if (FolderContainer.class.isAssignableFrom(entityClass)) {
                    actionList = appendToArray(actionList, new AddFolderAction((FolderContainer) entityWrapper.getEntity()));
                }

                //XXX: right now canRename will never change for a given node so it's safe to use it during initialization
//                if (canRename()) {
//                    actionList = appendToArray(actionList, SystemAction.get(RenameAction.class));
//                }
                if (isRefreshable()) {
                    actionList = appendToArray(actionList, null, new ResettableAction(this));
                }

                if (OvationEntity.class.isAssignableFrom(entityClass)
                        //User entities cannot be sent to the trash
                        && !User.class.isAssignableFrom(entityClass)) {
                    actionList = appendToArray(actionList, null, SystemAction.get(TrashEntityAction.class));
                }

            }
        }
        return actionList;
    }

    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        super.createPasteTypes(t, s);
        PasteType p = getDropType(t, 0, 0);
        if (p != null) {
            s.add(p);
        }
    }

    @Override
    public boolean canCut() {
        return true;
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Transferable clipboardCut() throws IOException {
        Transferable deflt = super.clipboardCut();
        ExTransferable added = ExTransferable.create(deflt);
        added.put(new ExTransferable.Single(ResourceFlavor.RESOURCE_FLAVOR) {
            @Override
            protected Resource getData() {
                return getLookup().lookup(Resource.class);
            }
        });
        return added;
    }
    
    private Project rootProject(Node n) {
        OvationEntity e = ((EntityNode) n).getEntity();
        while (!(e instanceof Project)) {
            n = n.getParentNode();
            if (n == null || !(n instanceof EntityNode)) {
                return null;
            }

            e = ((EntityNode) n).getEntity();
        }
        return (Project) e;
    }

    @Override
    public PasteType getDropType(final Transferable t, final int action, int index) {
        final Folder folder = getEntity(Folder.class);
        if (folder != null) {
            final Node dropNode = NodeTransfer.node(t, NodeTransfer.COPY | NodeTransfer.MOVE);

            if (null != dropNode && dropNode instanceof EntityNode) {
                
                final Project dropRoot = rootProject(dropNode);
                final Project targetRoot = rootProject(this);
                
                if(!dropRoot.equals(targetRoot)) {
                    return null;
                }

                final Resource r = (Resource) ((EntityNode) dropNode).getEntity(Resource.class);
                if (r != null) {
                    return new PasteType() {
                        @Override
                        public Transferable paste() throws IOException {
                            
                            if ((action & NodeTransfer.MOVE) != 0) {
                                Node parentNode = dropNode.getParentNode();
                                if (parentNode instanceof EntityNode) {
                                    Folder f = ((EntityNode) parentNode).getEntity(Folder.class);
                                    f.removeResource(r);
                                }
                            }

                            
                            folder.addResource(r);


                            return null;
                        }
                    };
                }
                
                final Folder f = (Folder) ((EntityNode)dropNode).getEntity(Folder.class);
                if(f != null) {
                    return new PasteType() {
                        @Override
                        public Transferable paste() throws IOException {
                            if ((action & NodeTransfer.MOVE) != 0) {
                                Node parentNode = dropNode.getParentNode();
                                if (parentNode instanceof EntityNode) {
                                    FolderContainer container = (FolderContainer) ((EntityNode) parentNode).getEntity();
                                    if(container != null) {
                                        container.removeFolder(f);
                                    }
                                }
                            }

                            folder.addFolder(f);

                            return null;
                        } 
                    };
                }
            }
        }
        

        return null;
    }

    private static Map<String, Class> createMap() {
        Map<String, Class> insertables = Maps.newHashMap();
        insertables
                .put(Project.class
                        .getSimpleName(), ProjectInsertable.class
                );
        insertables.put(Source.class
                .getSimpleName(), SourceInsertable.class
        );
        insertables.put(Experiment.class
                .getSimpleName(), ExperimentInsertable.class
        );
        insertables.put(EpochGroup.class
                .getSimpleName(), EpochGroupInsertable.class
        );
        insertables.put(Epoch.class
                .getSimpleName(), EpochInsertable.class
        );
        insertables.put(Measurement.class
                .getSimpleName(), ResponseInsertable.class
        );
        insertables.put(AnalysisRecord.class
                .getSimpleName(), AnalysisRecordInsertable.class
        );
        return insertables;
    }
}
