package us.physion.ovation.ui.browser;

import org.joda.time.DateTime;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.*;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author huecotanks
 */
public class EntityWrapper implements IEntityWrapper {

    private String uri;
    private List<URI> filteredParentURIs = new ArrayList<URI>();

    private Class type;
    private String displayName;

    public EntityWrapper(OvationEntity e)
    {
        uri = e.getURI().toString();
        type = e.getClass();
        displayName = EntityWrapper.inferDisplayName(e);
    }

    //used by the PerUserEntityWrapper object
    protected EntityWrapper(String name, Class clazz, String uri)
    {
        type = clazz;
        displayName = name;
        this.uri = uri;
    }

    @Override
    public OvationEntity getEntity(){
        return getEntity(false);
    }

    @Override
    public OvationEntity getEntity(boolean includeTrash){
        OvationEntity b = null;
        try{
            DataContext c = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
            if (c == null)
            {
                return null;
            }
            b = c.getObjectWithURI(uri, includeTrash);

        } catch (RuntimeException e)
        {
            ErrorManager.getDefault().notify(e);
            throw e;
        }
        catch(Exception e)
        {
            ErrorManager.getDefault().notify(e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
        return b;
    }
    @Override
    public String getURI()
    {
        return uri;
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
    public String getDisplayName() {return displayName;}
    @Override
    public Class getType() { return type;}

    public static String inferDisplayName(OvationEntity e) {
	Class type = e.getClass();
        if (Source.class.isAssignableFrom(type))
        {
            return ((Source) e).getLabel() + " (" + ((Source) e).getIdentifier() + ")";
        }
        else if (Project.class.isAssignableFrom(type))
        {
            return ((Project)e).getName();
        }else if (Experiment.class.isAssignableFrom(type))
        {
            return ((Experiment) e).getPurpose() + " (" + ((Experiment) e).getStart().toString("MM/dd/yyyy") + ")";
        }
        else if (EpochGroup.class.isAssignableFrom(type))
        {
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
        } else if (DataElement.class.isAssignableFrom(type))
        {
            return ((DataElement)e).getName();
        } else if (AnalysisRecord.class.isAssignableFrom(type))
        {
            return ((AnalysisRecord)e).getName();
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
        if (clazz.isAssignableFrom(getType()))
        {
            return (T)getEntity();
        }
        return null;
    }

    @Override
    public boolean isLeaf() {
        return Measurement.class.isAssignableFrom(getType()) ||
                Resource.class.isAssignableFrom(getType()) ||
                //not sure if EquipmentSetup even has a Node...
                EquipmentSetup.class.isAssignableFrom(getType()) ||
                Protocol.class.isAssignableFrom(getType());
    }
}
