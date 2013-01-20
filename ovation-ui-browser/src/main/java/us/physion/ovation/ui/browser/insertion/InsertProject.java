/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.joda.time.DateTime;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import ovation.IAuthenticatedDataStoreCoordinator;
import us.physion.ovation.ui.interfaces.RootInsertable;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

@ServiceProvider(service=RootInsertable.class)
/**
 *
 * @author huecotanks
 */
public class InsertProject extends InsertEntity implements RootInsertable {

    public InsertProject() {
        putValue(NAME, "Insert Project...");
    }
    
    @Override
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
    {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new InsertProjectWizardPanel1());
        return panels;
    }
    @Override
    public void wizardFinished(WizardDescriptor wiz, IAuthenticatedDataStoreCoordinator dsc, IEntityWrapper parent)
    {
            dsc.getContext().insertProject((String)wiz.getProperty("project.name"), 
                    (String)wiz.getProperty("project.purpose"), 
                    (DateTime)wiz.getProperty("project.start"), 
                    (DateTime)wiz.getProperty("project.end"));
    }
}
