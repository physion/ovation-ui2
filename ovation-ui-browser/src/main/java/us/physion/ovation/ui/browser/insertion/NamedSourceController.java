/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

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
import us.physion.ovation.ui.browser.insertion.NamedSourceSelector;
import us.physion.ovation.ui.browser.insertion.ProtocolSelector;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author jackie
 */
public class NamedSourceController extends BasicWizardPanel {
    
    Map<String, Source> defaultValues;
    String wdKey;
    String explanation;
    public NamedSourceController(String key, Map<String, Source> defaultSelectedValues, String explanation)
    {
        super();
        wdKey = key;
        defaultValues = defaultSelectedValues;
        this.explanation = explanation;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new NamedSourceSelector(changeSupport, defaultValues, explanation);
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        //sources
        Map<String, Source> sources = (Map<String, Source>)data.getProperty(wdKey);
        ((NamedSourceSelector)component).addSources(sources);
        
    }
    @Override
    public void storeSettings(WizardDescriptor data) {
        ((NamedSourceSelector)component).finish();//saves edited value to table model, if not saved yet
        Map<String, Source> sources = ((NamedSourceSelector)component).getNamedSources();
        if (sources == null)
            sources = new HashMap();
       
        data.putProperty(wdKey, sources);
    }

    @Override
    public boolean isValid() {
        Map<String, Source> sources = ((NamedSourceSelector)component).getNamedSources();
        return sources != null && !sources.isEmpty();
    }
    
}
