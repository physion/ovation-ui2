/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.junit.*;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import ovation.Experiment;
import ovation.LogLevel;
import ovation.Ovation;
import ovation.Project;
import us.physion.ovation.ui.browser.BrowserTestManager;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;
import us.physion.ovation.ui.test.TestManager;

import javax.swing.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

/**
 *
 * @author huecotanks
 */
public class InsertExperimentTest extends OvationTestCase
{
    static TestManager mgr = new BrowserTestManager();
    
    public InsertExperimentTest() {
        setTestManager(mgr); //this is because there are static and non-static methods that need to use the test manager
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        Ovation.enableLogging(LogLevel.ALL);
        AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            public Boolean run() {
                OvationTestCase.setUpDatabase(mgr, 2);
                return true;
            }
        });
        
    }
    
    @Before
    public void setUp() {
        dsc = setUpTest();
    }
    
    
    @After
    public void tearDown()
    {
        tearDownTest();
    }
    
     @AfterClass
    public static void tearDownClass() throws Exception {
        OvationTestCase.tearDownDatabase(mgr);
    }

    //InsertEntity action methods
    @Test
    public void testGetPanels() {
        InsertExperiment insert = new InsertExperiment();
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = insert.getPanels(null);
        TestCase.assertEquals(panels.size(), 1);
        
        TestCase.assertTrue(panels.get(0) instanceof InsertExperimentWizardPanel1);
    }
    
    @Test
    public void testWizardFinished()
    {
        String purpose = "purpose";
        DateTime start = new DateTime(0);
        DateTime end = new DateTime(1);
       
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        d.putProperty("experiment.purpose", purpose);
        d.putProperty("experiment.start", start);
        d.putProperty("experiment.end", end);
        
        Project p = dsc.getContext().insertProject("name", "purpose", new DateTime(0));
        IEntityWrapper w = new TestEntityWrapper(dsc, p);
        new InsertExperiment().wizardFinished(d, dsc, w);
        
        Experiment ex = p.getExperiments()[0];
        TestCase.assertEquals(ex.getPurpose(), purpose);
        TestCase.assertEquals(ex.getStartTime(), start);
        TestCase.assertEquals(ex.getEndTime(), end);
    }
    
    //Panel 1 methods
    @Test
    public void testNameForPanel1()
    {
        InsertExperimentVisualPanel1 s = new InsertExperimentVisualPanel1(new ChangeSupport(this));
        TestCase.assertEquals(s.getName(), "Insert Experiment");
    }
    
    @Test
    public void testPanel1Validity()
    {
        DummyPanel1 p = new DummyPanel1();
        ChangeSupport cs = new ChangeSupport(p);
        InsertExperimentVisualPanel1 panel = new InsertExperimentVisualPanel1(cs);
        DummyChangeListener l = new DummyChangeListener();
        cs.addChangeListener(l);
        p.component = panel;
        
        String purpose = "purpose";
        DateTime start = new DateTime(0);
        DateTime end = new DateTime(1);
        
        TestCase.assertFalse(p.isValid());
        l.resetStateChanged();
        
        panel.setPurpose(purpose);
        TestCase.assertTrue(l.getStateChanged());
        l.resetStateChanged();
        panel.setStart(start);
        TestCase.assertTrue(l.getStateChanged());
        l.resetStateChanged();
        panel.setEnd(end);
        TestCase.assertTrue(l.getStateChanged());
        l.resetStateChanged();
        TestCase.assertTrue(p.isValid());
        
        panel.setStart(null);
        TestCase.assertFalse(p.isValid());
        
        panel.setStart(new DateTime(3));
        panel.setEnd(new DateTime(0));
        TestCase.assertFalse(p.isValid());// startTime > endTime
        
        panel.setStart(start);
        panel.setEnd(end);
        panel.setPurpose("");
        TestCase.assertFalse(p.isValid());
    }
    
    @Test
    public void testPanel1WritesProperties()
    {
        DummyPanel1 p = new DummyPanel1();
        InsertExperimentVisualPanel1 panel = new InsertExperimentVisualPanel1(new ChangeSupport(p));
        p.component = panel;
        
        String purpose = "purpose";
        DateTime start = new DateTime(0);
        DateTime end = new DateTime(1);
        
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        
        p.storeSettings(d);
        TestCase.assertTrue(((String)d.getProperty("experiment.purpose")).isEmpty());
        TestCase.assertNull(d.getProperty("experiment.start"));
        TestCase.assertNull(d.getProperty("experiment.end"));
        
        panel.setPurpose(purpose);
        panel.setStart(start);
        panel.setEnd(end);
        p.storeSettings(d);
        TestCase.assertEquals(d.getProperty("experiment.purpose"), purpose);
        TestCase.assertEquals(d.getProperty("experiment.start"), start);
        TestCase.assertEquals(d.getProperty("experiment.end"), end);
        
    }
    private class DummyPanel1 extends InsertExperimentWizardPanel1
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

