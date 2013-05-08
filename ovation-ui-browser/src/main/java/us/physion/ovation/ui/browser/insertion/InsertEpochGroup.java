/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import org.joda.time.DateTime;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import ovation.*;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.*;
import us.physion.ovation.ui.browser.EntityWrapper;
import us.physion.ovation.ui.interfaces.*;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

@ServiceProviders(value={
    @ServiceProvider(service=ExperimentInsertable.class),
    @ServiceProvider(service=EpochGroupInsertable.class)
})
/**
 *
 * @author huecotanks
 */
public class InsertEpochGroup extends InsertEntity implements EpochGroupInsertable, ExperimentInsertable {

    public InsertEpochGroup() {
        putValue(NAME, "Insert Epoch Group...");
    }

    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
    {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();

        panels.add(new InsertEpochGroupWizardPanel1()); //protocol selector
        panels.add(new InsertEpochGroupWizardPanel3()); //protocol param
        panels.add(new InsertEpochGroupWizardPanel4()); //device param
        panels.add(new InsertEpochGroupWizardPanel2());
        return panels;
    }
    
    private Protocol insertProtocolsAndFindSelected(DataContext context, Map<String, String> newProtocols, String selectedProtocolName)
    {
        Protocol protocol = null;
        for(String name : newProtocols.keySet())
        {
            String doc = newProtocols.get(name);
            Protocol p = context.insertProtocol(
                    name,
                    newProtocols.get(name));
            if (name.equals(selectedProtocolName)) 
            {
                protocol = p;
            }
        }
        return protocol;
    }
    
    @Override
    public void wizardFinished(WizardDescriptor wiz, DataStoreCoordinator dsc, IEntityWrapper parent)
    {
        OvationEntity parentEntity = parent.getEntity();
        
        Protocol protocol = insertProtocolsAndFindSelected(parentEntity.getDataContext(),
                (Map<String, String>)wiz.getProperty("epochGroup.newProtocols"), 
                (String)wiz.getProperty("epochGroup.protocolName"));
        
        if (protocol == null)
        {
            protocol = (Protocol)wiz.getProperty("epochGroup.protocol");
        }
        if (parentEntity instanceof Experiment)
        {        
            ((Experiment)parentEntity).insertEpochGroup(((String)wiz.getProperty("epochGroup.label")),
                    ((DateTime)wiz.getProperty("epochGroup.start")),
                    protocol,
                    ((Map<String, Object>)wiz.getProperty("epochGroup.protocolParameters")),
                    ((Map<String, Object>)wiz.getProperty("epochGroup.deviceParameters")));
        }
        else if (parentEntity instanceof EpochGroup)
        {
            ((EpochGroup)parentEntity).insertEpochGroup((String)wiz.getProperty("epochGroup.label"),
                    ((DateTime)wiz.getProperty("epochGroup.start")),
                    (us.physion.ovation.domain.impl.Protocol)protocol,//TODO: fix this in the api
                    ((Map<String, Object>)wiz.getProperty("epochGroup.protocolParameters")),
                    ((Map<String, Object>)wiz.getProperty("epochGroup.deviceParameters")));
        }
    }
}
