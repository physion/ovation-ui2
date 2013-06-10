/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 *
 * @author huecotanks
 */
class RepaintOnResize implements ComponentListener {
    JTree tree;
    RepaintOnResize(JTree c)
    {
        super();
        tree = c;
    }

    @Override
    public void componentResized(ComponentEvent ce) {
        DefaultMutableTreeNode node = ((DefaultMutableTreeNode)tree.getModel().getRoot());
        while((node = node.getNextNode())!= null)
        {
            ((DefaultTreeModel)tree.getModel()).nodeStructureChanged(node);
        }
    }

    @Override
    public void componentMoved(ComponentEvent ce) {}

    @Override
    public void componentShown(ComponentEvent ce) {}

    @Override
    public void componentHidden(ComponentEvent ce) {}
    
}
