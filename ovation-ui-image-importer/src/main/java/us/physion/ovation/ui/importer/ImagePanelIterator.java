/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import org.openide.WizardDescriptor;
import us.physion.ovation.ui.interfaces.InsertEntityIterator;

import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author jackie
 */
public class ImagePanelIterator extends InsertEntityIterator
{

    public ImagePanelIterator(List<WizardDescriptor.Panel<WizardDescriptor>> panels)
    {
        super(panels);
    }

    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels()
    {
        if (panels == null) {
            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        }
        return panels;
    }

    public WizardDescriptor.Panel<WizardDescriptor> current()
    {
        if (index == 0) {
            //return first;
        }
        return getPanels().get(index);
    }

    public String name()
    {
        return index + 1 + " of " + getPanels().size();
    }

    public boolean hasNext()
    {
        return index < getPanels().size() - 1;
    }

    public boolean hasPrevious()
    {
        return index > 0;
    }

    public void nextPanel()
    {
        if (index == 0) {

        }
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }


    public void previousPanel()
    {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:                                                                                              
    public void addChangeListener(ChangeListener l)
    {
    }

    public void removeChangeListener(ChangeListener l)
    {
    }
    // If something changes dynamically (besides moving between panels), e.g.                                                                                       
    // the number of panels changes in response to user input, then use                                                                                             
    // ChangeSupport to implement add/removeChangeListener and call fireChange                                                                                      
    // when needed
}
