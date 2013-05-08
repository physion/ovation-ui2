/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import junit.framework.TestCase;
import org.junit.*;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import us.physion.ovation.ui.test.OvationTestCase;

import javax.swing.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import us.physion.ovation.domain.Source;

/**
 *
 * @author huecotanks
 */
public class InsertSourceTest extends OvationTestCase
{
    //InsertEntity action methods
    @Test
    public void testGetPanels() {
        InsertSource insert = new InsertSource();
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = insert.getPanels(null);
        TestCase.assertEquals(panels.size(), 1);
        
        TestCase.assertTrue(panels.get(0) instanceof InsertSourceWizardPanel1);
    }
    
    @Test
    public void testWizardFinished()
    {
        String label = "label";
       
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        d.putProperty("source.label", label);
        
        new InsertSource().wizardFinished(d, dsc, null);
        
        Source s = dsc.getContext().getTopLevelSources().iterator().next();
        TestCase.assertEquals(s.getLabel(), label);
    }
    
    //Panel 1 methods
    @Test
    public void testNameForPanel1()
    {
        InsertSourceVisualPanel1 s = new InsertSourceVisualPanel1(new ChangeSupport(this));
        TestCase.assertEquals(s.getName(), "Insert Source");
    }
    
    @Test
    public void testPanel1Validity()
    {
        DummyPanel1 p = new DummyPanel1();
        ChangeSupport cs = new ChangeSupport(p);
        InsertSourceVisualPanel1 panel = new InsertSourceVisualPanel1(cs);
        DummyChangeListener l = new DummyChangeListener();
        cs.addChangeListener(l);
        p.component = panel;
        
        String label = "label";
        
        TestCase.assertFalse(p.isValid());
        l.resetStateChanged();
        
        panel.setLabel(label);
        TestCase.assertTrue(l.getStateChanged());
        l.resetStateChanged();
        TestCase.assertTrue(p.isValid());
    }
    
    @Test
    public void testPanel1WritesProperties()
    {
        DummyPanel1 p = new DummyPanel1();
        InsertSourceVisualPanel1 panel = new InsertSourceVisualPanel1(new ChangeSupport(p));
        p.component = panel;
        
        String label = "label";
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        
        p.storeSettings(d);
        TestCase.assertTrue(((String)d.getProperty("source.label")).isEmpty());
        
        panel.setLabel(label);
        p.storeSettings(d);
        TestCase.assertEquals(d.getProperty("source.label"), label);
    }
    
    private class DummyPanel1 extends InsertSourceWizardPanel1
    {
        DummyPanel1()
        {
            super();
        }
        @Override
        public JPanel getComponent()
        {
            return null;
        }
    }
}
