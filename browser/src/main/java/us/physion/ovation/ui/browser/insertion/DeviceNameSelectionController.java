/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.WizardDescriptor;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.EquipmentSetup;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.mixin.ProcedureElement;

/**
 *
 * @author huecotanks
 */
public class DeviceNameSelectionController extends BasicWizardPanel{
    EquipmentSetup equipmentSetup;
    
    public DeviceNameSelectionController(ProcedureElement e)
    {
        super();
        if (e == null)
            return;
        if (e instanceof Epoch)
        {
            equipmentSetup = ((Epoch)e).getExperiment().getEquipmentSetup();
        }else if (e instanceof EpochGroup)
        {
            equipmentSetup = ((EpochGroup)e).getExperiment().getEquipmentSetup();
        }else if (e instanceof Experiment)
        {
            equipmentSetup = ((Experiment)e).getEquipmentSetup();
        }
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new ListSelectionPanel(changeSupport, 
                    "Select devices for this measurement", 
                    "Select Devices");
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        List<String> l;
        if (equipmentSetup == null)
        {
            l = new ArrayList();
        }else{
            Map<String, Object> devices = equipmentSetup.getDeviceDetails();
            if (devices == null)
                devices = new HashMap();
            l = new ArrayList(devices.keySet());
            Collections.sort(l);
        }
        ListSelectionPanel c = (ListSelectionPanel)getComponent();
        Collection<String> selectedNames = (Collection<String>)data.getProperty("deviceNames");
        
        c.setNames(l, selectedNames);
    }
    @Override
    public void storeSettings(WizardDescriptor data) {
       
        ListSelectionPanel c = (ListSelectionPanel)getComponent();
        data.putProperty("deviceNames", c.getNames());
    }

    @Override
    public boolean isValid() {
      return true;
    }
    
}
