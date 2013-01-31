/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import org.openide.WizardDescriptor;
import us.physion.ovation.ui.interfaces.BasicWizardPanel;

import java.awt.*;

/**
 * @author huecotanks
 */
public class InsertEpochController extends BasicWizardPanel
{

    @Override
    public Component getComponent()
    {
        if (component == null) {
            component = new InsertEpochPanel();
        }
        return component;
    }


    @Override
    public void storeSettings(WizardDescriptor data)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isValid()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
