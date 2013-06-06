/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import com.google.common.base.Function;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.AnnotatableEntity;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.User;
import us.physion.ovation.ui.ScrollableTableTree;
import us.physion.ovation.ui.TableTreeKey;
import us.physion.ovation.ui.browser.EntityWrapper;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.ParameterTableModel;
/**
 *
 * @author jackie
 */
public class NamedSourceSelector extends javax.swing.JPanel {
    ChangeSupport cs;
    private DataStoreCoordinator dsc;
    private DataContext context;
    
    JTree sourcesTree;
    ParameterTableModel tableModel; 
    
     public NamedSourceSelector(ChangeSupport cs) {
         this(cs, Lookup.getDefault().lookup(ConnectionProvider.class).getConnection());
     }
    
    /**
     * Creates new form NamedSourceSelector
     */
    public NamedSourceSelector(ChangeSupport cs, DataStoreCoordinator dsc) {
        this.cs = cs;
        this.dsc = dsc;
        this.context = dsc.getContext();
        tableModel = new ParameterTableModel(false);//doesnt have the extra row for ui editing
        tableModel.setColumnNames(new String[]{"Name", "Source"});
        tableModel.setEditableFunction(new Function<Point, Boolean>() {

            @Override
            public Boolean apply(Point input) {
                return input.x == 0; //only the first column is editable
            }
        });
        initComponents();
        jSplitPane1.setDividerLocation(300);
        sourcesTree = ((ScrollableTableTree)sourcesScrollPane).getTree();
        sourcesTree.setCellRenderer(new SourcesCellRenderer());
        sourcesTree.setRootVisible(false);
        sourcesTree.setShowsRootHandles(true);
        sourcesTree.setEditable(false);
        
        /*sourcesTree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent tse) {
                TreePath path = tse.getPath();
                DefaultMutableTreeNode n = (DefaultMutableTreeNode)path.getLastPathComponent();
                Object o = n.getUserObject();
                if (o instanceof IEntityWrapper)
                    setSource((IEntityWrapper)o);
                else{
                    setSource(null);
                }
            }
        });*/
        resetSources();
        
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                Source s = getSource();
                if (s != null)
                {
                    tableModel.addParameter(s.getIdentifier(), s);
                }
            }
        });
        
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                int num = table.getSelectedRow();
                tableModel.remove(num);
            }
        });
    }
    
    private Source getSource()
    {
        TreePath path = sourcesTree.getSelectionPath();
        if (path == null)
            return null;
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object o = n.getUserObject();
        if (o instanceof IEntityWrapper) {
            if (Source.class.isAssignableFrom(((IEntityWrapper) o).getType())) {
                return ((Source) ((IEntityWrapper) o).getEntity());
            }
        }
        return null;
    }
    
    public Map<String, Source> getNamedSources()
    {
        Map<String, Source> sourceMap = new HashMap();
        Map<String, Object> tableModelParams = tableModel.getParams();
        for (String key : tableModelParams.keySet())
        {
            sourceMap.put(key, (Source)tableModelParams.get(key));
        }
        return sourceMap;
    }
    
    @Override
    public String getName() {
        return "Select Source(s)";
    }

    private void resetSources() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sources");
        for (Source s : dsc.getContext().getTopLevelSources())
        {
            if (!s.getParentSources().iterator().hasNext())
            {
                root.add(new DefaultMutableTreeNode(new EntityWrapper(s)));
            }
        }
        if (!root.isLeaf()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getFirstChild();
            while ((node = node.getNextNode()) != null) {
                Source s = ((Source) ((IEntityWrapper) node.getUserObject()).getEntity());
                for (Source child : s.getChildrenSources()) {
                    node.add(new DefaultMutableTreeNode(new EntityWrapper(child)));
                }
            }
        }
        //root.add(new DefaultMutableTreeNode("<None>"));
        ((DefaultTreeModel)sourcesTree.getModel()).setRoot(root);

    }

    private static class SourcesCellRenderer implements TreeCellRenderer{

        public SourcesCellRenderer() {
        }

        @Override
        public Component getTreeCellRendererComponent(JTree jtree, 
        Object o, 
        boolean selected, 
        boolean expanded, 
        boolean leaf, 
        int row, 
        boolean hasFocus) {
            JLabel l;
            Object value = ((DefaultMutableTreeNode) o).getUserObject();
            if (value instanceof String)
            {
                l = new JLabel((String)value);
            }else{
                l = new JLabel(((Source)((IEntityWrapper)value).getEntity()).getLabel());
            }
            
            if (selected)
            {
                l.setOpaque(true);
                l.setBackground(Color.BLUE);
                l.setForeground(Color.WHITE);
            }
            return l;
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

        jPanel3 = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        sourceViewPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        sourcesScrollPane = new ScrollableTableTree();
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

        addButton.setText(org.openide.util.NbBundle.getMessage(NamedSourceSelector.class, "NamedSourceSelector.addButton.text")); // NOI18N

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

        removeButton.setText(org.openide.util.NbBundle.getMessage(NamedSourceSelector.class, "NamedSourceSelector.removeButton.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NamedSourceSelector.class, "NamedSourceSelector.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, Short.MAX_VALUE))
            .add(jSplitPane1)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
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
}
