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
        InsertEpochGroupWizardPanel1 panel1;
        /*if (parent.getType().equals(EpochGroup.class))
            panel1 = new InsertEpochGroupWizardPanel1(new EntityWrapper(((EpochGroup)parent.getEntity()).getSource()));
        else
        */
            panel1 = new InsertEpochGroupWizardPanel1(null);
        panels.add(panel1);
        panels.add(new InsertEpochGroupWizardPanel2());
        return panels;
    }

    @Override
    public void wizardFinished(WizardDescriptor wiz, IAuthenticatedDataStoreCoordinator dsc, IEntityWrapper parent)
    {
        IEntityWrapper s = (IEntityWrapper)wiz.getProperty("epochGroup.source");
        Source source = null;
        if (s != null)
            source = (Source)(s.getEntity());
            
        if (parent.getType().isAssignableFrom(Experiment.class))
            ((Experiment)parent.getEntity()).insertEpochGroup(source,
                    ((String)wiz.getProperty("epochGroup.label")),
                    ((DateTime)wiz.getProperty("epochGroup.start")),
                    ((DateTime)wiz.getProperty("epochGroup.end")));
        else if (parent.getType().isAssignableFrom(EpochGroup.class))
            ((EpochGroup)parent.getEntity()).insertEpochGroup(source,
                    ((String)wiz.getProperty("epochGroup.label")),
                    ((DateTime)wiz.getProperty("epochGroup.start")),
                    ((DateTime)wiz.getProperty("epochGroup.end")));
    }
}
