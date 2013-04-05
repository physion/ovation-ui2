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
import us.physion.ovation.domain.*;
import us.physion.ovation.ui.interfaces.EpochInsertable;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

//@ServiceProvider(service=EpochInsertable.class)
/**
 *
 * @author huecotanks
 */
public class InsertResponse extends InsertEntity implements EpochInsertable {

    public InsertResponse() {
        putValue(NAME, "Insert Measurement...");
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
        ((Epoch)parent.getEntity()).insertMeasurement(((String)wiz.getProperty("measurement.name")), 
                ((Set<String>)wiz.getProperty("measurement.sources")), 
                ((Set<String>)wiz.getProperty("measurement.devices")), 
                ((URL)wiz.getProperty("measurement.url")), 
                ((String)wiz.getProperty("measurement.mimeType"))); 
    }
}
