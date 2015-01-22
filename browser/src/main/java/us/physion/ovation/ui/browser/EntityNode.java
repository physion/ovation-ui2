package us.physion.ovation.ui.browser;

import com.google.common.collect.Maps;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
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
import static us.physion.ovation.ui.browser.ActionUtils.appendToArray;
import us.physion.ovation.ui.browser.dnd.FolderFlavor;
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
        return getEntityWrapper().getEntity(clazz);
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

    public EntityNode(Children c, IEntityWrapper parent) {
        super(c);
        this.entityWrapper = parent;
        loadURI();
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
        List<URI> paths = new ArrayList<URI>();

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

    Logger logger = LoggerFactory.getLogger(EntityNode.class);

    @Override
    public PasteType getDropType(final Transferable t, final int action, int index) {
        final Folder folder = getEntity(Folder.class);

        if (folder != null) {
            final Node dropNode = NodeTransfer.node(t,
                    DnDConstants.ACTION_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);

            if (null != dropNode && dropNode instanceof EntityNode) {

                final Resource r = (Resource) ((EntityNode) dropNode).getEntity(Resource.class);
                if (r != null) {
                    return new PasteType() {
                        @Override
                        public Transferable paste() throws IOException {
                            if ((action & DnDConstants.ACTION_MOVE) != 0) {
                                for (Folder f : r.getFolders()) {
                                    if (!f.equals(folder)) {
                                        f.removeResource(r);
                                    }
                                }
                            }

                            folder.addResource(r);

                            refresh();

                            return null;
                        }
                    };
                }
            }
        }

        return null;

//        Class entityClass = getEntity().getClass();
//        if (FolderContainer.class.isAssignableFrom(entityClass) && t.isDataFlavorSupported(FolderFlavor.FOLDER_FLAVOR)) {
//            return new PasteType() {
//
//                @Override
//                public Transferable paste() throws IOException {
//                    try {
//                        FolderContainer container = ((FolderContainer) getEntity());
//                        Folder folder = (Folder) t.getTransferData(FolderFlavor.FOLDER_FLAVOR);
//                        final Node node = NodeTransfer.node(t, NodeTransfer.DND_MOVE + NodeTransfer.CLIPBOARD_CUT);
//                        if (node != null) {
//                            for (FolderContainer c : folder.getParents()) {
//                                if (!c.equals(container)) {
//                                    c.removeFolder(folder);
//                                }
//                            }
//                        }
//                        container.addFolder(folder);
//
//
//                    } catch (UnsupportedFlavorException ex) {
//                        logger.error("Unable to paste Folder node", ex);
//                    }
//
//                    return null;
//
//                }
//
//            };
//        } else if (Folder.class.isAssignableFrom(entityClass) && t.isDataFlavorSupported(ResourceFlavor.RESOURCE_FLAVOR)) {
//            return new PasteType() {
//
//                @Override
//                public Transferable paste() throws IOException {
//                    try {
//                        Folder folder = getEntity(Folder.class);
//                        Resource r = (Resource) t.getTransferData(ResourceFlavor.RESOURCE_FLAVOR);
//                        final Node node = NodeTransfer.node(t, NodeTransfer.DND_MOVE + NodeTransfer.CLIPBOARD_CUT);
//                        if (node != null) {
//                            for (Folder f : r.getFolders()) {
//                                if (!f.equals(folder)) {
//                                    f.removeResource(r);
//                                }
//                            }
//                        }
//                        folder.addResource(r);
//                    } catch (UnsupportedFlavorException ex) {
//                        logger.error("Unable to paste Resource node", ex);
//                    }
//                    return null;
//                }
//            };
//        } else {
//            return null;
//        }
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
