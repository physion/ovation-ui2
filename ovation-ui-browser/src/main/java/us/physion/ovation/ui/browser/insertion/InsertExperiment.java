/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Project;
import us.physion.ovation.ui.interfaces.ProjectInsertable;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

@ServiceProvider(service=ProjectInsertable.class)
/**
 *
 * @author jackie
 */
public class InsertExperiment extends InsertEntity implements ProjectInsertable{
    
    public InsertExperiment(){    
        putValue(NAME, "Insert Experiment...");
    }
    
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
    {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new InsertExperimentWizardPanel1());
        return panels;
    }

    @Override
    public void wizardFinished(WizardDescriptor wiz, DataStoreCoordinator dsc, IEntityWrapper parent)
    {
        ((Project)parent.getEntity()).insertExperiment(((String)wiz.getProperty("experiment.purpose")), 
                ((DateTime)wiz.getProperty("experiment.start"))); 
    }
}
