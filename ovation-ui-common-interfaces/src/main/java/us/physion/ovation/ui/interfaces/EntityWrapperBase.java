/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
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
        DataStoreCoordinator dsc = Lookup.getDefault().lookup(ConnectionProvider.class).getConnection();
        if (dsc == null)
        {
            return null;
        }
        DataContext c = dsc.getContext();
      
        return c.getObjectWithURI(uri);
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
}
