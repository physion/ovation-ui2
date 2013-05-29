/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.browser.insertion.BasicWizardPanel;
import us.physion.ovation.ui.browser.insertion.ProtocolSelector;
import us.physion.ovation.ui.browser.insertion.SourceSelector;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author jackie
 */
public class SourceController extends BasicWizardPanel {
    
    SourceController()
    {
        super();
    }
    
    @Override
    public JPanel getComponent() {
        if (component == null) {
            component = new NamedSourceSelector(changeSupport);
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        //read protocolID and protocolParameters
    }
    @Override
    public void storeSettings(WizardDescriptor data) {
        
        Map<String, Source> sources = ((NamedSourceSelector)component).getNamedSources();
        if (sources == null)
            sources = new HashMap();
       
        data.putProperty("sources", sources);
    }

    @Override
    public boolean isValid() {
      return true;
    }
    
}
