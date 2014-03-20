/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.browser.EntityComparator;
import us.physion.ovation.ui.browser.EntityWrapper;
import us.physion.ovation.ui.browser.FilteredEntityChildren;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.ParameterTableModel;
import us.physion.ovation.util.PlatformUtils;

/**
 *
 * @author jackie
 */
public class NamedSourceSelector extends javax.swing.JPanel implements Lookup.Provider, ExplorerManager.Provider {

    final ChangeSupport cs;
    private DataContext context;

    ParameterTableModel tableModel;
    Lookup entitiesLookup;
    ExplorerManager em;
    
    private final Set<IEntityWrapper> selectedEntities;
    private final Set<Integer> selectedTableRows;

    public NamedSourceSelector(ChangeSupport cs, Map<String, Source> defaults, String labelText) {
        this(cs, null, Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext());
        addSources(defaults);
        jLabel1.setText(labelText);
        

        if (PlatformUtils.isMac()) {
            addButton.putClientProperty("JButton.buttonType", "gradient");
            addButton.setPreferredSize(new Dimension(34, 34));

            removeButton.putClientProperty("JButton.buttonType", "gradient");
            removeButton.setPreferredSize(new Dimension(34, 34));
            invalidate();
        }
    }

    public void finish() {
        if (jTable1.getCellEditor() != null) {
            jTable1.getCellEditor().stopCellEditing();
        }
    }

    public void addSources(Map<String, Source> sources) {
        if (sources == null) {
            return;
        }
        Map<String, Source> existingSources = getNamedSources();
        for (String name : sources.keySet()) {
            Source s = sources.get(name);
            if (existingSources.containsValue(s)) {
                if (!existingSources.containsKey(name) || !existingSources.get(name).equals(s)) {
                    //replace the existing source's name with the corresponding name in the 'sources' map
                    int row = -1;
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if (s.equals(tableModel.getValueAt(i, 1))) {
                            row = i;
                            break;
                        }
                    }
                    tableModel.setValueAt(name, row, 0);
                }
            } else {
                tableModel.addParameter(name, sources.get(name));
            }
        }
    }

    /**
     * Creates new form NamedSourceSelector
     */
    public NamedSourceSelector(ChangeSupport cs, Map<String, Source> defaultSources, DataContext c) {
        
        selectedEntities = Sets.newHashSet();
        selectedTableRows = Sets.newHashSet();
        
        this.cs = cs;
        this.context = c;
        tableModel = new ParameterTableModel(false);//doesnt have the extra row for ui editing
        tableModel.setColumnNames(new String[]{"Name", "Source"});
        tableModel.setEditableFunction(new Function<Point, Boolean>() {

            @Override
            public Boolean apply(Point input) {
                return input.x == 0; //only the first column is editable
            }
        });

        initComponents();
        
        
        addButton.setEnabled(false);
        removeButton.setEnabled(false);

        jTable1.setDefaultRenderer(Object.class, new TableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable jtable, Object o, boolean bln, boolean bln1, int i, int i1) {
                if (o instanceof Source) {
                    Source s = (Source) o;
                    return new JLabel(s.getLabel() + " | " + s.getIdentifier());
                } else {
                    return new JLabel(o.toString());
                }
            }
        });
        jSplitPane1.setDividerLocation(300);

        em = new ExplorerManager();
        entitiesLookup = ExplorerUtils.createLookup(em, getActionMap());
        BeanTreeView sourcesTree = new BeanTreeView();
        sourcesTree.setRootVisible(false);
        sourcesScrollPane.setViewportView(sourcesTree);

        resetSources();

        if (defaultSources != null) {
            for (String sourceName : defaultSources.keySet()) {
                tableModel.addParameter(sourceName, defaultSources.get(sourceName));
            }
        }

        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                addSelection();
            }
        });

        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                removeSelection();
            }
        });
        
        final Lookup.Result<IEntityWrapper> selectionResult
                = entitiesLookup.lookupResult(IEntityWrapper.class);
        selectionResult.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent le) {
                selectedEntities.clear();
                Collection<? extends IEntityWrapper> selection = selectionResult.allInstances();
                if (!selection.isEmpty()) {
                    addButton.setEnabled(true);
                    selectedEntities.addAll(selection);
                }

                addButton.setEnabled(selectedEntities.size() > 0);
            }
        });
        
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if(e.getValueIsAdjusting()) {
                            return;
                        }
                        
                        ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                        selectedTableRows.clear();
                        if (!lsm.isSelectionEmpty()) {

                            // Find out which indexes are selected.
                            int minIndex = lsm.getMinSelectionIndex();
                            int maxIndex = lsm.getMaxSelectionIndex();
                            for (int i = minIndex; i <= maxIndex; i++) {
                                if (lsm.isSelectedIndex(i)) {
                                    selectedTableRows.add(i);
                                }
                            }
                        }
                        
                        removeButton.setEnabled(selectedTableRows.size() > 0);

                    }
                });
        
    }

    public void addSelection() {
        for (IEntityWrapper e : getSelectedEntities()) {
            Source s = e.getEntity(Source.class);
            if (s != null) {
                if (getNamedSources().containsValue(s)) {
                    return;
                }
                tableModel.addParameter(s.getLabel() + " | " + s.getIdentifier(), s);
                NamedSourceSelector.this.cs.fireChange();
            }
        }
    }

    protected Set<IEntityWrapper> getSelectedEntities() {
        return selectedEntities;
    }

    protected Set<Integer> getSelectedTableRows() {
        return selectedTableRows;
    }

    public void removeSelection() {
        List<Integer> selection = Lists.newArrayList(getSelectedTableRows());
        Collections.sort(selection, Collections.reverseOrder());
        for (int i : selection) {
            tableModel.remove(i);
            NamedSourceSelector.this.cs.fireChange();
        }
    }

    private Source getSource() {
        IEntityWrapper ew = entitiesLookup.lookup(IEntityWrapper.class);
        if (ew == null) {
            return null;
        }

        if (Source.class.isAssignableFrom(ew.getType())) {
            return (Source) ew.getEntity();
        }
        return null;
    }

    public Map<String, Source> getNamedSources() {
        Map<String, Source> sourceMap = new HashMap();
        Map<String, Object> tableModelParams = tableModel.getParams();
        for (String key : tableModelParams.keySet()) {
            sourceMap.put(key, (Source) tableModelParams.get(key));
        }
        return sourceMap;
    }

    @Override
    public String getName() {
        return "Select Source(s)";
    }

    private void resetSources() {
        List<EntityWrapper> topLevelSources = FilteredEntityChildren.wrap(
                context.getTopLevelSources());

        Collections.sort(topLevelSources, new EntityComparator());

        em.setRootContext(
                new AbstractNode(
                        new FilteredEntityChildren(topLevelSources,
                                Sets.<Class>newHashSet(Source.class))));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        sourceViewPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        sourcesScrollPane = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        removeButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        addButton.setText(org.openide.util.NbBundle.getMessage(NamedSourceSelector.class, "NamedSourceSelector.addButton.text_1")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout sourceViewPanelLayout = new org.jdesktop.layout.GroupLayout(sourceViewPanel);
        sourceViewPanel.setLayout(sourceViewPanelLayout);
        sourceViewPanelLayout.setHorizontalGroup(
            sourceViewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        sourceViewPanelLayout.setVerticalGroup(
            sourceViewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(sourceViewPanel);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jSplitPane1.setRightComponent(jScrollPane1);
        jSplitPane1.setLeftComponent(sourcesScrollPane);

        table.setModel(tableModel);
        jScrollPane2.setViewportView(table);

        jSplitPane1.setRightComponent(jScrollPane2);

        removeButton.setText(org.openide.util.NbBundle.getMessage(NamedSourceSelector.class, "NamedSourceSelector.removeButton.text_1")); // NOI18N
        removeButton.setMaximumSize(new java.awt.Dimension(41, 23));
        removeButton.setMinimumSize(new java.awt.Dimension(41, 23));

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NamedSourceSelector.class, "NamedSourceSelector.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(30, 30, 30))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel sourceViewPanel;
    private javax.swing.JScrollPane sourcesScrollPane;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables

    @Override
    public Lookup getLookup() {
        return entitiesLookup;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
}
