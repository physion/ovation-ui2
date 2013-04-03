/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import java.util.List;
import javax.swing.Action;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
public interface EntityInsertable extends Action, Comparable{
    public int getPosition();
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent);
    public void wizardFinished(WizardDescriptor wiz, DataStoreCoordinator dsc, IEntityWrapper ew);
}