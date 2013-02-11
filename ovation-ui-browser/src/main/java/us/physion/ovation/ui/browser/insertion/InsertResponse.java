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
import org.openide.util.lookup.ServiceProvider;
import ovation.*;
import us.physion.ovation.ui.interfaces.EpochInsertable;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

//@ServiceProvider(service=EpochInsertable.class)
/**
 *
 * @author huecotanks
 */
public class InsertResponse extends InsertEntity implements EpochInsertable {

    public InsertResponse() {
        putValue(NAME, "Insert Response...");
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
        ((Epoch)parent.getEntity()).insertResponse(((ExternalDevice)wiz.getProperty("response.device")), 
                ((Map<String, Object>)wiz.getProperty("response.deviceParameters")), 
                ((NumericData)wiz.getProperty("response.numericData")), 
                ((String)wiz.getProperty("response.units")), 
                ((String[])wiz.getProperty("response.dimensionLabels")), 
                ((double[])wiz.getProperty("response.samplingRates")),
                ((String[])wiz.getProperty("response.samplingRateUnits")),
                ((String)wiz.getProperty("response.dataUTI")));
    }
}
