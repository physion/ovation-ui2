/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import java.util.List;
import javax.swing.Action;
import org.openide.WizardDescriptor;
import us.physion.ovation.DataContext;

/**
 *
 * @author huecotanks
 */
public interface EntityInsertable extends Action, Comparable{
    public int getPosition();
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent);
    public void wizardFinished(WizardDescriptor wiz, DataContext ctx, IEntityWrapper ew);
}