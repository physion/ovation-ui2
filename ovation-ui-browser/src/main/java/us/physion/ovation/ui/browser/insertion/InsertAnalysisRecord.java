/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import ovation.AnalysisRecord;
import ovation.Epoch;
import ovation.IAuthenticatedDataStoreCoordinator;
import ovation.Project;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.ProjectInsertable;

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
    public void wizardFinished(WizardDescriptor wiz, IAuthenticatedDataStoreCoordinator dsc, IEntityWrapper parent)
    {
        ((Project)parent.getEntity()).insertAnalysisRecord(((String)wiz.getProperty("analysisRecord.name")), 
                ((Iterator<Epoch>)wiz.getProperty("analysisRecord.epochs")), 
                ((Iterator<AnalysisRecord>)wiz.getProperty("analysisRecord.inputs")), 
                ((String)wiz.getProperty("analysisRecord.entryFn")), 
                ((Map<String,Object>)wiz.getProperty("analysisRecord.parameters")), 
                ((String)wiz.getProperty("analysisRecord.scmURL")), 
                ((String)wiz.getProperty("analysisRecord.scmRevision")));
    }
    
    @Override
    public int getPosition() {
        return 300;
    }
}