package us.physion.ovation.ui.browser;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.net.URISyntaxException;
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
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EntityColors;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
public class EntityWrapper implements IEntityWrapper {
    public final static EntityWrapper EMPTY = new EntityWrapper();

    private final URI uri;
    private OvationEntity entity = null;
    private final List<URI> filteredParentURIs = Lists.newArrayList();
    private final Set<URI> watchUris = Sets.newHashSet();

    private final Class type;

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final DataContext context;
    
    /**
     * Not an actual constructor... this only makes sense for the EMPTY constant which is useful when EntityWrappers are keys in hashmaps etc. that don't accept NULL keys.
     */
    private EntityWrapper() {
        uri = null;
        type = null;
        context = null;
    }

    public EntityWrapper(OvationEntity e) {
        uri = e.getURI();
        type = e.getClass();
        entity = e;

        watchUris.add(uri);
        if (e instanceof Measurement) {
            watchUris.add(((Measurement) e).getDataResource().getURI());
        }

        context = e.getDataContext();
        context.getEventBus().register(this);

    }
    
    //used by the PerUserEntityWrapper object
    protected EntityWrapper(String name, Class clazz, String uri) {
        type = clazz;
        this.context = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        entity = null;
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException ex) {
            throw new OvationException(ex);
        }
    }

    @Subscribe
    public void entityUpdated(EntityModifiedEvent updateEvent) {
        if (watchUris.contains(updateEvent.getEntityUri())) {
            OvationEntity e = getEntity();
            if (e != null) {
                propertyChangeSupport.firePropertyChange(PROP_ENTITY_UPDATE, null, null);
                if (Measurement.class.isAssignableFrom(type)) {
                    watchUris.add(getEntity(Measurement.class).getDataResource().getURI());
                }
            }
        }
    }



    @Override
    public OvationEntity getEntity() {
        return getEntity(false);
    }

    @Override
    public synchronized OvationEntity getEntity(boolean includeTrash) {
        
        try {
            if(entity == null) {
                entity = context.getObjectWithURI(getURI(), includeTrash);
            }
            
            return entity;
        } catch (EntityNotFoundException e) {
            Toolkit.getDefaultToolkit().beep();
        } catch (RuntimeException e) {
            ErrorManager.getDefault().notify(e);
            throw e;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
        
        return entity;
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
        if (e instanceof Source) {
            return ((Source) e).getLabel() + " (" + ((Source) e).getIdentifier() + ")";
        } else if (e instanceof Project) {
            return ((Project) e).getName();
        } else if (e instanceof Experiment) {
            return ((Experiment) e).getPurpose() + " (" + ((Experiment) e).getStart().toString("MM/dd/yyyy") + ")";
        } else if (e instanceof EpochGroup) {
            return ((EpochGroup) e).getLabel();
        } else if (e instanceof Epoch) {
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
        } else if (e instanceof DataElement) {
            return ((DataElement) e).getName();
        } else if (e instanceof AnalysisRecord) {
            return ((AnalysisRecord) e).getName();
        } else if (e instanceof Protocol) {
            String name = ((Protocol) e).getName();
            return name == null ? e.getURI().toString() : name;
        } else if (e instanceof User) {
            User u = (User) e;
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
        if(obj == EMPTY && this == obj){
            return true;
        }
        
        final EntityWrapper other = (EntityWrapper) obj;
        if (this.uri != other.uri && (this.uri == null || !this.uri.equals(other.uri))) {
            return false;
        }
        return true;
    }

}
