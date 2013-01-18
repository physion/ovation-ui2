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
import us.physion.ovation.ui.interfaces.*;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

@ServiceProviders(value={
    @ServiceProvider(service=SourceInsertable.class),
    @ServiceProvider(service=RootInsertable.class)
})
/**
 *
 * @author huecotanks
 */
public class InsertSource extends InsertEntity implements SourceInsertable, RootInsertable
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
    public void wizardFinished(WizardDescriptor wiz, IAuthenticatedDataStoreCoordinator dsc, IEntityWrapper parent)
    {
        if (parent == null)
            dsc.getContext().insertSource(((String)wiz.getProperty("source.label")));
        else
            ((Source)parent.getEntity()).insertSource(((String)wiz.getProperty("source.label")));
    }
}
