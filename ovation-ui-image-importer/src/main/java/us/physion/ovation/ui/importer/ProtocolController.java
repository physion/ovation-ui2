/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.awt.Component;
import java.util.List;
import java.util.Map;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.ui.browser.insertion.ProtocolSelector;
import us.physion.ovation.ui.interfaces.BasicWizardPanel;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author jackie
 */
public class ProtocolController extends BasicWizardPanel {

    String epochName;
    int epochNum;
    ProtocolController(int num)
    {
        super();
        epochNum = num;
        if (num == -1)
            epochName = "epoch";
        else
            epochName = "epoch" + num;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
            component = new ProtocolSelector(changeSupport, ctx, true);
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        //read protocolID and protocolParameters
    }
    @Override
    public void storeSettings(WizardDescriptor data) {
        List<Map<String, Object>> epochs = (List<Map<String, Object>>)data.getProperty("epochs");
        Map<String, Object> epoch = epochs.remove(epochNum);
        
        Protocol protocol = ((ProtocolSelector)component).getProtocol();
        if (protocol != null)
            epoch.put("protocol", protocol);
        else{
            Map<String, String> newProtocols = ((ProtocolSelector)component).getNewProtocols();
            String name = ((ProtocolSelector)component).getProtocolName();
            epoch.put("newProtocols", newProtocols);
            epoch.put("protocolName", name);
        }
        epochs.add(epochNum, epoch);
        data.putProperty("epochs", epochs);
    }

    @Override
    public boolean isValid() {
      return true;
    }
    
}
