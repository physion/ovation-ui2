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
import us.physion.ovation.domain.Source;

/**
 *
 * @author huecotanks
 */
public class SourceNameSelectionController extends BasicWizardPanel{
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new ListSelectionPanel(changeSupport, 
                    "Select sources for this measurement", 
                    "Select Sources");
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        Epoch e = (Epoch)data.getProperty("epoch");
        
        Map<String, Source> sources = e.getInputSources();
        if (sources == null)
            sources = new HashMap();
        ArrayList l = new ArrayList(sources.keySet());
        Collections.sort(l);
        ListSelectionPanel c = (ListSelectionPanel)getComponent();
        
        Collection<String> selectedNames = (Collection<String>)data.getProperty("sourceNames");
        
        c.setNames(l, selectedNames);
    }
    @Override
    public void storeSettings(WizardDescriptor data) {
       
        ListSelectionPanel c = (ListSelectionPanel)getComponent();
        data.putProperty("sourceNames", c.getNames());
    }

    @Override
    public boolean isValid() {
      return true;
    }
}
