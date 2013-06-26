/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.Component;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.openide.WizardDescriptor;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.BasicWizardPanel;

/**
 *
 * @author huecotanks
 */
public class KeyValueController extends BasicWizardPanel{

    KeyValuePanel c;
    String name;
    String description;
    String wizardDescriptorKey;
    public KeyValueController(String name, String description, String wizardDescriptorKey)
    {
        this.name = name;
        this.description = description;
        this.wizardDescriptorKey = wizardDescriptorKey;
    }
    
    @Override
    public Component getComponent() {
        if (c == null)
        {
            c = new KeyValuePanel(changeSupport, name, description);
        }
        return c;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        String[] keys = wizardDescriptorKey.split(";");
        Object o = data.getProperty(keys[0]);
        for (int i=1; i<keys.length; i++)
        {
            if (o instanceof List)
            {
                int index = Integer.parseInt(keys[i]);
                o = ((List)o).get(index);
            }else if (o instanceof Map)
            {
                o = ((Map)o).get(keys[i]);
            }
        }
        KeyValuePanel panel = (KeyValuePanel) getComponent();
        panel.setParameters((Map<String, Object>)o);
    }
    //TODO: cleanup this logic
    @Override
    public void storeSettings(WizardDescriptor data) {
        KeyValuePanel panel = (KeyValuePanel) getComponent();
        String[] keys = wizardDescriptorKey.split(";");
        Object o = data.getProperty(keys[0]);
        Stack s = new Stack();
        s.push(o);
        for (int i=1; i<keys.length; i++)
        {
            if (o instanceof List)
            {
                int index = Integer.parseInt(keys[i]);
                o = ((List)o).get(index);
                s.push(o);

            }else if (o instanceof Map)
            {
                o = ((Map)o).get(keys[i]);
                s.push(o);
            }
        }
        
        s.pop();//old map
        Object modifiedElement = panel.getParameters();//new map
        int keyIndex = keys.length-1;
        while (!s.isEmpty())
        {
            o = s.pop();
            
            if (o instanceof List)
            {
                int index = Integer.parseInt(keys[keyIndex]);
                ((List)o).set(index, modifiedElement);
                modifiedElement = o;
                keyIndex--;

            }else if (o instanceof Map)
            {
                ((Map)o).put(keys[keyIndex], modifiedElement);
                modifiedElement = o;
                keyIndex--;
            }else{
                throw new OvationException("Invalid key name for KeyValueController");
            }
        }
        
        data.putProperty(keys[0], modifiedElement);
    }

    @Override
    public boolean isValid() {
        return true;
    }
    
}
