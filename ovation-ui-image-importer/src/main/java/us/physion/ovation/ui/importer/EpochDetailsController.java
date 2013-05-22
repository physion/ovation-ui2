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
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.ui.browser.insertion.ProtocolSelector;
import us.physion.ovation.ui.interfaces.BasicWizardPanel;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author jackie
 */
public class EpochDetailsController extends BasicWizardPanel {

    String epochName;
    int epochNum;
    EpochDetailsController(int num)
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
            DataStoreCoordinator dsc = Lookup.getDefault().lookup(ConnectionProvider.class).getConnection();
            component = new ProtocolSelector(changeSupport, dsc);//EpochDetailsPanel
            System.out.println("creating component");
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        //read protocolID and protocolParameters
    }
    @Override
    public void storeSettings(WizardDescriptor data) {
        Map<String, Object> epoch;
        
        Protocol protocol = ((ProtocolSelector)component).getProtocol();
        if (protocol != null)
            data.putProperty(epochName + ".protocol", protocol);
        else{
            Map<String, String> newProtocols = ((ProtocolSelector)component).getNewProtocols();
            String name = ((ProtocolSelector)component).getProtocolName();
            data.putProperty(epochName + ".newProtocols", newProtocols);
            data.putProperty(epochName + ".protocolName", name);
        }
    }

    @Override
    public boolean isValid() {
      return true;
    }
    
}
