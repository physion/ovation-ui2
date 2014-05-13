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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.joda.time.DateTime;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.ui.interfaces.IEntityNode;
import us.physion.ovation.ui.interfaces.ParameterTableModel;

/**
 *
 * @author barry
 */
public final class EpochGroupVisualizationPanel extends AbstractContainerVisualizationPanel {

    /**
     * Creates new form EpochGroupVisualizationPanel
     */
    public EpochGroupVisualizationPanel(IEntityNode groupNode) {

        super(groupNode);

        initComponents();

        initUi();
    }

    private void initUi() {

        Binding binding = Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${group.start}"),
                startPicker,
                org.jdesktop.beansbinding.BeanProperty.create("dateTime"));

        BindingGroup bindingGrp = new org.jdesktop.beansbinding.BindingGroup();
        bindingGrp.addBinding(binding);
        bindingGrp.bind();

        startZoneComboBox.setSelectedItem(getGroup().getStart().getZone().getID());

        startPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if ("date".equals(propertyChangeEvent.getPropertyName())) {
                    startDateTimeChanged();
                }
            }
        });

        startPicker.setDateTime(new DateTime(getGroup().getStart()));

        startZoneComboBox.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                startDateTimeChanged();
            }
        });

        protocolComboBox.setRenderer(new AbstractContainerVisualizationPanel.ProtocolCellRenderer());

        final ParameterTableModel paramsModel = new ParameterTableModel(
                getGroup().canWrite(getContext().getAuthenticatedUser()));

        protocolParametersTable.setModel(paramsModel);

        paramsModel.setParams(getGroup().getProtocolParameters());

        paramsModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                switch (e.getType()) {
                    case TableModelEvent.DELETE:
                        for (String k : paramsModel.getAndClearRemovedKeys()) {
                            getGroup().removeProtocolParameter(k);
                        }
                        break;
                    case TableModelEvent.INSERT:
                        for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                            String key = (String) paramsModel.getValueAt(r, 0);
                            Object value = paramsModel.getValueAt(r, 1);
                            getGroup().addProtocolParameter(key, value);
                        }
                        break;
                    case TableModelEvent.UPDATE:
                        for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                            String key = (String) paramsModel.getValueAt(r, 0);
                            if (key != null && !key.isEmpty()) {
                                Object value = paramsModel.getValueAt(r, 1);
                                getGroup().addProtocolParameter(key, value);
                            }
                        }
                        break;
                }
            }

        });
    }

    protected void startDateTimeChanged() {
        //project.setStart(zonedDate(startPicker, startZoneComboBox));
    }

    public EpochGroup getGroup() {
        return getNode().getEntity(EpochGroup.class);
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

        titleLabel = new javax.swing.JLabel();
        labelTextField = new javax.swing.JTextField();
        dateLabel = new javax.swing.JLabel();
        startPicker = new us.physion.ovation.ui.interfaces.DateTimePicker();
        startZoneComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        protocolComboBox = new javax.swing.JComboBox<Protocol>();
        jScrollPane2 = new javax.swing.JScrollPane();
        protocolParametersTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();

        setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        titleLabel.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(titleLabel, org.openide.util.NbBundle.getMessage(EpochGroupVisualizationPanel.class, "EpochGroupVisualizationPanel.titleLabel.text")); // NOI18N

        labelTextField.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        labelTextField.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.background")));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${group.label}"), labelTextField, org.jdesktop.beansbinding.BeanProperty.create("text_ON_ACTION_OR_FOCUS_LOST"));
        bindingGroup.addBinding(binding);

        org.openide.awt.Mnemonics.setLocalizedText(dateLabel, org.openide.util.NbBundle.getMessage(EpochGroupVisualizationPanel.class, "EpochGroupVisualizationPanel.dateLabel.text")); // NOI18N

        startZoneComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${availableZoneIDs}");
        org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, startZoneComboBox);
        bindingGroup.addBinding(jComboBoxBinding);

        jPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(EpochGroupVisualizationPanel.class, "EpochGroupVisualizationPanel.jPanel1.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EpochGroupVisualizationPanel.class, "EpochGroupVisualizationPanel.jLabel1.text")); // NOI18N

        protocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(EpochGroupVisualizationPanel.class, "EpochGroupVisualizationPanel.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(protocolComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(protocolComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(dateLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(startZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 40, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(titleLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelTextField)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(labelTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dateLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField labelTextField;
    private javax.swing.JComboBox protocolComboBox;
    private javax.swing.JTable protocolParametersTable;
    private us.physion.ovation.ui.interfaces.DateTimePicker startPicker;
    private javax.swing.JComboBox startZoneComboBox;
    private javax.swing.JLabel titleLabel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
