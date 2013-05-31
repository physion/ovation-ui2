/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import javax.swing.JPanel;
import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.junit.*;
import org.openide.WizardDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import ovation.*;
import us.physion.ovation.domain.Project;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;

/**
 *
 * @author huecotanks
 */
public class InsertProjectTest extends OvationTestCase
{
    //InsertEntity action methods
    @Test
    public void testGetPanels() {
        InsertProject insert = new InsertProject();
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = insert.getPanels(null);
        TestCase.assertEquals(panels.size(), 1);
        
        TestCase.assertTrue(panels.get(0) instanceof InsertProjectWizardPanel1);
    }
    
    @Test
    public void testWizardFinished()
    {
        String name = "name";
        String purpose = "purpose";
        DateTime start = new DateTime(0);
       
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        d.putProperty("project.name", name);
        d.putProperty("project.purpose", purpose);
        d.putProperty("project.start", start);
        
        new InsertProject().wizardFinished(d, dsc, null);
        
        Project p = (Project)(ctx.getProjects().iterator().next());
        TestCase.assertEquals(p.getName(), name);
        TestCase.assertEquals(p.getPurpose(), purpose);
        TestCase.assertEquals(p.getStart(), start);
    }
    
    //Panel 1 methods
    @Test
    public void testNameForPanel1()
    {
        InsertProjectVisualPanel1 s = new InsertProjectVisualPanel1(new ChangeSupport(this));
        TestCase.assertEquals(s.getName(), "Insert Project");
    }
    
    @Test
    public void testPanel1Validity()
    {
        DummyPanel1 p = new DummyPanel1();
        ChangeSupport cs = new ChangeSupport(p);
        InsertProjectVisualPanel1 panel = new InsertProjectVisualPanel1(cs);
        p.component = panel;
        DummyChangeListener l = new DummyChangeListener();
        cs.addChangeListener(l);
        
        String name = "name";
        String purpose = "purpose";
        DateTime start = new DateTime(0);
        DateTime end = new DateTime(1);
        
        TestCase.assertFalse(p.isValid());
        l.resetStateChanged();
        
        panel.setProjectName(name);
        TestCase.assertTrue(l.getStateChanged());
        l.resetStateChanged();
        panel.setPurpose(purpose);
        TestCase.assertTrue(l.getStateChanged());
        l.resetStateChanged();
        panel.setStart(start);
        TestCase.assertTrue(l.getStateChanged());
        l.resetStateChanged();
        TestCase.assertTrue(p.isValid());
        
        panel.setStart(null);
        TestCase.assertFalse(p.isValid());
        
        panel.setStart(start);
        panel.setProjectName("");
        TestCase.assertFalse(p.isValid());
        
        panel.setProjectName(name);
        panel.setPurpose("");
        TestCase.assertFalse(p.isValid());
    }
    
    @Test
    public void testPanel1WritesProperties()
    {
        DummyPanel1 p = new DummyPanel1();
        InsertProjectVisualPanel1 panel = new InsertProjectVisualPanel1(new ChangeSupport(p));
        p.component = panel;
        
        String name = "name";
        String purpose = "purpose";
        DateTime start = new DateTime(0);
        
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        
        p.storeSettings(d);
        TestCase.assertTrue(((String)d.getProperty("project.name")).isEmpty());
        TestCase.assertTrue(((String)d.getProperty("project.purpose")).isEmpty());
        TestCase.assertNull(d.getProperty("project.start"));
        
        panel.setProjectName(name);
        panel.setPurpose(purpose);
        panel.setStart(start);
        p.storeSettings(d);
        TestCase.assertEquals(d.getProperty("project.name"), name);
        TestCase.assertEquals(d.getProperty("project.purpose"), purpose);
        TestCase.assertEquals(d.getProperty("project.start"), start);
        
    }
    private class DummyPanel1 extends InsertProjectWizardPanel1
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
