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
import us.physion.ovation.domain.EquipmentSetup;
import us.physion.ovation.domain.Source;

/**
 *
 * @author huecotanks
 */
public class DeviceNameSelectionController extends BasicWizardPanel{
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
        Epoch e = (Epoch)data.getProperty("epoch");
        
        EquipmentSetup es = e.getExperiment().getEquipmentSetup();
        List<String> l;
        if (es == null)
        {
            l = new ArrayList();
        }else{
            Map<String, Object> devices = es.getDeviceDetails();
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
