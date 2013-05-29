/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import us.physion.ovation.ui.TableTreeKey;
import us.physion.ovation.ui.ScrollableTableTree;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Cancellable;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.AnnotatableEntity;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.User;
import us.physion.ovation.ui.browser.BrowserUtilities;
import us.physion.ovation.ui.browser.EntityWrapper;
import us.physion.ovation.ui.browser.ResetQueryAction;
import us.physion.ovation.ui.*;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.ExpressionTreeProvider;

/**
 *
 * @author huecotanks
 */
public class SourceSelector extends javax.swing.JPanel {

    @Override
    public String getName() {
        return "Select a Source";
    }

    
    ChangeSupport cs;
    private DataStoreCoordinator dsc;
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
        root.add(new DefaultMutableTreeNode("<None>"));
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

    private IEntityWrapper selected;
    /**
     * Creates new form SourceSelector
     */
    public SourceSelector(ChangeSupport changeSupport, IEntityWrapper source) {
        this(changeSupport, source, Lookup.getDefault().lookup(ConnectionProvider.class).getConnection());
    }
    public SourceSelector(ChangeSupport changeSupport, IEntityWrapper source, DataStoreCoordinator dsc) {
        initComponents();
        this.cs = changeSupport;
        this.dsc = dsc;
        //TODO: find the relative paths
        resetButton.setIcon(new ImageIcon("/Users/huecotanks/Ovation/ui/ovation-ui/Browser/src/us/physion/ovation/ui/browser/reset-query24.png"));
        runQueryButton.setIcon(new ImageIcon("/Users/huecotanks/Ovation/ui/ovation-ui/QueryTools/src/us/physion/ovation/query/query24.png"));
            
        //save Browser regisetered ems
        
        //init Browser = ExplorerUtils.createLookup(em, getActionMap());
        //sourcesTree.setUI(new PropertiesTreeUI(this));
        //addComponentListener(new RepaintOnResize(tree));
        sourcesTree.setCellRenderer(new SourcesCellRenderer());
        sourcesTree.setEditable(true);
        sourcesTree.setRootVisible(false);
        sourcesTree.setShowsRootHandles(true);
        sourcesTree.setEditable(false);
        
        setSource(source);
        sourcesTree.addTreeSelectionListener(new TreeSelectionListener() {

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
        });
        resetSources();
        
    }

    public void setSource(IEntityWrapper w)//this should be an IEntityWrapper containing a source
    {
        if (w == null || Source.class.isAssignableFrom(w.getType()))
        {
            if (selected != null)
            {
                selected = null;
                cs.fireChange();
                EventQueueUtilities.runOffEDT(new Runnable(){
                    public void run()
                    {
                        ((ScrollableTableTree)propertiesPane).setKeys(new ArrayList<TableTreeKey>());
                    }
                });
            }
        }
        else {
            selected = w;
            cs.fireChange();
            EventQueueUtilities.runOffEDT(new Runnable() {

                public void run() {
                    ArrayList<TableTreeKey> keys = new ArrayList<TableTreeKey>();
                    for (User u :dsc.getContext().getUsers())
                    {
                        if (!((AnnotatableEntity)selected.getEntity()).getUserProperties(u).isEmpty())
                        {
                            keys.add(new PerUserPropertySet(u, selected));
                        }
                    }
                    ((ScrollableTableTree) propertiesPane).setKeys(keys);
                }   
            });
        }
        
    }
    
    public IEntityWrapper getSource()
    {
        return selected;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        runQueryButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourcesTree = new javax.swing.JTree();
        propertiesPane = new us.physion.ovation.ui.ScrollableTableTree();

        runQueryButton.setText(org.openide.util.NbBundle.getMessage(SourceSelector.class, "SourceSelector.runQueryButton.text")); // NOI18N
        runQueryButton.setBorderPainted(false);
        runQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runQueryButtonActionPerformed(evt);
            }
        });

        resetButton.setText(org.openide.util.NbBundle.getMessage(SourceSelector.class, "SourceSelector.resetButton.text")); // NOI18N
        resetButton.setBorderPainted(false);
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        jSplitPane1.setDividerLocation(235);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(500, 300));

        jScrollPane1.setBorder(null);

        sourcesTree.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 255)));
        sourcesTree.setOpaque(false);
        sourcesTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                sourcesTreeMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(sourcesTree);

        jSplitPane1.setLeftComponent(jScrollPane1);

        propertiesPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 255)));
        jSplitPane1.setRightComponent(propertiesPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(runQueryButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(resetButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 33, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(resetButton)
                    .add(runQueryButton))
                .add(18, 18, 18)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        resetSources();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void runQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runQueryButtonActionPerformed
        /*final ExpressionTreeProvider etp = Lookup.getDefault().lookup(ExpressionTreeProvider.class);
        ExpressionTree et = etp.getExpressionTree();
        
        final ExpressionTree result = ExpressionBuilder.editExpression(et).expressionTree;
        //run query, and reset
        EventQueueUtilities.runOffEDT(new Runnable(){
            @Override
            public void run() {
                 Iterator itr = dsc.getContext().query(result);
                 DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sources");
                Map<String, DefaultMutableTreeNode> sources = new HashMap<String, DefaultMutableTreeNode>(); 
                
                while(itr.hasNext())
                 {
                     Object n = itr.next();
                     if (n instanceof Source) {
                         Source child = (Source) n;
                         Source parent;
                         DefaultMutableTreeNode parentNode = null;
                         DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new EntityWrapper(child));
                         if (sources.containsKey(child.getURIString())) {
                             continue;
                         }
                         
                         while ( (parent = child.getParent()) != null)
                         {
                             if (sources.containsKey(parent.getURIString())) 
                             {
                                 parentNode = sources.get(parent.getURIString());
                                 break;
                             }else{
                                 sources.put(child.getURIString(), childNode);
                                 child = child.getParent();
                                 DefaultMutableTreeNode cn = new DefaultMutableTreeNode(new EntityWrapper(child));
                                 cn.add(childNode);
                                 childNode = cn;
                             }
                         }
                         if (parentNode == null){
                             parentNode = root;
                         }
                         parentNode.add(childNode);
                     }
                 }
                root.add(new DefaultMutableTreeNode("<None>"));
                ((DefaultTreeModel)sourcesTree.getModel()).setRoot(root);
            }
        });*/
    }//GEN-LAST:event_runQueryButtonActionPerformed

    private void sourcesTreeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourcesTreeMouseReleased
        /* TODO: unselect the tree
         * TreePath p = sourcesTree.getPathForLocation(evt.getX(), evt.getY());
        if (p == null || evt.getID() == MouseEvent.BUTTON2)
        {
            selected = null;
            cs.fireChange();
        }*/ 
    }//GEN-LAST:event_sourcesTreeMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JScrollPane propertiesPane;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton runQueryButton;
    private javax.swing.JTree sourcesTree;
    // End of variables declaration//GEN-END:variables

}
