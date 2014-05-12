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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityNode;
import us.physion.ovation.ui.interfaces.ParameterTableModel;
import us.physion.ovation.ui.interfaces.TreeViewProvider;
import us.physion.ovation.util.PlatformUtils;

/**
 *
 * @author barry
 */
@NbBundle.Messages({
    "AnalysisRecord_No_protocol=(No protocol)",
    "AnalysisRecord_Adding_Outputs=Adding outputs..."
})
public class AnalysisRecordVisualizationPanel extends AbstractContainerVisualizationPanel {

    FileDrop dropListener;

    /**
     * Creates new form AnalysisRecordVisualizationPanel
     */
    public AnalysisRecordVisualizationPanel(IEntityNode analysisRecordNode) {
        super(analysisRecordNode);
        initComponents();
        initUI();
    }

    private void initUI() {
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
                            //getAnalysisRecord().removeProtocolParameter(k);
                        }
                        break;
                    case TableModelEvent.INSERT:
                        for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                            String key = (String) paramsModel.getValueAt(r, 0);
                            Object value = paramsModel.getValueAt(r, 1);
                            //getAnalysisRecord().addProtocolParameter(key, value);
                        }
                        break;
                    case TableModelEvent.UPDATE:
                        for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                            String key = (String) paramsModel.getValueAt(r, 0);
                            if (key != null && !key.isEmpty()) {
                                Object value = paramsModel.getValueAt(r, 1);
                                //getAnalysisRecord().addProtocolParameter(key, value);
                            }
                        }
                        break;
                }
            }

        });

        addInputButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addOutputs();
            }

        });

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
                        addOutputs(files);
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
            addInputButton.setPreferredSize(new Dimension(34, 34));

            removeInputButton.putClientProperty("JButton.buttonType", "gradient");
            removeInputButton.setPreferredSize(new Dimension(34, 34));
            invalidate();
        }
    }

    private void addOutputs(File[] files) {
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

    private static Area createShape() {
        Area shape = new Area(new RoundRectangle2D.Double(0, 20, 500, 200, 20, 20));

        GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gp.moveTo(230, 20);
        gp.lineTo(250, 0);
        gp.lineTo(270, 20);
        gp.closePath();
        shape.add(new Area(gp));

        return shape;
    }

    private void addOutputs() {

        final Area shape = createShape();

        JPanel glassPane = new JPanel(null) {
            @Override
            public boolean contains(int x, int y) {
                // This is to avoid cursor and mouse-events troubles
                return shape.contains(x, y);
            }
        };
        glassPane.setOpaque(false);

        glassPane.setVisible(true);

        final JComponent popup = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setPaint(Color.BLACK);
                g2d.fill(shape);
            }
        };

        this.add(popup, JLayeredPane.POPUP_LAYER);
        popup.setBounds(shape.getBounds());
        popup.setVisible(true);

        EventQueueUtilities.runOffEDT(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                EventQueueUtilities.runOnEDT(new Runnable() {

                    @Override
                    public void run() {
                        popup.setVisible(false);
                    }
                });
            }
        });
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

        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        inputsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        inputsList = new javax.swing.JList();
        addInputButton = new javax.swing.JButton();
        removeInputButton = new javax.swing.JButton();
        protocolPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        protocolComboBox = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        parametersTable = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.jButton1.text")); // NOI18N

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

        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.background")));

        inputsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(inputsList);

        addInputButton.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addInputButton, org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.addInputButton.text")); // NOI18N
        addInputButton.setSize(new java.awt.Dimension(29, 29));

        removeInputButton.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(removeInputButton, org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.removeInputButton.text")); // NOI18N
        removeInputButton.setSize(new java.awt.Dimension(29, 29));

        javax.swing.GroupLayout inputsPanelLayout = new javax.swing.GroupLayout(inputsPanel);
        inputsPanel.setLayout(inputsPanelLayout);
        inputsPanelLayout.setHorizontalGroup(
            inputsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inputsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inputsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(removeInputButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addInputButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        protocolPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        protocolPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.protocolPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AnalysisRecordVisualizationPanel.class, "AnalysisRecordVisualizationPanel.jLabel2.text")); // NOI18N

        protocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${protocols}");
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
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField nameField;
    private javax.swing.JTable parametersTable;
    private javax.swing.JComboBox protocolComboBox;
    private javax.swing.JPanel protocolPanel;
    private javax.swing.JButton removeInputButton;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
