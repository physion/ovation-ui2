/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.*;
import javax.swing.AbstractAction;
import org.openide.WizardDescriptor;
import org.openide.util.lookup.ServiceProvider;
import ovation.*;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.ui.interfaces.EpochInsertable;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

//@ServiceProvider(service=EpochInsertable.class)
/**
 *
 * @author huecotanks
 */
public class InsertDerivedResponse extends InsertEntity implements EpochInsertable{

    public InsertDerivedResponse() {
        putValue(NAME, "Insert AnalysisRecord...");
    }

    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels()
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
        ((Epoch)parent.getEntity()).insertMeasurement(((String)wiz.getProperty("analysisRecord.name")), 
                ((Set<String>)wiz.getProperty("analysisRecord.sources")), 
                ((Set<String>)wiz.getProperty("analysisRecord.devices")), 
                ((URL)wiz.getProperty("analysisRecord.url")), 
                ((String)wiz.getProperty("analysisRecord.mimeType"))); 
    }
}
