/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.tree.DefaultMutableTreeNode;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author huecotanks
 */
public class TableNode extends DefaultMutableTreeNode{
    TablePanel tp;
    public TableNode(TableTreeKey p)
    {
        super(p);
    }
    
    public void reset(DataStoreCoordinator dsc)
    {
        //regrab properties/tags/etc from the database
        TableTreeKey p = (TableTreeKey)getUserObject();
        p.refresh(dsc);
    }
    
    public void setPanel(TablePanel t)
    {
        tp = t;
    }
    
    public TablePanel getPanel()
    {
        return tp;
    }
    
    public int getHeight()
    {
        if (tp == null)
            return -1;
        return tp.getPanel().getPreferredSize().height;
    }
    
    public int getViewportHeight()
    {
        if (tp == null)
            return -1;
        return tp.getTable().getPreferredScrollableViewportSize().height;
    }
}
