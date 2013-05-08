/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.util.Map;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

public class InsertEpochGroupWizardPanel1 extends BasicWizardPanel {

    @Override
    public JPanel getComponent() {
        if (component == null) {
            DataStoreCoordinator dsc = Lookup.getDefault().lookup(ConnectionProvider.class).getConnection();
            component = new ProtocolSelector(changeSupport, dsc);//Protocol
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        return true;
    }


    @Override
    public void storeSettings(WizardDescriptor wiz) {
        Protocol protocol = ((ProtocolSelector)component).getProtocol();
        if (protocol != null)
            wiz.putProperty("epochGroup.protocol", protocol);
        else{
            Map<String, String> newProtocols = ((ProtocolSelector)component).getNewProtocols();
            String name = ((ProtocolSelector)component).getProtocolName();
            wiz.putProperty("epochGroup.newProtocols", newProtocols);
            wiz.putProperty("epochGroup.protocolName", name);
        }
    }
}
