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

    @Override
    public void wizardFinished(WizardDescriptor wiz, DataStoreCoordinator dsc, IEntityWrapper parent)
    {
        if (parent.getType().isAssignableFrom(Experiment.class))
            ((Experiment)parent.getEntity()).insertEpochGroup(((String)wiz.getProperty("epochGroup.label")),
                    ((DateTime)wiz.getProperty("epochGroup.start")),
                    ((Protocol)wiz.getProperty("epochGroup.protocol")),
                    ((Map<String, Object>)wiz.getProperty("epochGroup.protocolParameters")),
                    ((Map<String, Object>)wiz.getProperty("epochGroup.deviceParameters")));
        else if (parent.getType().isAssignableFrom(EpochGroup.class))
            ((EpochGroup)parent.getEntity()).insertEpochGroup((String)wiz.getProperty("epochGroup.label"),
                    ((DateTime)wiz.getProperty("epochGroup.start")),
                    ((us.physion.ovation.domain.impl.Protocol)(wiz.getProperty("epochGroup.protocol"))),//TODO: cast to interface, not implementation
                    ((Map<String, Object>)wiz.getProperty("epochGroup.protocolParameters")),
                    ((Map<String, Object>)wiz.getProperty("epochGroup.deviceParameters")));
    }
}
