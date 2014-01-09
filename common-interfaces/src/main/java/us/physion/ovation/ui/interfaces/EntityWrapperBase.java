package us.physion.ovation.ui.interfaces;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.*;

/**
 *
 * @author huecotanks
 */
//XXX: EMI: Not used?
@Deprecated
public class EntityWrapperBase implements IEntityWrapper {

    private String uri;
    private Class type;
    private String displayName;

    public EntityWrapperBase(OvationEntity e)
    {
        uri = e.getURI().toString();
        type = e.getClass();
        displayName = inferDisplayName(e);
    }

    //used by the PerUserEntityWrapper object
    protected EntityWrapperBase(String name, Class clazz, String uri)
    {
        type = clazz;
        displayName = name;
        this.uri = uri;
    }

    @Override
    public OvationEntity getEntity(){
        DataContext c = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        if (c == null)
            return null;
        return c.getObjectWithURI(uri);
    }

    @Override
    public List<URI> getFilteredParentURIs() {
        return Collections.EMPTY_LIST;
    }
    @Override
    public String getURI()
    {
        return uri;
    }
    @Override
    public String getDisplayName() {return displayName;}
    @Override
    public Class getType() { return type;}


    public static String inferDisplayName(OvationEntity e) {
	Class type = e.getClass();
        if (type.isAssignableFrom(Source.class))
        {
            return ((Source) e).getLabel() + " (" + ((Source) e).getIdentifier() + ")";
        }
        else if (type.isAssignableFrom(Project.class))
        {
            return ((Project)e).getName();
        }else if (type.isAssignableFrom(Experiment.class))
        {
            return ((Experiment) e).getPurpose() + " (" + ((Experiment) e).getStart().toString("MM/dd/yyyy-hh:mm:ss") + ")";
        }
        else if (type.isAssignableFrom(EpochGroup.class))
        {
            return ((EpochGroup)e).getLabel();
        }
        else if (type.isAssignableFrom(Epoch.class))
        {
            return ((Epoch)e).getStart().toString("MM/dd/yyyy-hh:mm:ss");
        }
        else if (type.isAssignableFrom(Measurement.class))
        {
            return ((Measurement)e).getName();
        }

        else if (type.isAssignableFrom(AnalysisRecord.class))
        {
            return ((AnalysisRecord)e).getName();
        }
        return "<no name>";
    }

    @Override
    public <T extends OvationEntity> T getEntity(Class<T> clazz) {
        if (clazz.isAssignableFrom(getType()))
        {
            return (T)getEntity();
        }
        return null;
    }
}
