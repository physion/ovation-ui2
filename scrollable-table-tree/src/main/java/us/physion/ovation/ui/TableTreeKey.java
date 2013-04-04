/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import us.physion.ovation.DataStoreCoordinator;

/**
 *
 * @author huecotanks
 */
public interface TableTreeKey extends Comparable
{
    public void refresh(DataStoreCoordinator dsc);//regrab info from the database
    public String getDisplayName();
    public String getID();// for comparison
    public boolean isEditable();
    public boolean isExpandedByDefault();
    public TableModelListener createTableModelListener(ScrollableTableTree t, TableNode n);
    public TableModel createTableModel();
    public Object[][] getData();// for testing and ease of use
}
