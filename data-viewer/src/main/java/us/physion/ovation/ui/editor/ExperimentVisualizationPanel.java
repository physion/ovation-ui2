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

import com.google.common.collect.Maps;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.event.TableModelEvent;
import org.joda.time.DateTime;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.ui.browser.BrowserUtilities;
import static us.physion.ovation.ui.editor.DatePickers.zonedDate;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityNode;
import us.physion.ovation.ui.interfaces.ParameterTableModel;
import us.physion.ovation.ui.reveal.api.RevealNode;

/**
 * Experiment visualization panel
 *
 * @author barry
 */
@Messages({
    "Adding_measurements=Adding measurements…",
    "Experiment_No_protocol=(No protocol)",
    "Experiment_Drop_Files_To_Add_Data=Drop files here to add data",
    "EpochGroup_Default_Name=New Group"
})
public class ExperimentVisualizationPanel extends AbstractContainerVisualizationPanel {

    FileDrop dropPanelListener;

    /**
     * Creates new form ExperimentVisualizationPanel
     */
    public ExperimentVisualizationPanel(IEntityNode expNode) {
        super(expNode);

        initComponents();

        initUI();

    }

    private void initUI() {

        setEntityBorder(this);

        protocolComboBox.setRenderer(new ProtocolCellRenderer());

        addProtocolHyperlink.addActionListener((ActionEvent e) -> {
            Protocol current = getExperiment().getProtocol();
            getExperiment().setProtocol(addProtocol());
            firePropertyChange("experiment.protocol", current, getExperiment().getProtocol());
            protocolComboBox.setSelectedItem(getExperiment().getProtocol());
        });

        editHyperlink.addActionListener(new ActionListenerImpl());

        final ParameterTableModel paramsModel = new ParameterTableModel(
                getExperiment().canWrite(getContext().getAuthenticatedUser()));

        protocolParametersTable.setModel(paramsModel);

        paramsModel.setParams(getExperiment().getProtocolParameters());

        paramsModel.addTableModelListener((TableModelEvent e) -> {
            switch (e.getType()) {
                case TableModelEvent.DELETE:
                    for (String k : paramsModel.getAndClearRemovedKeys()) {
                        getExperiment().removeProtocolParameter(k);
                    }
                    break;
                case TableModelEvent.INSERT:
                    for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                        String key = (String) paramsModel.getValueAt(r, 0);
                        Object value = paramsModel.getValueAt(r, 1);
                        getExperiment().addProtocolParameter(key, value);
                    }
                    break;
                case TableModelEvent.UPDATE:
                    for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                        String key = (String) paramsModel.getValueAt(r, 0);
                        if (key != null && !key.isEmpty()) {
                            Object value = paramsModel.getValueAt(r, 1);
                            getExperiment().addProtocolParameter(key, value);
                        }
                    }
                    break;
            }
        });

        startPicker.setDateTime(getExperiment().getStart());

        startZoneComboBox.setSelectedItem(getExperiment().getStart().getZone().getID());

        startPicker.addActionListener((ActionEvent e) -> {
            startDateTimeChanged();
        });

        startZoneComboBox.addActionListener((ActionEvent e) -> {
            startDateTimeChanged();
        });

        String prompt = Bundle.Experiment_Drop_Files_To_Add_Data();

        fileWell.setDelegate(new FileWell.AbstractDelegate(prompt) {

            @Override
            public void filesDropped(final File[] files) {
                final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Adding_measurements());

                EventQueueUtilities.runOffEDT(() -> {
                    final List<Measurement> m = EntityUtilities.insertMeasurements(getExperiment(), files);
                    EventQueueUtilities.runOnEDT(() -> {
                        if (!m.isEmpty()) {
                            RevealNode.forEntity(BrowserUtilities.PROJECT_BROWSER_ID, m.get(0));
                        }
                    });
                }, ph);
            }
        });

        newEpochGroupHyperlink.addActionListener((ActionEvent e) -> {
            addEpochGroup();
        });
    }

    private void addEpochGroup() {
        final EpochGroup group = getExperiment().insertEpochGroup(Bundle.EpochGroup_Default_Name(),
                new DateTime(),
                null,
                Maps.<String, Object>newHashMap(),
                Maps.<String, Object>newHashMap());

        node.refresh();
        RevealNode.forEntity(BrowserUtilities.PROJECT_BROWSER_ID, group);
    }

    protected void startDateTimeChanged() {
        getExperiment().setStart(zonedDate(startPicker, startZoneComboBox));
    }

    public String getDisplayName() {
        return getExperiment().getStart().toString();
    }

    public Experiment getExperiment() {
        return getNode().getEntity(Experiment.class);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        titleLabel = new javax.swing.JLabel();
        dateEntryLabel = new javax.swing.JLabel();
        startPicker = new us.physion.ovation.ui.interfaces.DateTimePicker();
        startZoneComboBox = new javax.swing.JComboBox();
        puropseField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        protocolComboBox = new javax.swing.JComboBox<Protocol>();
        jScrollPane2 = new javax.swing.JScrollPane();
        protocolParametersTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        editHyperlink = new org.jdesktop.swingx.JXHyperlink();
        addProtocolHyperlink = new org.jdesktop.swingx.JXHyperlink();
        fileWell = new us.physion.ovation.ui.editor.FileWell();
        newEpochGroupHyperlink = new org.jdesktop.swingx.JXHyperlink();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTable1);

        setBackground(java.awt.Color.white);

        titleLabel.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(titleLabel, org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.titleLabel.text")); // NOI18N

        dateEntryLabel.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(dateEntryLabel, org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.dateEntryLabel.text")); // NOI18N

        startZoneComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${availableZoneIDs}");
        org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, startZoneComboBox);
        bindingGroup.addBinding(jComboBoxBinding);

        puropseField.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        puropseField.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.puropseField.toolTipText")); // NOI18N
        puropseField.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.background")));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${experiment.purpose}"), puropseField, org.jdesktop.beansbinding.BeanProperty.create("text_ON_ACTION_OR_FOCUS_LOST"));
        bindingGroup.addBinding(binding);

        jPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.jPanel1.border.title"))); // NOI18N

        jLabel1.setBackground(java.awt.Color.white);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.jLabel1.text")); // NOI18N

        protocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        eLProperty = org.jdesktop.beansbinding.ELProperty.create("${protocols}");
        jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, protocolComboBox);
        bindingGroup.addBinding(jComboBoxBinding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${experiment.protocol}"), protocolComboBox, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        protocolParametersTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(protocolParametersTable);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(editHyperlink, org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.editHyperlink.text")); // NOI18N
        editHyperlink.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.editHyperlink.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addProtocolHyperlink, org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.addProtocolHyperlink.text")); // NOI18N
        addProtocolHyperlink.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.addProtocolHyperlink.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(protocolComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addProtocolHyperlink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(editHyperlink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(protocolComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editHyperlink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addProtocolHyperlink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.openide.awt.Mnemonics.setLocalizedText(newEpochGroupHyperlink, org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.newEpochGroupHyperlink.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dateEntryLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(startPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(startZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(fileWell, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(titleLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(puropseField)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newEpochGroupHyperlink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(puropseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(dateEntryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newEpochGroupHyperlink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(fileWell, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(86, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXHyperlink addProtocolHyperlink;
    private javax.swing.JLabel dateEntryLabel;
    private org.jdesktop.swingx.JXHyperlink editHyperlink;
    private us.physion.ovation.ui.editor.FileWell fileWell;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private org.jdesktop.swingx.JXHyperlink newEpochGroupHyperlink;
    private javax.swing.JComboBox protocolComboBox;
    private javax.swing.JTable protocolParametersTable;
    private javax.swing.JTextField puropseField;
    private us.physion.ovation.ui.interfaces.DateTimePicker startPicker;
    private javax.swing.JComboBox startZoneComboBox;
    private javax.swing.JLabel titleLabel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    private class ActionListenerImpl implements ActionListener {

        public ActionListenerImpl() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RevealNode.forEntity(BrowserUtilities.PROTOCOL_BROWSER_ID, getExperiment().getProtocol());
        }
    }
}
