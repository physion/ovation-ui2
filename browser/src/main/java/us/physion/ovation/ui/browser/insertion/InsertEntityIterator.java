/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import com.google.common.collect.Sets;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import us.physion.ovation.domain.Epoch;

public final class InsertEntityIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    boolean includeProtocolInfo = false;
    boolean includeDeviceInfo = false;
    public InsertEntityIterator(List<WizardDescriptor.Panel<WizardDescriptor>> panels)
    {
        this.panels = panels;
        if (panels == null)
        {
            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        }
        createSteps();
        addProtocolAndDeviceListener();
        hideOrDisplayPanels();
    }
    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> filteredPanels;
    private String[] steps;
    private String[] currentSteps;

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        WizardDescriptor.Panel<WizardDescriptor> panel = filteredPanels.get(index);
        Component c = panel.getComponent();
        if (c instanceof JComponent) { // assume Swing components
            JComponent jc = (JComponent) c;
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index);
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, currentSteps);
        }
        return panel;
    }

    @Override
    public String name() {
        return index + 1 + " of " + filteredPanels.size();
    }

    @Override
    public boolean hasNext() {
        return index < filteredPanels.size() -1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    /*(private int findNextPanel()
    {
        int newIndex = index+1;
        while (newIndex < panels.size() &&!filteredPanels.contains(panels.get(newIndex)))
        {
            newIndex++;
        }
        return newIndex;
    }
    
    private int findPreviousPanel()
    {
        int newIndex = index-1;
        while (newIndex > -1 && !filteredPanels.contains(panels.get(newIndex)))
        {
            newIndex--;
        }
        return newIndex;
    }*/
  
    private void hideOrDisplayPanels() {
        filteredPanels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        List<String> updatedSteps = new ArrayList();
        int panelCount = -1;
        for(WizardDescriptor.Panel<WizardDescriptor> panel : panels)
        {
            panelCount++;
            if (panel instanceof SelectProtocolController && !includeProtocolInfo)
            {
                continue;
            }else if(panel instanceof DeviceNameSelectionController && !includeDeviceInfo)
            {
                continue;
            }
            else if (panel instanceof KeyValueController)
            {
                KeyValueController kpanel = (KeyValueController)panel;
                if (kpanel.wizardDescriptorKey.toLowerCase().contains("protocolparameters") && !includeProtocolInfo)
                {
                    continue;
                }
                else if (kpanel.wizardDescriptorKey.toLowerCase().contains("deviceparameters") && !includeDeviceInfo)
                {
                    continue;
                }
            }else if(panel instanceof SourceNameSelectionController)
            {
                Epoch epoch = ((SourceNameSelectionController)panel).epoch;
                if (epoch!= null && epoch.getInputSources() != null)
                {
                    continue;
                }
            }
            filteredPanels.add(panel);
            updatedSteps.add(steps[panelCount]);
        }
        currentSteps = updatedSteps.toArray(new String[filteredPanels.size()]);
    }
 
    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
        //cs.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        //cs.removeChangeListener(l);
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed

    private void addProtocolAndDeviceListener() {
        for(WizardDescriptor.Panel<WizardDescriptor> panel : panels)
        {
            if (panel instanceof StartAndEndTimeController)
            {
                ((StartAndEndTimeController)panel).changeSupport.addChangeListener(new ChangeListener() {

                    @Override
                    public void stateChanged(ChangeEvent e) {
                        boolean refilterPanels = false;
                        if (includeProtocolInfo != ((StartAndEndTimeController)e.getSource()).includeProtocolInfo())
                        {
                            includeProtocolInfo = !includeProtocolInfo;
                            refilterPanels = true;
                        }
                        if(includeDeviceInfo != ((StartAndEndTimeController)e.getSource()).includeDeviceInfo())
                        {
                            includeDeviceInfo = !includeDeviceInfo;
                            refilterPanels = true;
                        }
                        if (refilterPanels)
                        {
                            hideOrDisplayPanels();
                        }
                    }
                });
            }
        }
    }
    
    private void createSteps()
    {
        steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            WizardDescriptor.Panel<WizardDescriptor> panel = panels.get(i);
            Component c = panel.getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
    }
}
