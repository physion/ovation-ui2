/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import ovation.*;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.*;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
public class EntityWrapper implements IEntityWrapper {
    
    private String uri;
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
        OvationEntity b = null;
        try{
            DataStoreCoordinator dsc = Lookup.getDefault().lookup(ConnectionProvider.class).getConnection();
            if (dsc == null)
            {
                return null;
            }
            DataContext c = dsc.getContext();
            b = c.getObjectWithURI(uri);
        
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
    @Override
    public String getDisplayName() {return displayName;}
    @Override
    public Class getType() { return type;}

    public static String inferDisplayName(OvationEntity e) {
	Class type = e.getClass();
        if (type.isAssignableFrom(Source.class))
        {
            return ((Source)e).getLabel();
        }
        else if (type.isAssignableFrom(Project.class))
        {
            return ((Project)e).getName();
        }else if (type.isAssignableFrom(Experiment.class))
        {
            return ((Experiment)e).getStart().toString("MM/dd/yyyy-hh:mm:ss");
        }
        else if (type.isAssignableFrom(EpochGroup.class))
        {
            return ((EpochGroup)e).getLabel();
        }
        else if (type.isAssignableFrom(Epoch.class))
        {
            if (((Epoch)e).getProtocol() != null)
                return ((Epoch)e).getProtocol().getName();
            else{
                ((Epoch)e).getStart().toString("MM/dd/yyyy-hh:mm:ss");
            }
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
}
