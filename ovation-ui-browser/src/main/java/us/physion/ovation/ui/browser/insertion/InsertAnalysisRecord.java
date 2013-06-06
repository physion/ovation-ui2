/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import org.openide.WizardDescriptor;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.ProjectInsertable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@ServiceProvider(service=ProjectInsertable.class)
/**
 *
 * @author huecotanks
 */
public class InsertAnalysisRecord extends InsertEntity implements ProjectInsertable{

    public InsertAnalysisRecord() {
        putValue(NAME, "Insert Analysis Record...");
    }

    @Override
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
    {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new InsertAnalysisRecordWizardPanel1());
        panels.add(new InsertAnalysisRecordWizardPanel2());
        panels.add(new InsertAnalysisRecordWizardPanel3());
        return panels;
    }

    @Override
    public void wizardFinished(WizardDescriptor wiz, DataStoreCoordinator dsc, IEntityWrapper parent)
    {
        Protocol protocol = (Protocol)wiz.getProperty("analysisRecord.protocol");
        if (protocol == null)
            protocol = dsc.getContext().insertProtocol((String)wiz.getProperty("protocol.name"), (String)wiz.getProperty("protocol.name"));
        ((Project)parent.getEntity()).insertAnalysisRecord(((String)wiz.getProperty("analysisRecord.name")),
                ((Map<String, DataElement>)wiz.getProperty("analysisRecord.namedInputs")),
                protocol,
                ((Map<String, Object>)wiz.getProperty("analysisRecord.scmURL")));
    }

    @Override
    public int getPosition() {
        return 300;
    }
}
