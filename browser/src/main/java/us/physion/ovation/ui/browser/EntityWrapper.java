package us.physion.ovation.ui.browser;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.*;
import us.physion.ovation.domain.events.EntityModifiedEvent;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.exceptions.EntityNotFoundException;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventBusProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
public class EntityWrapper implements IEntityWrapper {

    private URI uri;
    private final List<URI> filteredParentURIs = new ArrayList<URI>();
    private final Set<URI> watchUris = Sets.newHashSet();;

    private final Class type;
    private String displayName;
    private Color displayColor;

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public EntityWrapper(OvationEntity e) {
        uri = e.getURI();
        type = e.getClass();
        displayName = EntityWrapper.inferDisplayName(e);
        displayColor = inferDisplayColor(e);

        watchUris.add(uri);
        if (Measurement.class.isAssignableFrom(type)) {
            watchUris.add(((Measurement) e).getDataResource().getURI());
        }


        EventBusProvider evp = Lookup.getDefault().lookup(EventBusProvider.class);

        EventBus bus = evp.getDefaultEventBus();
        bus.register(this);

    }

    @Subscribe
    public void entityUpdated(EntityModifiedEvent updateEvent) {
        if (watchUris.contains(updateEvent.getEntityUri())) {
            OvationEntity e = getEntity();
            if(e != null) {
                displayName = EntityWrapper.inferDisplayName(getEntity());

                propertyChangeSupport.firePropertyChange(ENTITY_UPDATE, null, null);
                if (Measurement.class.isAssignableFrom(type)) {
                    watchUris.add(getEntity(Measurement.class).getDataResource().getURI());
                }
            }
        }
    }

    public static final String ENTITY_UPDATE = "entity_update";

    //used by the PerUserEntityWrapper object
    protected EntityWrapper(String name, Class clazz, String uri) {
        type = clazz;
        displayName = name;
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException ex) {
            //pass
        }
        
        
    }

    @Override
    public OvationEntity getEntity() {
        return getEntity(false);
    }

    @Override
    public OvationEntity getEntity(boolean includeTrash) {
        OvationEntity b = null;
        try {
            DataContext c = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
            if (c == null) {
                return null;
            }
            b = c.getObjectWithURI(getURI(), includeTrash);

        } catch (EntityNotFoundException e) {
            Toolkit.getDefaultToolkit().beep();
        } catch (RuntimeException e) {
            ErrorManager.getDefault().notify(e);
            throw e;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
        return b;
    }

    @Override
    public String getURI() {
        return uri.toString();
    }

    public void addFilteredParentURIs(List<URI> list) {
        for (URI u : list) {
            filteredParentURIs.add(u);
        }
    }

    public void addFilteredParentURI(URI uri) {
        filteredParentURIs.add(uri);
    }

    @Override
    public List<URI> getFilteredParentURIs() {
        return filteredParentURIs;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Class getType() {
        return type;
    }

    public static String inferDisplayName(OvationEntity e) {
        Class type = e.getClass();
        if (Source.class.isAssignableFrom(type)) {
            return ((Source) e).getLabel() + " (" + ((Source) e).getIdentifier() + ")";
        } else if (Project.class.isAssignableFrom(type)) {
            return ((Project) e).getName();
        } else if (Experiment.class.isAssignableFrom(type)) {
            return ((Experiment) e).getPurpose() + " (" + ((Experiment) e).getStart().toString("MM/dd/yyyy") + ")";
        } else if (EpochGroup.class.isAssignableFrom(type)) {
            return ((EpochGroup) e).getLabel();
        } else if (Epoch.class.isAssignableFrom(type)) {
            if (((Epoch) e).getProtocol() != null) {
                String epochTime;
                Epoch epoch = (Epoch) e;
                DateTime start = new DateTime(epoch.getStart());
                if (epoch.getStart().equals(start.toDateMidnight())) {
                    epochTime = epoch.getStart().toString("MM/dd/yyyy");
                } else {
                    epochTime = epoch.getStart().toString("MM/dd/yyyy hh:mm:ss");
                }

                return epoch.getProtocol().getName() + " (" + epochTime + ")";
            } else {
                return ((Epoch) e).getStart().toString("MM/dd/yyyy hh:mm:ss");
            }
        } else if (DataElement.class.isAssignableFrom(type)) {
            return ((DataElement) e).getName();
        } else if (AnalysisRecord.class.isAssignableFrom(type)) {
            return ((AnalysisRecord) e).getName();
        } else if (Protocol.class.isAssignableFrom(type)) {
            return ((Protocol) e).getName();
        }
        return "<no name>";
    }

    @Override
    public boolean canRename() {
        if (Source.class.isAssignableFrom(type)) {
            return true;
        } else if (Project.class.isAssignableFrom(type)) {
            return true;
        } else if (Experiment.class.isAssignableFrom(type)) {
            return true;
        } else if (EpochGroup.class.isAssignableFrom(type)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setName(String s) {
        OvationEntity e = getEntity();
        if (Source.class.isAssignableFrom(type)) {
            ((Source) e).setLabel(s);
        } else if (Project.class.isAssignableFrom(type)) {
            ((Project) e).setName(s);
        } else if (Experiment.class.isAssignableFrom(type)) {
            ((Experiment) e).setPurpose(s);
        } else if (EpochGroup.class.isAssignableFrom(type)) {
            ((EpochGroup) e).setLabel(s);
        }

        displayName = EntityWrapper.inferDisplayName(e);
    }

    @Override
    public String getName() {
        OvationEntity e = getEntity();
        if (e == null) {
            return "";
        }

        if (Source.class.isAssignableFrom(type)) {
            return ((Source) e).getLabel();
        } else if (Project.class.isAssignableFrom(type)) {
            return ((Project) e).getName();
        } else if (Experiment.class.isAssignableFrom(type)) {
            return ((Experiment) e).getPurpose();
        } else if (EpochGroup.class.isAssignableFrom(type)) {
            return ((EpochGroup) e).getLabel();
        } else {
            return null;
        }
    }

    @Override
    public <T extends OvationEntity> T getEntity(Class<T> clazz) {
        if (clazz.isAssignableFrom(getType())) {
            return (T) getEntity();
        }
        return null;
    }

    @Override
    public boolean isLeaf() {
        return Measurement.class.isAssignableFrom(getType())
                || Resource.class.isAssignableFrom(getType());

        //not sure if EquipmentSetup even has a Node...
        //EquipmentSetup.class.isAssignableFrom(getType()) ||
        //Protocol.class.isAssignableFrom(getType());
    }

    @Override
    public Color getDisplayColor() {
        return displayColor;
    }

    private static Color inferDisplayColor(OvationEntity e) {
        Class type = e.getClass();
        if (Source.class.isAssignableFrom(type)) {
            return new Color(161, 37, 127);
        } else if (Project.class.isAssignableFrom(type)) {
            return new Color(0, 89, 153);
        } else if (Experiment.class.isAssignableFrom(type)) {
            return Color.darkGray;
        } else if (EpochGroup.class.isAssignableFrom(type)) {
            return Color.black;
        } else if (Epoch.class.isAssignableFrom(type)) {
            return Color.black;
        } else if (DataElement.class.isAssignableFrom(type)) {
            return new Color(0, 126, 189);
        } else if (AnalysisRecord.class.isAssignableFrom(type)) {
            return new Color(51, 153, 0);
        } else if (Protocol.class.isAssignableFrom(type)) {
            return new Color(234, 147, 61);
        }

        return Color.BLACK;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + (this.uri != null ? this.uri.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityWrapper other = (EntityWrapper) obj;
        if (this.uri != other.uri && (this.uri == null || !this.uri.equals(other.uri))) {
            return false;
        }
        return true;
    }

}
