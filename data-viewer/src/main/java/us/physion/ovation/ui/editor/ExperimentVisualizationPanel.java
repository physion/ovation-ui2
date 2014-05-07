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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import org.joda.time.DateTime;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.Experiment;
import static us.physion.ovation.ui.editor.DatePickers.zonedDate;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityNode;

/**
 * Experiment visualization panel
 *
 * @author barry
 */
@Messages({
    "Adding_measurements=Adding measurements…"
})
public class ExperimentVisualizationPanel extends javax.swing.JPanel {

    final Experiment experiment;
    final IEntityNode node;
    FileDrop dropPanelListener;

    /**
     * Creates new form ExperimentVisualizationPanel
     */
    public ExperimentVisualizationPanel(IEntityNode expNode) {
        node = expNode;
        experiment = expNode.getEntity(Experiment.class);

        initComponents();

        initUI();
    }

    private void initUI() {
        startPicker.setDateTime(experiment.getStart());

        startZoneComboBox.setSelectedItem(experiment.getStart().getZone().getID());

        startPicker.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                startDateTimeChanged();
            }
        });

        startZoneComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                startDateTimeChanged();
            }
        });

        dropPanelListener = new FileDrop(dropPanel, new FileDrop.Listener() {

            @Override
            public void filesDropped(final File[] files) {

                final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Adding_measurements());

                EventQueueUtilities.runOffEDT(new Runnable() {

                    @Override
                    public void run() {
                        insertMeasurements(files);
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

    }

    private void insertMeasurements(File[] files) {
        DateTime start = new DateTime();
        DateTime end = new DateTime();

        for (File f : files) {
            DateTime lastModified = new DateTime(f.lastModified());
            if (lastModified.isBefore(end)) {
                end = lastModified;
            }

            if (start.isBefore(lastModified)) {
                start = lastModified;
            }

        }

        Epoch e = getExperiment().insertEpoch(start,
                end,
                null,
                Maps.<String, Object>newHashMap(),
                Maps.<String, Object>newHashMap());

        for (File f : files) {
            try {
                e.insertMeasurement(f.getName(),
                        Sets.<String>newHashSet(),
                        Sets.<String>newHashSet(),
                        f.toURI().toURL(),
                        ContentTypes.getContentType(f));
            } catch (MalformedURLException ex) {
                Toolkit.getDefaultToolkit().beep();
            } catch (IOException ex) {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    protected void startDateTimeChanged() {
        getExperiment().setStart(zonedDate(startPicker, startZoneComboBox));
    }

    public String getDisplayName() {
        return getExperiment().getStart().toString();
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public IEntityNode getNode() {
        return node;
    }

    public List<String> getAvailableZoneIDs() {
        return Lists.newArrayList(DatePickers.getTimeZoneIDs());
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
        dateEntryLabel = new javax.swing.JLabel();
        startPicker = new us.physion.ovation.ui.interfaces.DateTimePicker();
        startZoneComboBox = new javax.swing.JComboBox();
        dropPanel = new javax.swing.JPanel();
        puropseField = new javax.swing.JTextField();

        setBackground(java.awt.SystemColor.control);
        setBorder(new javax.swing.border.LineBorder(new java.awt.Color(120, 124, 123), 2, true));

        titleLabel.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(titleLabel, org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.titleLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dateEntryLabel, org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.dateEntryLabel.text")); // NOI18N

        startZoneComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${availableZoneIDs}");
        org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, startZoneComboBox);
        bindingGroup.addBinding(jComboBoxBinding);

        dropPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        dropPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.dropPanel.border.title"))); // NOI18N

        javax.swing.GroupLayout dropPanelLayout = new javax.swing.GroupLayout(dropPanel);
        dropPanel.setLayout(dropPanelLayout);
        dropPanelLayout.setHorizontalGroup(
            dropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        dropPanelLayout.setVerticalGroup(
            dropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        puropseField.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        puropseField.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.puropseField.toolTipText")); // NOI18N
        puropseField.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.background")));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${experiment.purpose}"), puropseField, org.jdesktop.beansbinding.BeanProperty.create("text_ON_ACTION_OR_FOCUS_LOST"));
        bindingGroup.addBinding(binding);

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
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dropPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(titleLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(puropseField)))
                        .addContainerGap())))
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
                .addComponent(dropPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dateEntryLabel;
    private javax.swing.JPanel dropPanel;
    private javax.swing.JTextField puropseField;
    private us.physion.ovation.ui.interfaces.DateTimePicker startPicker;
    private javax.swing.JComboBox startZoneComboBox;
    private javax.swing.JLabel titleLabel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
