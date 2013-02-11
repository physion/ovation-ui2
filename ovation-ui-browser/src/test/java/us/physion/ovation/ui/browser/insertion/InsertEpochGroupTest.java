/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.junit.*;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import ovation.*;
import us.physion.ovation.ui.test.TestManager;
import us.physion.ovation.ui.browser.BrowserTestManager;
import us.physion.ovation.ui.browser.EntityWrapperUtilities;
import us.physion.ovation.ui.browser.QueryChildren;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;

/**
 *
 * @author huecotanks
 */
public class InsertEpochGroupTest extends OvationTestCase{

    static TestManager mgr = new BrowserTestManager();
    public InsertEpochGroupTest() {
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
    public void testGetPanelsForParentEpochGroup() {
        DateTime start = new DateTime();
        EpochGroup e = dsc.getContext().insertProject("name", "purpose", start).insertExperiment("purpose", start).insertEpochGroup("different-label", start);
        IEntityWrapper parent = new TestEntityWrapper(dsc, e);
        InsertEpochGroup insert = new InsertEpochGroup();
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = insert.getPanels(parent);
        TestCase.assertEquals(panels.size(), 2);
        
        TestCase.assertTrue(panels.get(0) instanceof InsertEpochGroupWizardPanel1);
        TestCase.assertTrue(panels.get(1) instanceof InsertEpochGroupWizardPanel2);
        //TODO: test the parent's source is set appropriately, when we start using that code
    }
    
    @Test
    public void testWizardFinishedForInsertionIntoAnExperiment()
    {
        String label = "label";
        DateTime start = new DateTime(0);
        DateTime end = new DateTime(1);
        Source src = dsc.getContext().insertSource("Mouse");
        IEntityWrapper s = new TestEntityWrapper(dsc, src);
       
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        d.putProperty("epochGroup.source", s);
        d.putProperty("epochGroup.label", label);
        d.putProperty("epochGroup.start", start);
        d.putProperty("epochGroup.end", end);
        
        Experiment e = dsc.getContext().insertProject("name", "purpose", start).insertExperiment("purpose", start);
        IEntityWrapper parent = new TestEntityWrapper(dsc, e);
        
        new InsertEpochGroup().wizardFinished(d, dsc, parent);
        
        EpochGroup eg = e.getEpochGroups()[0];
        TestCase.assertEquals(eg.getLabel(), label);
        TestCase.assertEquals(eg.getStartTime(), start);
        TestCase.assertEquals(eg.getEndTime(), end);
        TestCase.assertEquals(eg.getSource().getUuid(), src.getUuid());
    }
    
    @Test
    public void testWizardFinishedForInsertionIntoAnEpochGroup()
    {
        String label = "label";
        DateTime start = new DateTime(0);
        DateTime end = null;
        Source src = dsc.getContext().insertSource("Mouse");
        IEntityWrapper s = new TestEntityWrapper(dsc, src);
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        d.putProperty("epochGroup.source", s);
        d.putProperty("epochGroup.label", label);
        d.putProperty("epochGroup.start", start);
        d.putProperty("epochGroup.end", end);
        
        EpochGroup e = dsc.getContext().insertProject("name", "purpose", start).insertExperiment("purpose", start).insertEpochGroup("different-label", start, end);
        IEntityWrapper parent = new TestEntityWrapper(dsc, e);
        
        new InsertEpochGroup().wizardFinished(d, dsc, parent);
        
        EpochGroup eg = e.getChildren()[0];
        TestCase.assertEquals(eg.getLabel(), label);
        TestCase.assertEquals(eg.getStartTime(), start);
        TestCase.assertEquals(eg.getEndTime(), end);
        TestCase.assertEquals(eg.getSource().getUuid(), src.getUuid());
    }
    
    //Panel 1 methods
    @Test
    public void testNameForPanel1()
    {
        SourceSelector s = new SourceSelector(null, null, dsc);
        TestCase.assertEquals(s.getName(), "Select a Source");
    }
    @Test
    public void testSourceSelectorSetsSelectedSource()
    {
        DummyChangeListener listener = new DummyChangeListener();
        SourceSelector s = new SourceSelector(listener.getChangeSupport(), null, dsc);
        
        TestCase.assertFalse(listener.getStateChanged());
        Source src = dsc.getContext().insertSource("a new source");
        s.setSource(new TestEntityWrapper(dsc, src));
        TestCase.assertTrue(listener.getStateChanged());
        
        TestCase.assertEquals(src.getUuid(), s.getSource().getEntity().getUuid());
    }
    
    @Test
    public void testSourceSelectorValidity()
    {
        DummyPanel1 p = new DummyPanel1();
        ChangeSupport cs = new ChangeSupport(p);
        SourceSelector ss = new SourceSelector(cs, null, dsc);
        DummyChangeListener l = new DummyChangeListener();
        cs.addChangeListener(l);
        p.component = ss;
        
        TestCase.assertTrue(p.isValid());
        l.resetStateChanged();
        ss.setSource( new TestEntityWrapper(dsc, dsc.getContext().insertSource("label")));
        TestCase.assertTrue(l.getStateChanged());
    }
    
    @Test
    public void testSourceSelectorWritesProperties()
    {
        DummyPanel1 p = new DummyPanel1();
        SourceSelector ss = new SourceSelector(new ChangeSupport(p), null, dsc);
        p.component = ss;
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        
        p.storeSettings(d);
        TestCase.assertNull(d.getProperty("epochGroup.source"));
        
        Source src = dsc.getContext().insertSource("new label");
        ss.setSource( new TestEntityWrapper(dsc, src));
        p.storeSettings(d);
        TestCase.assertEquals(((IEntityWrapper)d.getProperty("epochGroup.source")).getEntity().getUuid(), src.getUuid());
        
    }

    @Test
    public void testSourceSelectorResetsSourceView()
    {
    }
    
    @Test
    public void testSourceSelectorRunsQueryForSources()
    {
    }
    
    
    private class DummyPanel1 extends InsertEpochGroupWizardPanel1 
    {
        DummyPanel1()
        {
            super(null);
        }
        @Override
        public JPanel getComponent()
        {
            return null;
        }
    }
    
    private class DummyPanel2 extends InsertEpochGroupWizardPanel2 
    {
        @Override
        public JPanel getComponent()
        {
            return null;
        }
    }
    
    //Panel2 methods
    @Test
    public void testNameForPanel2()
    {
        InsertEpochGroupVisualPanel2 s = new InsertEpochGroupVisualPanel2(new ChangeSupport(this));
        TestCase.assertEquals(s.getName(), "Insert Epoch Group");
    }
    
    @Test
    public void testPanel2Validity()
    {
        DummyPanel2 p = new DummyPanel2();
        ChangeSupport cs = new ChangeSupport(p);
        InsertEpochGroupVisualPanel2 panel = new InsertEpochGroupVisualPanel2(cs);
        DummyChangeListener l = new DummyChangeListener();
        cs.addChangeListener(l);

        p.component = panel;
        
        TestCase.assertFalse(p.isValid());// no label
        l.resetStateChanged();
        panel.setLabel("label");
        TestCase.assertTrue(l.getStateChanged());
        l.resetStateChanged();
        panel.setStart(null);
        TestCase.assertTrue(l.getStateChanged());
        l.resetStateChanged();
        panel.setEnd(null);
        TestCase.assertTrue(l.getStateChanged());
        l.resetStateChanged();
        TestCase.assertFalse(p.isValid());// no start time
        
        panel.setStart(new DateTime());
        TestCase.assertTrue(p.isValid());
        
        panel.setStart(null);
        panel.setEnd(new DateTime(0));
        TestCase.assertFalse(p.isValid());// no start time
        
        panel.setStart(new DateTime(1));// end < start
        TestCase.assertFalse(p.isValid());
        
        panel.setEnd(new DateTime(2));
        TestCase.assertTrue(p.isValid());
    }
    
    @Test
    public void testPanel2WritesProperties()
    {
        DummyPanel2 p = new DummyPanel2();
        InsertEpochGroupVisualPanel2 panel = new InsertEpochGroupVisualPanel2(new ChangeSupport(p));
        p.component = panel;
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        String label = "l";
        DateTime start = new DateTime(0);
        DateTime end = new DateTime(1);
        
        p.storeSettings(d);
        TestCase.assertTrue(((String)d.getProperty("epochGroup.label")).isEmpty());
        TestCase.assertNull(d.getProperty("epochGroup.start"));
        TestCase.assertNull(d.getProperty("epochGroup.end"));
        
        panel.setLabel(label);
        panel.setStart(start);
        panel.setEnd(end);
        p.storeSettings(d);
        TestCase.assertEquals(d.getProperty("epochGroup.label"), label);
        TestCase.assertEquals(d.getProperty("epochGroup.start"), start);
        TestCase.assertEquals(d.getProperty("epochGroup.end"), end);
        
    }
}
