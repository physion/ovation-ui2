package us.physion.ovation.ui.editor;

import com.google.common.collect.Sets;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.*;
import java.util.concurrent.Future;
import javax.swing.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.domain.mixin.DataElementContainer;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//us.physion.ovation.editor//ResponseView//EN",
        autostore = false)
@TopComponent.Description(preferredID = "ResponseViewTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "us.physion.ovation.editor.ResponseViewTopComponent")
@ActionReference(path = "Menu/Window" /*
         * , position = 333
         */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ResponseViewAction",
        preferredID = "ResponseViewTopComponent")
@Messages({
    "CTL_ResponseViewAction=Selection View",
    "CTL_ResponseViewTopComponent=Selection Viewer",
    "HINT_ResponseViewTopComponent=Displays the current selected entity, if possible"
})
public final class ResponseViewTopComponent extends TopComponent {

    private final static class FixedHeightPanel extends JPanel {

        private int fixedHeight = -1;

        private void setFixedHeight(int height) {
            this.fixedHeight = height;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            if (fixedHeight != -1) {
                return new Dimension(d.width, fixedHeight);
            } else {
                return d;
            }
        }
        
        @Override
        public Dimension getMaximumSize() {
            Dimension d = super.getMaximumSize();
            if (fixedHeight != -1) {
                return new Dimension(d.width, fixedHeight);
            } else {
                return d;
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            if (fixedHeight != -1) {
                return new Dimension(d.width, fixedHeight);
            } else {
                return d;
            }
        }
        
    }
    
    private FixedHeightPanel contentPanel;
    Lookup.Result global;
    List<FixedHeightPanel> responsePanels = new ArrayList<FixedHeightPanel>();
    Future updateEntitySelection;
    private LookupListener listener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent le) {

            //TODO: we should have some other Interface for things that can update the tags view
            //then we could get rid of the Library dependancy on the Explorer API
            if (TopComponent.getRegistry().getActivated() instanceof ExplorerManager.Provider) {
                //resetTableEditor();
                updateEntitySelection();
            }
        }
    };

    public ResponseViewTopComponent() {
        initTopComponent();
    }

    private void initTopComponent() {
        initComponents();

        setName("Data Viewer");//Bundle.CTL_ResponseViewTopComponent());
        setToolTipText("Displays the selected DataElements");//Bundle.HINT_ResponseViewTopComponent());
        global = Utilities.actionsGlobalContext().lookupResult(IEntityWrapper.class);
        global.addLookupListener(listener);
    }
    
    private void initComponents() {
        contentPanel = new FixedHeightPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
        JScrollPane responseListPane = new JScrollPane(contentPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(responseListPane, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(responseListPane, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
        );
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        //responseListPane.setVisible(false);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    protected void updateEntitySelection() {
        
        final ProgressHandle progress = ProgressHandleFactory.createHandle("Updating view");
        final Collection<? extends IEntityWrapper> entities = global.allInstances();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                updateEntitySelection(entities, progress);
            }
        };

        if (updateEntitySelection != null && !updateEntitySelection.isDone()) {
            updateEntitySelection.cancel(true);
            LoggerFactory.getLogger(ResponseViewTopComponent.class).debug("Cancelled other thread");
        }
        updateEntitySelection = EventQueueUtilities.runOffEDT(r, progress);
    }

    protected List<Visualization> updateEntitySelection(Collection<? extends IEntityWrapper> entities, ProgressHandle progress) {
        if (progress != null) {
            progress.switchToDeterminate(entities.size());
        }
        int progressWorkUnit = 0;
        
        LinkedList<DataElement> responseList = new LinkedList<DataElement>();

        Iterator i = entities.iterator();
        while (i.hasNext()) {
            IEntityWrapper ew = (IEntityWrapper) i.next();
            if (DataElementContainer.class.isAssignableFrom(ew.getType())) {
                DataElementContainer container = (DataElementContainer) (ew.getEntity());//getEntity gets the context for the given thread
                responseList.addAll(Sets.newHashSet(container.getDataElements().values()));

                if (container instanceof Epoch) {
                    for (AnalysisRecord a : ((Epoch) container).getAnalysisRecords()) {
                        responseList.addAll(Sets.newHashSet(a.getOutputs().values()));
                    }
                }

            } else if (DataElement.class.isAssignableFrom(ew.getType())) {
                responseList.add((DataElement) ew.getEntity());
            }
            
            if (progress != null) {
                progress.progress(progressWorkUnit++);
            }
        }

        List<Visualization> responseGroups = new LinkedList<Visualization>();

        for (DataElement rw : responseList) {
            boolean added = false;
            for (Visualization group : responseGroups) {
                if (group.shouldAdd(rw)) {
                    group.add(rw);
                    added = true;
                    break;
                }
            }
            if (!added) {
                responseGroups.add(ResponseWrapperFactory.create(rw).createVisualization(rw));
            }
        }

        if (progress != null) {
            progress.switchToIndeterminate();
        }
        EventQueueUtilities.runOnEDT(updateChartRunnable(responseGroups));
        return responseGroups;
    }

    //for debugging
    protected static void error(String s) {
        JDialog d = new JDialog(new JFrame(), true);
        d.setPreferredSize(new Dimension(500, 500));
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setLocationRelativeTo(null);
        JLabel l = new JLabel();
        l.setText(s);
        d.add(l);
        d.setVisible(true);
    }

    private Runnable updateChartRunnable(final List<Visualization> responseGroups) {
        final int height = contentPanel.getParent().getHeight();
        return new Runnable() {
            @Override
            public void run() {
                while (!responsePanels.isEmpty()) {
                    Component c = responsePanels.remove(0);
                    contentPanel.remove(c);
                }

                if (!responseGroups.isEmpty()) {
                    //This is for setting each row in the table to a more appropriate height
                    int[] rowHeights = new int[responseGroups.size()];//highest allowable height for each row
                    int totalStrictHeight = 0;
                    int flexiblePanels = 0;
                    int minHeight = 150;//min height of a chart

                    for (Visualization c : responseGroups) {
                        Component p = c.generatePanel();

                        int row = responsePanels.size();
                        if (p instanceof StrictSizePanel) {
                            int strictHeight = ((StrictSizePanel) p).getStrictSize().height;
                            rowHeights[row] = strictHeight;
                            totalStrictHeight += strictHeight;
                        } else {
                            rowHeights[row] = Integer.MAX_VALUE;
                            flexiblePanels++;
                        }
                        
                        FixedHeightPanel wrap = new FixedHeightPanel();
                        wrap.setLayout(new BorderLayout());
                        wrap.add(p, BorderLayout.CENTER);
                        
                        responsePanels.add(wrap);
                    }
                    int flexiblePanelHeight = minHeight;
                    if (flexiblePanels != 0) {
                        flexiblePanelHeight = Math.max(minHeight, (height - totalStrictHeight) / flexiblePanels);
                    }
                    for (int i = 0; i < rowHeights.length; ++i) {
                        if (rowHeights[i] == Integer.MAX_VALUE) {
                            rowHeights[i] = flexiblePanelHeight;
                        }
                        responsePanels.get(i).setFixedHeight(rowHeights[i]);
                        
                        contentPanel.add(responsePanels.get(i));
                    }
                }
            }
        };
    }
}
