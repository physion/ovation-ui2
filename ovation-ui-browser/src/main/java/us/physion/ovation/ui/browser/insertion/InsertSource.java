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
import org.openide.util.lookup.ServiceProviders;
import ovation.*;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.interfaces.*;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

@ServiceProviders(value={
    @ServiceProvider(service=RootInsertable.class)
})
/**
 *
 * @author huecotanks
 */
public class InsertSource extends InsertEntity implements RootInsertable
{
    public InsertSource() {
        putValue(NAME, "Insert Source...");
    }

    @Override
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
    {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new InsertSourceWizardPanel1());
        return panels;
    }

    @Override
    public void wizardFinished(WizardDescriptor wiz, DataStoreCoordinator dsc, IEntityWrapper parent)
    {
        if (parent == null)
            dsc.getContext().insertSource(((String)wiz.getProperty("source.label")),
                    ((String)wiz.getProperty("source.identifier")));
        else
            ((Source)parent.getEntity()).insertSource((Map<String, Source>)wiz.getProperty("source.parents"),
                    (Epoch)wiz.getProperty("source.epoch"),
                    (String)wiz.getProperty("source.name"),
                    (String)wiz.getProperty("source.label"),
                    (String)wiz.getProperty("source.identifier"));
    }
}
