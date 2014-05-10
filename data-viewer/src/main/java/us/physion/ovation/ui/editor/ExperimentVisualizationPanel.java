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
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.joda.time.DateTime;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Protocol;
import static us.physion.ovation.ui.editor.DatePickers.zonedDate;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityNode;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.ParameterTableModel;
import us.physion.ovation.ui.interfaces.TreeViewProvider;

/**
 * Experiment visualization panel
 *
 * @author barry
 */
@Messages({
    "Adding_measurements=Adding measurements…",
    "No_protocol=(No protocol)"
})
public class ExperimentVisualizationPanel extends javax.swing.JPanel {

    public static final String PROP_PROTOCOLS = "protocols";
    public static final String PROP_PROTOCOL = "protocol";

    final Experiment experiment;
    final IEntityNode node;
    final IEntityWrapper entityWrapper;
    FileDrop dropPanelListener;

    private final DataContext context;
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Creates new form ExperimentVisualizationPanel
     */
    public ExperimentVisualizationPanel(IEntityNode expNode) {
        node = expNode;
        experiment = expNode.getEntity(Experiment.class);
        entityWrapper = expNode.getEntityWrapper();

        context = experiment.getDataContext();

        initComponents();

        initUI();

    }

    public List<Protocol> getProtocols() {
        List<Protocol> result = Lists.newLinkedList(context.getProtocols());
//        result.sort(new Comparator<Protocol>() {
//
//            @Override
//            public int compare(Protocol o1, Protocol o2) {
//                if (o1 == null || o2 == null) {
//                    return 0;
//                }
//
//                return o1.getName().compareTo(o2.getName());
//            }
//        });

        return result;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    static class ProtocolCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Protocol) {
                Protocol p = (Protocol) value;
                setText(p.getName()); //TODO getVersion()
            }
            return this;
        }
    }

    private void initUI() {

        protocolComboBox.setRenderer(new ProtocolCellRenderer());

        final ParameterTableModel paramsModel = new ParameterTableModel(
                experiment.canWrite(experiment.getDataContext().getAuthenticatedUser()));

        protocolParametersTable.setModel(paramsModel);
        
        paramsModel.setParams(experiment.getProtocolParameters());

        paramsModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
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
            }

        });

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

        dropPanelListener = new FileDrop(this, new FileDrop.Listener() {

            @Override
            public void filesDropped(final File[] files) {

                final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Adding_measurements());

                TopComponent tc = WindowManager.getDefault().findTopComponent(OpenNodeInBrowserAction.PROJECT_BROWSER_ID);
                if (!(tc instanceof ExplorerManager.Provider) || !(tc instanceof TreeViewProvider)) {
                    throw new IllegalStateException();
                }

                TreeView view = (TreeView) ((TreeViewProvider) tc).getTreeView();

                view.expandNode((Node) node);

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

        /*
         AutoCompleteDecorator.decorate(protocolComboBox, new ObjectToStringConverter() {

         @Override
         public String[] getPossibleStringsForItem(Object o) {
         if (!(o instanceof Protocol)) {
         return new String[]{Bundle.No_protocol()};
         }

         return new String[]{((Protocol) o).getName(), ((Protocol) o).getURI().toString()};
         }

         @Override
         public String getPreferredStringForItem(Object item) {
         return item == null ? null : getPossibleStringsForItem(item)[0];
         }

         });
         */
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
        jLabel3 = new javax.swing.JLabel();

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

        setBackground(java.awt.SystemColor.control);
        setBorder(new javax.swing.border.LineBorder(new java.awt.Color(120, 124, 123), 2, true));

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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ExperimentVisualizationPanel.class, "ExperimentVisualizationPanel.jLabel3.text")); // NOI18N

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
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(titleLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(puropseField))
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dateEntryLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JComboBox protocolComboBox;
    private javax.swing.JTable protocolParametersTable;
    private javax.swing.JTextField puropseField;
    private us.physion.ovation.ui.interfaces.DateTimePicker startPicker;
    private javax.swing.JComboBox startZoneComboBox;
    private javax.swing.JLabel titleLabel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
