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
import java.util.Map;
import org.openide.WizardDescriptor;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.mixin.DataElement;

/**
 *
 * @author huecotanks
 */
public class NamedDataElementController extends BasicWizardPanel{
    String analysisRecordsKey;
    
    public NamedDataElementController(String key)
    {
        super();
        analysisRecordsKey = key;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new NamedDataElementSelectionPanel(changeSupport);
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        Map<String, OvationEntity> dataelements = (Map<String, OvationEntity>)data.getProperty(analysisRecordsKey);
        
        NamedDataElementSelectionPanel c = (NamedDataElementSelectionPanel)getComponent();
        c.addSelectedEntities(dataelements);
    }
    @Override
    public void storeSettings(WizardDescriptor data) {
       
        NamedDataElementSelectionPanel c = (NamedDataElementSelectionPanel)getComponent();
        c.finishEditing();
        data.putProperty(analysisRecordsKey, c.getNamedEntities());
    }

    @Override
    public boolean isValid() {
      return true;
    } 
}
