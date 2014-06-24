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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.exceptions.OvationException;
import static us.physion.ovation.ui.editor.DatePickers.zonedDate;
import us.physion.ovation.ui.importer.ImageImporter;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityNode;
import us.physion.ovation.ui.interfaces.ParameterTableModel;
import us.physion.ovation.ui.interfaces.TreeViewProvider;

/**
 *
 * @author barry
 */
@Messages({
    "Epoch_Drop_Files_To_Add_Measurements=Drop files to add Measurements"
})
public class EpochVisualizationPanel extends AbstractContainerVisualizationPanel {

    FileDrop dropPanelListener;

    /**
     * Creates new form EpochVisualizationPanel
     */
    public EpochVisualizationPanel(IEntityNode expNode) {
        super(expNode);

        initComponents();

        initUI();

    }

    private void initUI() {

        setEntityBorder(this);

        protocolComboBox.setRenderer(new ProtocolCellRenderer());

        final ParameterTableModel paramsModel = new ParameterTableModel(
                getEpoch().canWrite(getContext().getAuthenticatedUser()));

        protocolParametersTable.setModel(paramsModel);

        paramsModel.setParams(getEpoch().getProtocolParameters());

        paramsModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                switch (e.getType()) {
                    case TableModelEvent.DELETE:
                        for (String k : paramsModel.getAndClearRemovedKeys()) {
                            getEpoch().removeProtocolParameter(k);
                        }
                        break;
                    case TableModelEvent.INSERT:
                        for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                            String key = (String) paramsModel.getValueAt(r, 0);
                            Object value = paramsModel.getValueAt(r, 1);
                            getEpoch().addProtocolParameter(key, value);
                        }
                        break;
                    case TableModelEvent.UPDATE:
                        for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                            String key = (String) paramsModel.getValueAt(r, 0);
                            if (key != null && !key.isEmpty()) {
                                Object value = paramsModel.getValueAt(r, 1);
                                getEpoch().addProtocolParameter(key, value);
                            }
                        }
                        break;
                }
            }

        });

        startPicker.setDateTime(getEpoch().getStart());

        startZoneComboBox.setSelectedItem(getEpoch().getStart().getZone().getID());

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

        if (getEpoch().getEnd() != null) {
            endPicker.setDateTime(getEpoch().getEnd());

            endZoneComboBox.setSelectedItem(getEpoch().getEnd().getZone().getID());
        }

        endPicker.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                endDateTimeChanged();
            }
        });

        endZoneComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                endDateTimeChanged();
            }
        });

        measurementFileWell.setDelegate(new FileWell.AbstractDelegate(Bundle.Epoch_Drop_Files_To_Add_Measurements()) {

            @Override
            public void filesDropped(final File[] files) {
                final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Adding_measurements());

                TopComponent tc = WindowManager.getDefault().findTopComponent(OpenNodeInBrowserAction.PROJECT_BROWSER_ID);
                if (!(tc instanceof ExplorerManager.Provider) || !(tc instanceof TreeViewProvider)) {
                    throw new IllegalStateException();
                }

                final TreeView view = (TreeView) ((TreeViewProvider) tc).getTreeView();

                ListenableFuture<Iterable<Measurement>> addMeasurements = EventQueueUtilities.runOffEDT(new Callable<Iterable<Measurement>>() {

                    @Override
                    public Iterable<Measurement> call() {
                        return insertMeasurements(files);
                    }
                }, ph);

                Futures.addCallback(addMeasurements, new FutureCallback<Iterable<Measurement>>() {

                    @Override
                    public void onSuccess(final Iterable<Measurement> result) {
                        EventQueueUtilities.runOnEDT(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    node.refresh().get();
                                    view.expandNode((Node) node);
                                } catch (InterruptedException ex) {
                                    logger.error("Unable to refresh Epoch node", ex);
                                } catch (ExecutionException ex) {
                                    logger.error("Unable to refresh Epoch node", ex);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        logger.error("Unable to display added Measurements", t);
                    }
                });
            }
        });

        analysisFileWell.setDelegate(new FileWell.AbstractDelegate(Bundle.Project_Drop_Files_To_Add_Analysis()) {

            @Override
            public void filesDropped(final File[] files) {

                Iterable<? extends DataElement> inputElements = getEpoch().getMeasurements();

                final List<DataElement> inputs = Lists.newArrayList(inputElements);

                final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.AnalysisRecord_Adding_Outputs());

                TopComponent tc = WindowManager.getDefault().findTopComponent(OpenNodeInBrowserAction.PROJECT_BROWSER_ID);
                if (!(tc instanceof ExplorerManager.Provider) || !(tc instanceof TreeViewProvider)) {
                    throw new IllegalStateException();
                }

                final TreeView view = (TreeView) ((TreeViewProvider) tc).getTreeView();

                ListenableFuture<AnalysisRecord> addRecord = EventQueueUtilities.runOffEDT(new Callable<AnalysisRecord>() {

                    @Override
                    public AnalysisRecord call() throws Exception {
                        return addAnalysisRecord(files, inputs);
                    }

                });

                Futures.addCallback(addRecord, new FutureCallback<AnalysisRecord>() {

                    @Override
                    public void onSuccess(final AnalysisRecord ar) {
                        EventQueueUtilities.runOnEDT(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    node.refresh().get();
                                    view.expandNode((Node) node);
                                } catch (InterruptedException ex) {
                                    logger.error("Unable to refresh Epoch node", ex);
                                } catch (ExecutionException ex) {
                                    logger.error("Unable to refresh Epoch node", ex);
                                }

                                for (final Node userNode : ((Node) node).getChildren().getNodes()) {
                                    final User user = ((IEntityNode) userNode).getEntity(User.class);
                                    if (user != null && user.equals(ar.getDataContext().getAuthenticatedUser())) {
                                        view.expandNode(userNode);
                                    }

                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        logger.error("Unable to display AnalysisRecord", t);
                    }
                });
            }
        });
    }

    protected void startDateTimeChanged() {
        getEpoch().setStart(zonedDate(startPicker, startZoneComboBox));
    }

    protected void endDateTimeChanged() {
        getEpoch().setEnd(zonedDate(endPicker, endZoneComboBox));
    }

    public Epoch getEpoch() {
        return getNode().getEntity(Epoch.class);
    }

    private Iterable<Measurement> insertMeasurements(File[] files) {
        List<File> images = Lists.newLinkedList(Iterables.filter(Lists.newArrayList(files),
                new Predicate<File>() {

                    @Override
                    public boolean apply(File input) {
                        return ImageImporter.canImport(input);
                    }
                }));

        final Epoch e = getEpoch();

        List<Measurement> result = Lists.newLinkedList(ImageImporter.importImageMeasurements(e, images)
                .toList()
                .toBlockingObservable()
                .lastOrDefault(Lists.<Measurement>newArrayList()));

        Set<File> others = Sets.newHashSet(files);
        others.removeAll(images);
        for (File f : others) {
            try {
                Measurement m = e.insertMeasurement(f.getName(),
                        Sets.<String>newHashSet(),
                        Sets.<String>newHashSet(),
                        f.toURI().toURL(),
                        ContentTypes.getContentType(f));

                result.add(m);
            } catch (MalformedURLException ex) {
                Toolkit.getDefaultToolkit().beep();
            } catch (IOException ex) {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        return result;
    }

    private AnalysisRecord addAnalysisRecord(File[] files, final Iterable<DataElement> inputs) {
        getContext().beginTransaction();
        try {
            AnalysisRecord ar = getEpoch().addAnalysisRecord(Bundle.Project_New_Analysis_Record_Name(),
                    inputs,
                    null,
                    Maps.<String, Object>newHashMap());

            for (File f : files) {
                String name = f.getName();
                int i = 1;
                while (ar.getOutputs().keySet().contains(name)) {
                    name = name + "_" + i;
                    i++;
                }

                try {
                    ar.addOutput(
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

            getContext().markModified(getEpoch());
            getContext().commitTransaction();

            return ar;
        } catch (Throwable t) {
            getContext().abortTransaction();
            throw new OvationException(t);
        }
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
        jLabel2 = new javax.swing.JLabel();
        startPicker = new us.physion.ovation.ui.interfaces.DateTimePicker();
        endPicker = new us.physion.ovation.ui.interfaces.DateTimePicker();
        jLabel3 = new javax.swing.JLabel();
        startZoneComboBox = new javax.swing.JComboBox();
        endZoneComboBox = new javax.swing.JComboBox();
        protocolPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        protocolComboBox = new javax.swing.JComboBox<Protocol>();
        jScrollPane2 = new javax.swing.JScrollPane();
        protocolParametersTable = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        dropPanelContainer = new javax.swing.JPanel();
        measurementFileWell = new us.physion.ovation.ui.editor.FileWell();
        analysisFileWell = new us.physion.ovation.ui.editor.FileWell();

        setBackground(java.awt.Color.white);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EpochVisualizationPanel.class, "EpochVisualizationPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(EpochVisualizationPanel.class, "EpochVisualizationPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(EpochVisualizationPanel.class, "EpochVisualizationPanel.jLabel3.text")); // NOI18N

        startZoneComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${availableZoneIDs}");
        org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, startZoneComboBox);
        bindingGroup.addBinding(jComboBoxBinding);

        endZoneComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        eLProperty = org.jdesktop.beansbinding.ELProperty.create("${availableZoneIDs}");
        jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, endZoneComboBox);
        bindingGroup.addBinding(jComboBoxBinding);

        protocolPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        protocolPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(EpochVisualizationPanel.class, "EpochVisualizationPanel.protocolPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(EpochVisualizationPanel.class, "EpochVisualizationPanel.jLabel4.text")); // NOI18N

        protocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        eLProperty = org.jdesktop.beansbinding.ELProperty.create("${protocols}");
        jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, protocolComboBox);
        bindingGroup.addBinding(jComboBoxBinding);
        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${epoch.protocol}"), protocolComboBox, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(EpochVisualizationPanel.class, "EpochVisualizationPanel.jLabel5.text")); // NOI18N

        javax.swing.GroupLayout protocolPanelLayout = new javax.swing.GroupLayout(protocolPanel);
        protocolPanel.setLayout(protocolPanelLayout);
        protocolPanelLayout.setHorizontalGroup(
            protocolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(protocolPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(protocolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
                    .addGroup(protocolPanelLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(protocolPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(protocolComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        protocolPanelLayout.setVerticalGroup(
            protocolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(protocolPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(protocolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(protocolComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                .addContainerGap())
        );

        dropPanelContainer.setBackground(java.awt.Color.white);
        dropPanelContainer.setLayout(new java.awt.GridLayout());
        dropPanelContainer.add(measurementFileWell);
        dropPanelContainer.add(analysisFileWell);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(protocolPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(startPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(endPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(endZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(startZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(dropPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(startPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(endPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addComponent(protocolPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(dropPanelContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(140, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private us.physion.ovation.ui.editor.FileWell analysisFileWell;
    private javax.swing.JPanel dropPanelContainer;
    private us.physion.ovation.ui.interfaces.DateTimePicker endPicker;
    private javax.swing.JComboBox endZoneComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane2;
    private us.physion.ovation.ui.editor.FileWell measurementFileWell;
    private javax.swing.JComboBox protocolComboBox;
    private javax.swing.JPanel protocolPanel;
    private javax.swing.JTable protocolParametersTable;
    private us.physion.ovation.ui.interfaces.DateTimePicker startPicker;
    private javax.swing.JComboBox startZoneComboBox;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
