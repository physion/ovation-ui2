/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.ui.interfaces.EpochGroupInsertable;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

//@ServiceProvider(service=EpochGroupInsertable.class)
/**
 *
 * @author huecotanks
 */
public class InsertEpoch extends InsertEntity implements EpochGroupInsertable {

    public InsertEpoch() {
        putValue(NAME, "Insert Epoch...");
    }

    @Override
    public List<Panel<WizardDescriptor>> getPanels(IEntityWrapper parent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}