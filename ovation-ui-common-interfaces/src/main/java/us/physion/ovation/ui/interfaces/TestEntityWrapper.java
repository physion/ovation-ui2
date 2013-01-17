/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import ovation.*;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
public class TestEntityWrapper implements IEntityWrapper{

    IAuthenticatedDataStoreCoordinator dsc; 
    String uri;
    String displayName;
    Class type;
    public TestEntityWrapper(IAuthenticatedDataStoreCoordinator dsc, IEntityBase e)
    {
        this.dsc = dsc;
        dsc.getContext();
        uri = e.getURIString();
        type = e.getClass();
        displayName = inferDisplayName(e);
    }
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public IEntityBase getEntity() {
        return dsc.getContext().objectWithURI(uri);
    }

    @Override
    public Class getType() {
        return type;
    }

    @Override
    public String getURI() {
        return uri;
    }
    
    //TODO: call this on some static method in our iterfaces jar
    protected String inferDisplayName(IEntityBase e)
    {
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
            return ((Experiment)e).getStartTime().toString("MM/dd/yyyy-hh:mm:ss");
        }
        else if (type.isAssignableFrom(EpochGroup.class))
        {
            return ((EpochGroup)e).getLabel();
        }
        else if (type.isAssignableFrom(Epoch.class))
        {
            return ((Epoch)e).getProtocolID();
        }
        else if (type.isAssignableFrom(Response.class))
        {
            return ((Response)e).getExternalDevice().getName();
        }
        else if (type.isAssignableFrom(Stimulus.class))
        {
            return ((Stimulus)e).getExternalDevice().getName();
        }
        else if (type.isAssignableFrom(DerivedResponse.class))
        {
            return ((DerivedResponse)e).getName();
        }
        else if (type.isAssignableFrom(AnalysisRecord.class))
        {
            return ((AnalysisRecord)e).getName();
        }
        return "<no name>";
    }
    
}
