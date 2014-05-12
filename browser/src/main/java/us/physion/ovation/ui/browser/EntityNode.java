package us.physion.ovation.ui.browser;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.swing.Action;
import org.openide.actions.RenameAction;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import us.physion.ovation.domain.*;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.ui.actions.OpenInSeparateViewAction;
import us.physion.ovation.ui.actions.RevealElementAction;
import static us.physion.ovation.ui.browser.ActionUtils.appendToArray;
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

    public EntityNode(Children c, Lookup l, IEntityWrapper entity) {
        this(c, l, new URITreePathProviderImpl(), entity);
    }

    private EntityNode(Children c, Lookup l, URITreePathProviderImpl pathProvider, IEntityWrapper entity) {
        super(c, new ProxyLookup(l, Lookups.singleton(pathProvider)));

        pathProvider.setDelegate(this);
        this.entityWrapper = entity;

        loadURI();

        entityWrapper.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                EventQueueUtilities.runOnEDT(new Runnable() {

                    @Override
                    public void run() {
                        setDisplayName(entityWrapper.getDisplayName());
                        EntityNode.this.refresh();
                    }
                });
            }
        });
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

    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x",
                color.getRed(),
                color.getGreen(),
                color.getBlue());
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
            String html = "<font color=\"" + colorToHex(entityWrapper.getDisplayColor()) + "\">" + getDisplayName() + "</font>";
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
        Children c = getChildren();
        if (c == null || this.isLeaf()) {
            return;
        }
        if (c instanceof EntityChildren) {
            ((EntityChildren) c).refreshKeys();
        }
    }

    protected void setActionList(Action[] actions) {
        actionList = actions;
    }

    @Override
    public Action getPreferredAction() {
        if (!DataElement.class.isAssignableFrom(entityWrapper.getType())) {
            return super.getPreferredAction();
        }

        DataElement data = (DataElement) entityWrapper.getEntity();
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

                if (DataElement.class.isAssignableFrom(entityClass)) {
                    actionList = appendToArray(actionList, new RevealElementAction((DataElement) entityWrapper.getEntity()));
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

                //XXX: right now canRename will never change for a given node so it's safe to use it during initialization
                if (canRename()) {
                    actionList = appendToArray(actionList, SystemAction.get(RenameAction.class));
                }

                if (OvationEntity.class.isAssignableFrom(entityClass)) {
                    actionList = appendToArray(actionList, null, SystemAction.get(TrashEntityAction.class));
                }

            }
        }
        return actionList;
    }

    private static Map<String, Class> createMap() {
        Map<String, Class> insertables = new HashMap<String, Class>();
        insertables.put(Project.class.getSimpleName(), ProjectInsertable.class);
        insertables.put(Source.class.getSimpleName(), SourceInsertable.class);
        insertables.put(Experiment.class.getSimpleName(), ExperimentInsertable.class);
        insertables.put(EpochGroup.class.getSimpleName(), EpochGroupInsertable.class);
        insertables.put(Epoch.class.getSimpleName(), EpochInsertable.class);
        insertables.put(Measurement.class.getSimpleName(), ResponseInsertable.class);
        insertables.put(AnalysisRecord.class.getSimpleName(), AnalysisRecordInsertable.class);
        return insertables;
    }
}
