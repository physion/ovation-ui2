/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

/**
 *
 * @author huecotanks
 */
public class ExpandableJTree extends JTree implements ResizableTree{
    
    public ExpandableJTree(DefaultMutableTreeNode root)
    {
        super(root);
    }
    public void expand(DefaultMutableTreeNode node)
    {
        setExpandedState(new TreePath(node.getPath()), true);
    }
    
    public void resizeNode(final TableNode n)
    {
        EventQueueUtilities.runOnEDT(new Runnable(){
            public void run()
            {
                ((DefaultTreeModel)getModel()).nodeChanged(n);
            }
        });
    }
}
