/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openide.WizardDescriptor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.ui.interfaces.EpochInsertable;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.ProjectInsertable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*@ServiceProviders(value={
    @ServiceProvider(service=ProjectInsertable.class),
    @ServiceProvider(service=EpochInsertable.class)
})*/
/**
 *
 * @author huecotanks
 */
public class InsertAnalysisRecord extends InsertEntity implements ProjectInsertable, EpochInsertable{

    String objectPrefix;
    public InsertAnalysisRecord() {
        putValue(NAME, "Insert Analysis Record...");
        objectPrefix = "analysisRecord";
    }

    @Override
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
    {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new NameWizard(objectPrefix));
        panels.add(new SelectProtocolController(objectPrefix));
        panels.add(new KeyValueController("Add Protocol Parameters", "Add option protocol parameters, if any", combine(objectPrefix, "protocolParameters")));
        //panels.add(new AnalysisRecordInputsWizard(objectPrefix));
        //panels.add(new AnalysisRecordOutputsWizard(objectPrefix));
        return panels;
    }

    @Override
    public void wizardFinished(WizardDescriptor wiz, DataContext c, IEntityWrapper parent)
    {
        OvationEntity parentEntity = parent.getEntity();
        Protocol protocol = getProtocolFromProtocolSelector(parentEntity.getDataContext(),
                    (Map<String, String>) wiz.getProperty("epoch.newProtocols"),
                    (String) wiz.getProperty("epoch.protocolName"),
                    (Protocol) wiz.getProperty("epoch.protocol"));
        
        if (Project.class.isAssignableFrom(parent.getType())) {
            ((Project)parentEntity).insertAnalysisRecord(((String) wiz.getProperty(objectPrefix + ".name")),
                    ((Map<String, DataElement>) wiz.getProperty(objectPrefix + ".namedInputs")),
                    protocol,
                    ((Map<String, Object>) wiz.getProperty(objectPrefix + ".parameters")));
        } else if (Epoch.class.isAssignableFrom(parent.getType())) {
            ((Epoch)parentEntity).insertAnalysisRecord(((String) wiz.getProperty(objectPrefix + ".name")),
                    ((Map<String, DataElement>) wiz.getProperty(objectPrefix + ".namedInputs")),
                    protocol,
                    ((Map<String, Object>) wiz.getProperty(objectPrefix + ".parameters")));
        }

    }

    @Override
    public int getPosition() {
        return 300;
    }
}
