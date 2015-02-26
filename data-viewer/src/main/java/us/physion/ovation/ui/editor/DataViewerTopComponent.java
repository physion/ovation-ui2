package us.physion.ovation.ui.editor;

import com.google.common.collect.Lists;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Future;
import javax.swing.*;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.domain.mixin.Content;
import us.physion.ovation.ui.actions.spi.ResourceLookupProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityNode;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//us.physion.ovation.editor//DataViewer//EN",
        autostore = false)
@TopComponent.Description(preferredID = "DataViewerTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "us.physion.ovation.editor.DataViewerTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_DataViewerAction",
        preferredID = "DataViewerTopComponent")
@Messages({
    "CTL_DataViewerAction=Data Viewer",
    "CTL_DataViewerTopComponent=Data Viewer",
    "HINT_DataViewerTopComponent=Displays the current entity selection",
    "# {0} - data element name",
    "Temporary_Data_Viewer_Title=Data Viewer: {0}",
    "Temporary_Data_Viewer_Loading=Opening...",
    "Main_Data_Viewer_Name=Data Viewer"
})
public final class DataViewerTopComponent extends TopComponent {

    private final static class TemporaryViewTopComponent extends TopComponent {

        private final List<AbstractAction> tabActions = Lists.newArrayList();

        public TemporaryViewTopComponent(final Resource element) {
            setName(Bundle.Temporary_Data_Viewer_Title(element.getLabel()));
            setLayout(new BorderLayout());
            setBackground(Color.white);
            EventQueueUtilities.runOffEDT(() -> {
                final DataVisualization v = ResponseWrapperFactory.create(element).createVisualization(element);
                
                EventQueueUtilities.runOnEDT(() -> {
                    add(v.generatePanel(), BorderLayout.CENTER);
                });
            }, ProgressHandleFactory.createHandle(Bundle.Temporary_Data_Viewer_Loading()));
        }

        @Override
        public int getPersistenceType() {
            return PERSISTENCE_NEVER;
        }

        private void addTabAction(AbstractAction a) {
            this.tabActions.add(a);
        }

        @Override
        public Action[] getActions() {
            Action[] other = super.getActions();
            if (tabActions.isEmpty()) {
                return other;
            } else {
                Action[] merged = new Action[tabActions.size() + 1 + other.length];
                System.arraycopy(tabActions.toArray(), 0, merged, 0, tabActions.size());
                //separator
                merged[tabActions.size()] = null;
                System.arraycopy(other, 0, merged, tabActions.size() + 1, other.length);

                return merged;
            }
        }

    }

    @ServiceProvider(service = ResourceLookupProvider.class)
    public final static class ResourceViewer implements ResourceLookupProvider {

        @Override
        public Lookup getLookup(final Resource element) {
            return getLookup(element, null);
        }

        @Override
        public Lookup getLookup(final Resource element, final List<URI> entityURI) {
            return Lookups.singleton(new Openable() {
                @Override
                public void open() {
                    TemporaryViewTopComponent t = new TemporaryViewTopComponent(element);

                    if (entityURI != null) {
                        t.addTabAction(new OpenNodeInBrowserAction(entityURI, element.getName()));
                    }

                    WindowManager.getDefault().findMode("editor").dockInto(t); //NOI18N
                    t.open();
                    t.requestActive();
                }
            });
        }
    }

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
    List<FixedHeightPanel> responsePanels = new ArrayList<>();
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

    public DataViewerTopComponent() {
        initTopComponent();
    }

    private void initTopComponent() {
        initComponents();

        //Don't allow the user to close the data viewer
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);

        setName(Bundle.Main_Data_Viewer_Name());
        setToolTipText(Bundle.HINT_DataViewerTopComponent());
        global = Utilities.actionsGlobalContext().lookupResult(IEntityNode.class);
        global.addLookupListener(listener);
    }

    private void initComponents() {
        contentPanel = new FixedHeightPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
        contentPanel.setBackground(Color.white);
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
        final Collection<? extends IEntityNode> entityNodes = global.allInstances();
        Runnable r = () -> {
            updateEntitySelection(entityNodes, progress);
        };

        if (updateEntitySelection != null && !updateEntitySelection.isDone()) {
            updateEntitySelection.cancel(true);
            LoggerFactory.getLogger(DataViewerTopComponent.class).debug("Cancelled other thread");
        }
        updateEntitySelection = EventQueueUtilities.runOffEDT(r, progress);
    }

    Logger logger = LoggerFactory.getLogger(DataViewerTopComponent.class);
    
    protected List<DataVisualization> updateEntitySelection(Collection<? extends IEntityNode> nodes, ProgressHandle progress) {

        Set<IEntityNode> entityNodes = new TreeSet<>((IEntityNode o1, IEntityNode o2) -> o1.getEntity().getURI().compareTo(o2.getEntity().getURI()));

        for (IEntityNode n : nodes) {
            entityNodes.add(n);
        }
        
        if (progress != null) {
            progress.switchToDeterminate(entityNodes.size());
        }
        int progressWorkUnit = 0;

        List<Content> contentElements = Lists.newLinkedList();
        List<IEntityNode> containers = Lists.newLinkedList();

        for (IEntityNode n : entityNodes) {

            IEntityWrapper ew = n.getEntityWrapper();
            
            if (Content.class.isAssignableFrom(ew.getType())) {
                contentElements.add((Content)ew.getEntity());
            } else {
                containers.add(n);
            }

            if (progress != null) {
                progress.progress(progressWorkUnit++);
            }
        }

        List<DataVisualization> dataVisualizations = Lists.newLinkedList();
        List<ContainerVisualization> containerVisualizations = Lists.newLinkedList();

        
        for (Content rw : contentElements) {
            boolean added = false;
            for (DataVisualization group : dataVisualizations) {
                if (group.shouldAdd(rw)) {
                    group.add(rw);
                    added = true;
                    break;
                }
            }
            if (!added) {
                dataVisualizations.add(ResponseWrapperFactory.create(rw).createVisualization(rw));
            }
        }


        for (IEntityNode n : containers) {

            containerVisualizations.add(ResponseWrapperFactory.create(n).createVisualization(n));
        }

        if (progress != null) {
            progress.switchToIndeterminate();
        }



        List<Component> visualizationComponents = Lists.newLinkedList();
        for (DataVisualization v : dataVisualizations) {
            final JComponent vizPanel = v.generatePanel();
            JComponent visualizationChrome
                    = new ResourceVisualizationChrome(vizPanel, v);

            visualizationComponents.add(visualizationChrome);
        }

        for (ContainerVisualization v : containerVisualizations) {
            JScrollPane sp = new JScrollPane(v.generatePanel());
            visualizationComponents.add(sp);
        }

        EventQueueUtilities.runOnEDT(updateVisualizationComponents(visualizationComponents));

        return dataVisualizations;
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

    private Runnable updateVisualizationComponents(final List<? extends Component> vizComponents) {
        final int height = contentPanel.getParent().getHeight();
        return () -> {
            while (!responsePanels.isEmpty()) {
                Component c = responsePanels.remove(0);
                contentPanel.remove(c);
            }
            
            if (!vizComponents.isEmpty()) {
                //This is for setting each row in the table to a more appropriate height
                int[] rowHeights = new int[vizComponents.size()];//highest allowable height for each row
                int totalStrictHeight = 0;
                int flexiblePanels = 0;
                int minHeight = 150;//min height of a chart
                
                for (Component p : vizComponents) {
                    
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
            
            contentPanel.revalidate();
            contentPanel.repaint();
        };
    }
}
