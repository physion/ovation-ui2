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
    String objectPrefix;

    public InsertEpochGroup() {
        putValue(NAME, "Insert Epoch Group...");
        objectPrefix = "epochGroup";
    }
    
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
    {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();

        panels.add(new SelectProtocolController(objectPrefix)); //protocol selector
        panels.add(new KeyValueController("Add Protocol Parameters", "Add optional protocol parameters", "epochGroup.protocolParameters")); //device param
        panels.add(new KeyValueController("Add Device Parameters", "Add optional device parameters", "epochGroup.deviceParameters")); //device param
        panels.add(new InsertEpochGroupWizardPanel(objectPrefix));
        return panels;
    }
    
    @Override
    public void wizardFinished(WizardDescriptor wiz, DataContext c, IEntityWrapper parent)
    {
        OvationEntity parentEntity = parent.getEntity();
        
        Protocol protocol = getProtocolFromProtocolSelector(parentEntity.getDataContext(),
                (Map<String, String>)wiz.getProperty(combine(objectPrefix, "newProtocols")), 
                (String)wiz.getProperty(combine(objectPrefix, "protocolName")),
                (Protocol)wiz.getProperty(combine(objectPrefix, "protocol")));
     
        if (parentEntity instanceof Experiment)
        {        
            ((Experiment)parentEntity).insertEpochGroup(((String)wiz.getProperty(combine(objectPrefix, "label"))),
                    ((DateTime)wiz.getProperty(combine(objectPrefix, "start"))),
                    protocol,
                    ((Map<String, Object>)wiz.getProperty(combine(objectPrefix, "protocolParameters"))),
                    ((Map<String, Object>)wiz.getProperty(combine(objectPrefix, "deviceParameters"))));
        }
        else if (parentEntity instanceof EpochGroup)
        {
            ((EpochGroup)parentEntity).insertEpochGroup((String)wiz.getProperty(combine(objectPrefix, "label")),
                    ((DateTime)wiz.getProperty(combine(objectPrefix, "start"))),
                    protocol,
                    ((Map<String, Object>)wiz.getProperty(combine(objectPrefix, "protocolParameters"))),
                    ((Map<String, Object>)wiz.getProperty(combine(objectPrefix, "deviceParameters"))));
        }
    }
}
