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
import org.openide.WizardDescriptor;
import ovation.*;
import us.physion.ovation.ui.browser.moveme.EpochInsertable;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
public class InsertStimulus extends InsertEntity implements EpochInsertable {

    public InsertStimulus(IEntityWrapper parent) {
        putValue(NAME, "Insert Stimulus...");
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
    public void wizardFinished(WizardDescriptor wiz, IAuthenticatedDataStoreCoordinator dsc, IEntityWrapper parent)
    {
        ((Epoch)parent.getEntity()).insertStimulus(((ExternalDevice)wiz.getProperty("stimulus.device")), 
                ((Map<String, Object>)wiz.getProperty("stimulus.deviceParameters")), 
                ((String)wiz.getProperty("stimulus.pluginID")), 
                ((Map<String, Object>)wiz.getProperty("stimulus.parameters")), 
                ((String)wiz.getProperty("stimulus.units")), 
                ((String[])wiz.getProperty("stimulus.dimensionLabels"))); 
    }
}
