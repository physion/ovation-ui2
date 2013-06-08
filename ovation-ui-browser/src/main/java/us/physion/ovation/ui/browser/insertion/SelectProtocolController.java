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
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

public class SelectProtocolController extends BasicWizardPanel {
    
    String objectPrefix;
    
    public SelectProtocolController(String objectPrefix)
    {
         if (objectPrefix == null || objectPrefix.isEmpty())
            throw new OvationException("No object prefix set for Protocol Selector");
         this.objectPrefix = objectPrefix;
    }
    @Override
    public JPanel getComponent() {
        if (component == null) {
            DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
            component = new ProtocolSelector(changeSupport, ctx);//Protocol
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
    public void storeSettings(WizardDescriptor wiz) {
        Protocol protocol = ((ProtocolSelector)component).getProtocol();
        if (protocol != null)
            wiz.putProperty(objectPrefix + ".protocol", protocol);
        else{
            Map<String, String> newProtocols = ((ProtocolSelector)component).getNewProtocols();
            String name = ((ProtocolSelector)component).getProtocolName();
            wiz.putProperty(objectPrefix + ".newProtocols", newProtocols);
            wiz.putProperty(objectPrefix + ".protocolName", name);
        }
    }

    @Override
    public void readSettings(WizardDescriptor data) {
    }
}
