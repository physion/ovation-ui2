/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.util.ArrayList;
import java.util.List;
import org.openide.WizardDescriptor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import us.physion.ovation.DataContext;
import us.physion.ovation.ui.browser.BrowserUtilities;
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
    public void wizardFinished(WizardDescriptor wiz, DataContext c, IEntityWrapper parent)
    {
        c.insertSource(((String)wiz.getProperty("source.label")),
                    ((String)wiz.getProperty("source.identifier")));
        BrowserUtilities.resetView();
    }
}
