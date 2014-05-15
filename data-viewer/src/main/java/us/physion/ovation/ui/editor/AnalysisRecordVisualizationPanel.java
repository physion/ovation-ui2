/*
 * Copyright (C) 2014 Physion LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package us.physion.ovation.ui.editor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochContainer;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.domain.mixin.DataElementContainer;
import us.physion.ovation.domain.mixin.EpochGroupContainer;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityNode;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.ParameterTableModel;
import us.physion.ovation.ui.interfaces.TreeViewProvider;
import us.physion.ovation.util.PlatformUtils;

/**
 *
 * @author barry
 */
@NbBundle.Messages({
    "AnalysisRecord_No_protocol=(No protocol)",
    "AnalysisRecord_Adding_Outputs=Adding outputs...",
    "AnalysisRecord_Add_Input=Add..."
})
public class AnalysisRecordVisualizationPanel extends AbstractContainerVisualizationPanel
        implements ExplorerManager.Provider, Lookup.Provider {

    FileDrop dropListener;
    private final ExplorerManager explorerManager;
    private final Lookup treeLookup;
    private List<String> selectedInputNames;

    /**
     * Creates new form AnalysisRecordVisualizationPanel
     */
    public AnalysisRecordVisualizationPanel(IEntityNode analysisRecordNode) {
        super(analysisRecordNode);

        explorerManager = new ExplorerManager();
        treeLookup = ExplorerUtils.createLookup(explorerManager, getActionMap());


        initComponents();
        initUI();
    }

    private void initUI() {

        setEntityBorder(this);

        getNode().getEntityWrapper().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                getPropertyChangeSupport().firePropertyChange(PROP_INPUT_NAMES,
                        null,
                        getInputNames());
            }
        });


        protocolComboBox.setRenderer(new ProtocolCellRenderer());
        final ParameterTableModel paramsModel = new ParameterTableModel(
                getAnalysisRecord().canWrite(getContext().getAuthenticatedUser()));

        parametersTable.setModel(paramsModel);

        paramsModel.setParams(getAnalysisRecord().getProtocolParameters());

        paramsModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                switch (e.getType()) {
                    case TableModelEvent.DELETE:
                        for (String k : paramsModel.getAndClearRemovedKeys()) {
                            getAnalysisRecord().removeProtocolParameter(k);
                        }
                        break;
                    case TableModelEvent.INSERT:
                        for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                            String key = (String) paramsModel.getValueAt(r, 0);
                            Object value = paramsModel.getValueAt(r, 1);
                            getAnalysisRecord().addProtocolParameter(key, value);
                        }
                        break;
                    case TableModelEvent.UPDATE:
                        for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                            String key = (String) paramsModel.getValueAt(r, 0);
                            if (key != null && !key.isEmpty()) {
                                Object value = paramsModel.getValueAt(r, 1);
                                getAnalysisRecord().addProtocolParameter(key, value);
                            }
                        }
                        break;
                }
            }

        });

        addInputButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addInputsFromDialog();
            }

        });

        inputsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                List<String> selection = Lists.newArrayList();
                List<String> elements = getInputNames();

                for (int i : inputsList.getSelectedIndices()) {
                    selection.add(elements.get(i));
                }

                setSelectedInputs(selection);
            }
        });

        removeInputButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Map<String, DataElement> inputs = getAnalysisRecord().getInputs();

                for (String name : getSelectedInputs()) {
                    getAnalysisRecord().removeInput(name);
                }
            }

        });

        removeInputButton.setEnabled(false);

        dropListener = new FileDrop(this, new FileDrop.Listener() {

            @Override
            public void filesDropped(final File[] files) {

                final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.AnalysisRecord_Adding_Outputs());

                TopComponent tc = WindowManager.getDefault().findTopComponent(OpenNodeInBrowserAction.PROJECT_BROWSER_ID);
                if (!(tc instanceof ExplorerManager.Provider) || !(tc instanceof TreeViewProvider)) {
                    throw new IllegalStateException();
                }

                TreeView view = (TreeView) ((TreeViewProvider) tc).getTreeView();

                view.expandNode((Node) node);

                EventQueueUtilities.runOffEDT(new Runnable() {

                    @Override
                    public void run() {
                        addOutputFiles(files);
                        EventQueueUtilities.runOnEDT(new Runnable() {
                            @Override
                            public void run() {
                                node.refresh();
                            }
                        });
                    }
                }, ph);
            }
        });

        if (PlatformUtils.isMac()) {
            addInputButton.putClientProperty("JButton.buttonType", "gradient");
            //addInputButton.setPreferredSize(new Dimension(34, 34));

            removeInputButton.putClientProperty("JButton.buttonType", "gradient");
            //removeInputButton.setPreferredSize(new Dimension(34, 34));
            invalidate();
        }

        addInputButton.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (addInputButton.isShowing()
                        && getAnalysisRecord().getInputs().isEmpty()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            addInputsFromDialog();
                        }
                    });
                }
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                // Component container moved
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // Componnent container removed
            }
        });



    }

    private void addInputsFromDialog() {
        for (DataElement dataElement : showAddInputsDialog()) {
            getAnalysisRecord().addInput(dataElement.getName(), dataElement);
        }
    }

    public static final String PROP_INPUT_NAMES = "inputNames";

    public List<String> getInputNames() {
        List<String> result = Lists.newLinkedList(getAnalysisRecord().getInputs().keySet());

        Collections.sort(result);

        return result;
    }

    public static final String PROP_SELECTED_INPUTS = "selectedInputs";

    public void setSelectedInputs(List<String> elementNames) {
        List<String> current = getSelectedInputs() == null ? null : Lists.newArrayList(getSelectedInputs());
        selectedInputNames = Lists.newArrayList(elementNames);
        getPropertyChangeSupport().firePropertyChange(PROP_SELECTED_INPUTS,
                current,
                getSelectedInputs());

        removeInputButton.setEnabled(getSelectedInputs().size() > 0);
    }

    public List<String> getSelectedInputs() {
        return selectedInputNames;
    }

    private Iterable<DataElement> showAddInputsDialog() {
        Rectangle targetBounds = SwingUtilities.convertRectangle(inputsPanel,
                inputsScrollPane.getBounds(),
                AnalysisRecordVisualizationPanel.this);

        Point screenLoc = new Point(targetBounds.getLocation());
        SwingUtilities.convertPointToScreen(screenLoc,
                AnalysisRecordVisualizationPanel.this);

        targetBounds.setLocation(screenLoc);

        Area targetShape = makePopOverShape(targetBounds);

        SelectDataElementsDialog addDialog = new SelectDataElementsDialog((JFrame) SwingUtilities.getRoot(AnalysisRecordVisualizationPanel.this),
                true,
                targetShape);

        addDialog.setVisible(true);

        List<DataElement> result = Lists.newArrayList();
        if (addDialog.isSuccess()) {
            for (IEntityWrapper entityWrapper : addDialog.getSelectedEntities()) {
                for (DataElement entity : getDataElementsFromEntity(entityWrapper.getEntity())) {
                    result.add(entity);
                }
            }

            System.out.println(Sets.newHashSet(addDialog.getSelectedEntities()));
        }

        addDialog.dispose();

        return result;
    }

    private List<DataElement> getDataElementsFromEntity(OvationEntity e) {

        List<DataElement> result = Lists.newLinkedList();

        if (e instanceof Project) {
            for (Experiment child : ((Project) e).getExperiments()) {
                result.addAll(getDataElementsFromEntity(child));
            }
        }
        if (e instanceof EpochGroupContainer) {
            for (EpochGroup child : ((EpochGroupContainer) e).getEpochGroups()) {
                result.addAll(getDataElementsFromEntity(child));
            }
        }
        if (e instanceof EpochContainer) {
            for (Epoch child : ((EpochContainer) e).getEpochs()) {
                result.addAll(getDataElementsFromEntity(child));
            }
        }
        if (e instanceof DataElementContainer) {
            for (DataElement child : ((DataElementContainer) e).getDataElements().values()) {
                result.addAll(getDataElementsFromEntity(child));
            }
        }
        if (e instanceof DataElement) {
            result.add((DataElement) e);
        }

        return result;
    }

    private void addOutputFiles(File[] files) {
        for (File f : files) {
            String name = f.getName();
            int i = 1;
            while (getAnalysisRecord().getOutputs().keySet().contains(name)) {
                name = name + "_" + i;
                i++;
            }

            try {
                getAnalysisRecord().addOutput(
                        name,
                        f.toURI().toURL(),
                        ContentTypes.getContentType(f));
            } catch (MalformedURLException ex) {
                logger.error("Unable to determine file URL", ex);
                Toolkit.getDefaultToolkit().beep();
            } catch (IOException ex) {
                logger.error("Unable to determine file content type", ex);
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    private static final double TIP_WIDTH = 20;

    private Area makePopOverShape(Rectangle2D targetBounds) {

        Rectangle2D contentBounds = new Rectangle2D.Double(targetBounds.getMaxX() + TIP_WIDTH,
                targetBounds.getY(),
                targetBounds.getWidth(),
                targetBounds.getHeight());

        RoundRectangle2D roundedContentBounds = new RoundRectangle2D.Double(contentBounds.getX(),
                contentBounds.getY() - TIP_WIDTH / 2,
                contentBounds.getWidth() + TIP_WIDTH,
                contentBounds.getHeight() + TIP_WIDTH,
                TIP_WIDTH / 4,
                TIP_WIDTH / 4);

        Area result = new Area(roundedContentBounds);

        Point2D tip = new Point2D.Double(roundedContentBounds.getX() - TIP_WIDTH, roundedContentBounds.getCenterY());

        GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gp.moveTo(tip.getX(), tip.getY());
        gp.lineTo(tip.getX() + TIP_WIDTH, tip.getY() + TIP_WIDTH);
        gp.lineTo(tip.getX() + TIP_WIDTH, tip.getY() - TIP_WIDTH);
        gp.lineTo(tip.getX(), tip.getY());
        gp.closePath();

        result.add(new Area(gp));

        return result;
    }

    private void addInputs(Rectangle2D targetBounds) {

        final Area popoverShape = new Area(makePopOverShape(targetBounds));

        final JPanel popover = new JPanel() {
            @Override
            public boolean contains(int x, int y) {
                // This is to avoid cursor and mouse-events troubles
                return popoverShape.contains(x, y);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setPaint(Color.LIGHT_GRAY); //TODO — lighter

                Rectangle bounds = popoverShape.getBounds();

                AffineTransform tx = g2d.getTransform();
                Composite currentComposite = g2d.getComposite();

                try {
                    float alpha = 0.75f;
                    AlphaComposite composite = makeAlphaComposite(alpha);
                    g2d.setComposite(composite);

                    g2d.transform(AffineTransform.getTranslateInstance(-bounds.getX(), -bounds.getY()));

                    g2d.fill(popoverShape);

                } finally {
                    g2d.setComposite(currentComposite);
                    g2d.setTransform(tx);
                }

            }

            private AlphaComposite makeAlphaComposite(float alpha) {
                AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
                return composite;
            }
        };

        this.add(popover, JLayeredPane.POPUP_LAYER);
        popover.setBounds(popoverShape.getBounds());

        popover.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());

        BeanTreeView entitiesTree = new BeanTreeView();
        entitiesTree.setRootVisible(false);

        JScrollPane treeScrollPane = new JScrollPane(entitiesTree);
        content.add(treeScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton addButton = new JButton(Bundle.AnalysisRecord_Add_Input());
        buttonPanel.add(addButton);

        content.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                popover.setVisible(false);
            }
        });

        content.validate();

        popover.add(content, BorderLayout.CENTER);
        popover.validate();

        popover.setVisible(true);

    }

    public AnalysisRecord getAnalysisRecord() {
        return getNode().getEntity(AnalysisRecord.class);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        jLabel1 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        inputsPanel = new javax.swing.JPanel();
        inputsScrollPane = new javax.swing.JScrollPane();
        inputsList = new javax.swing.JList();
        addInputButton = new javax.swing.JButton();
        removeInputButton = new javax.swing.JButton();
        protocolPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        protocolComboBox = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        parametersTable = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();

        setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.jLabel1.text")); // NOI18N

        nameField.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        nameField.setToolTipText(org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.nameField.toolTipText")); // NOI18N
        nameField.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.background")));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${analysisRecord.name}"), nameField, org.jdesktop.beansbinding.BeanProperty.create("text_ON_ACTION_OR_FOCUS_LOST"));
        bindingGroup.addBinding(binding);

        inputsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        inputsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.inputsPanel.border.title"))); // NOI18N

        inputsScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.background")));

        inputsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${inputNames}");
        org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, inputsList);
        bindingGroup.addBinding(jListBinding);

        inputsScrollPane.setViewportView(inputsList);

        addInputButton.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addInputButton, org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.addInputButton.text")); // NOI18N
        addInputButton.setSize(new java.awt.Dimension(29, 29));

        removeInputButton.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(removeInputButton, org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.removeInputButton.text")); // NOI18N
        removeInputButton.setSize(new java.awt.Dimension(29, 29));

        javax.swing.GroupLayout inputsPanelLayout = new javax.swing.GroupLayout(inputsPanel);
        inputsPanel.setLayout(inputsPanelLayout);
        inputsPanelLayout.setHorizontalGroup(
            inputsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inputsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(inputsScrollPane)
                    .addGroup(inputsPanelLayout.createSequentialGroup()
                        .addComponent(addInputButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeInputButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 129, Short.MAX_VALUE)))
                .addContainerGap())
        );
        inputsPanelLayout.setVerticalGroup(
            inputsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(inputsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inputsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addInputButton)
                    .addComponent(removeInputButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        protocolPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        protocolPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.protocolPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.jLabel2.text")); // NOI18N

        protocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        eLProperty = org.jdesktop.beansbinding.ELProperty.create("${protocols}");
        org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, protocolComboBox);
        bindingGroup.addBinding(jComboBoxBinding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${analysisRecord.protocol}"), protocolComboBox, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        parametersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(parametersTable);

        javax.swing.GroupLayout protocolPanelLayout = new javax.swing.GroupLayout(protocolPanel);
        protocolPanel.setLayout(protocolPanelLayout);
        protocolPanelLayout.setHorizontalGroup(
            protocolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, protocolPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(protocolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(protocolPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(protocolComboBox, 0, 203, Short.MAX_VALUE)))
                .addContainerGap())
        );
        protocolPanelLayout.setVerticalGroup(
            protocolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(protocolPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(protocolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(protocolComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 0, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.jLabel3.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameField))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(inputsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(protocolPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(inputsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(protocolPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addContainerGap())
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addInputButton;
    private javax.swing.JList inputsList;
    private javax.swing.JPanel inputsPanel;
    private javax.swing.JScrollPane inputsScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField nameField;
    private javax.swing.JTable parametersTable;
    private javax.swing.JComboBox protocolComboBox;
    private javax.swing.JPanel protocolPanel;
    private javax.swing.JButton removeInputButton;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public Lookup getLookup() {
        return treeLookup;
    }

}
