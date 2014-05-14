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
import java.util.Objects;
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
import us.physion.ovation.ui.interfaces.EntityColors;
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

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public EntityWrapper(OvationEntity e) {
        uri = e.getURI();
        type = e.getClass();

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
                propertyChangeSupport.firePropertyChange(PROP_ENTITY_UPDATE, null, null);
                if (Measurement.class.isAssignableFrom(type)) {
                    watchUris.add(getEntity(Measurement.class).getDataResource().getURI());
                }
            }
        }
    }


    //used by the PerUserEntityWrapper object
    protected EntityWrapper(String name, Class clazz, String uri) {
        type = clazz;
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
        return inferDisplayName(getEntity());
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
            String name = ((Protocol) e).getName();
            return name == null ? e.getURI().toString() : name;
        } else if(User.class.isAssignableFrom(type)) {
            User u = (User)e;
            return u.getUsername() == null ? u.getEmail() : u.getUsername();
        }

        return "";
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
        String currentName = getName();

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

        propertyChangeSupport.firePropertyChange(PROP_NAME, currentName, s);
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
        return inferDisplayColor(getEntity());
    }

    private static Color inferDisplayColor(OvationEntity e) {

        return EntityColors.getEntityColor(e.getClass());

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
        return Objects.hash(this.uri);
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
