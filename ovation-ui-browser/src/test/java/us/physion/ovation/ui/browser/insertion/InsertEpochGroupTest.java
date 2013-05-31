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
import us.physion.ovation.domain.*;
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

    public InsertEpochGroupTest() {
    }
    
    //InsertEntity action methods
    @Test
    public void testGetPanelsForParentEpochGroup() {
        DateTime start = new DateTime();
        EpochGroup e = ctx.insertProject("name", "purpose", start).insertExperiment("purpose", start).insertEpochGroup("different-label", start, null, null, null);
        IEntityWrapper parent = new TestEntityWrapper(ctx, e);
        InsertEpochGroup insert = new InsertEpochGroup();
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = insert.getPanels(parent);
        TestCase.assertEquals(panels.size(), 4);
        
        TestCase.assertTrue(panels.get(0) instanceof SelectProtocolController);
        TestCase.assertTrue(panels.get(1) instanceof ProtocolParametersController);
        TestCase.assertTrue(panels.get(2) instanceof DeviceParametersController);
        TestCase.assertTrue(panels.get(3) instanceof InsertEpochGroupWizardPanel);
        //TODO: test the parent's source is set appropriately, when we start using that code
    }
    
    @Test
    public void testWizardFinishedForInsertionIntoAnExperiment()
    {
        String label = "label";
        DateTime start = new DateTime(0);
        DateTime end = new DateTime(1);
        Source src = ctx.insertSource("Mouse", "45");
        IEntityWrapper s = new TestEntityWrapper(ctx, src);
       
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        d.putProperty("epochGroup.source", s);
        d.putProperty("epochGroup.label", label);
        d.putProperty("epochGroup.start", start);
        
        Experiment e = ctx.insertProject("name", "purpose", start).insertExperiment("purpose", start);
        IEntityWrapper parent = new TestEntityWrapper(ctx, e);
        
        new InsertEpochGroup().wizardFinished(d, dsc, parent);
        
        EpochGroup eg = e.getEpochGroups().iterator().next();
        TestCase.assertEquals(eg.getLabel(), label);
        TestCase.assertEquals(eg.getStart(), start);
    }
    
    @Test
    public void testWizardFinishedForInsertionIntoAnEpochGroup()
    {
        String label = "label";
        DateTime start = new DateTime(0);
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        d.putProperty("epochGroup.label", label);
        d.putProperty("epochGroup.start", start);
        
        EpochGroup e = ctx.insertProject("name", "purpose", start).insertExperiment("purpose", start).insertEpochGroup("different-label", start, null, null, null);
        IEntityWrapper parent = new TestEntityWrapper(ctx, e);
        
        new InsertEpochGroup().wizardFinished(d, dsc, parent);
        
        EpochGroup eg = e.getEpochGroups().iterator().next();
        TestCase.assertEquals(eg.getLabel(), label);
        TestCase.assertEquals(eg.getStart(), start);
    }
    
    //Panel 1 methods
    @Test
    public void testNameForPanel1()
    {
        SourceSelector s = new SourceSelector(new ChangeSupport(this), null, ctx);
        TestCase.assertEquals(s.getName(), "Select a Source");
    }
    @Test
    public void testSourceSelectorSetsSelectedSource()
    {
        DummyChangeListener listener = new DummyChangeListener();
        SourceSelector s = new SourceSelector(listener.getChangeSupport(), null, ctx);
        
        TestCase.assertFalse(listener.getStateChanged());
        Source src = ctx.insertSource("a new source", "89");
        s.setSource(new TestEntityWrapper(ctx, src));
        TestCase.assertTrue(listener.getStateChanged());
        
        TestCase.assertEquals(src.getUuid(), s.getSource().getEntity().getUuid());
    }
    
    @Test
    public void testSourceSelectorValidity()
    {
        DummyPanel1 p = new DummyPanel1();
        ChangeSupport cs = new ChangeSupport(p);
        SourceSelector ss = new SourceSelector(cs, null, ctx);
        DummyChangeListener l = new DummyChangeListener();
        cs.addChangeListener(l);
        p.component = ss;
        
        TestCase.assertTrue(p.isValid());
        l.resetStateChanged();
        ss.setSource( new TestEntityWrapper(ctx, ctx.insertSource("label", "89")));
        TestCase.assertTrue(l.getStateChanged());
    }
    
    @Test
    public void testSourceSelectorWritesProperties()
    {
        DummyPanel1 p = new DummyPanel1();
        SourceSelector ss = new SourceSelector(new ChangeSupport(p), null, ctx);
        p.component = ss;
        WizardDescriptor d = new WizardDescriptor(new InsertEntityIterator(null));
        
        p.storeSettings(d);
        TestCase.assertNull(d.getProperty("epochGroup.source"));
        
        Source src = ctx.insertSource("new label", "blah");
        ss.setSource( new TestEntityWrapper(ctx, src));
        p.storeSettings(d);
        TestCase.assertEquals(((IEntityWrapper)d.getProperty("epochGroup.source")).getEntity().getUuid(), src.getUuid());
        
    }

    @Test
    public void testSourceSelectorResetsSourceView()
    {
        //TODO:
    }
    
    @Test
    public void testSourceSelectorRunsQueryForSources()
    {
        //TODO:
    }
    
    
    private class DummyPanel1 extends SelectProtocolController 
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
    
    private class DummyPanel2 extends InsertEpochGroupWizardPanel 
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
        TestCase.assertFalse(p.isValid());// no start time
        
        panel.setStart(new DateTime());
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
        
        panel.setLabel(label);
        panel.setStart(start);
        p.storeSettings(d);
        TestCase.assertEquals(d.getProperty("epochGroup.label"), label);
        TestCase.assertEquals(d.getProperty("epochGroup.start"), start);
    }
}
