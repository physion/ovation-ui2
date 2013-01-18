/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import ovation.IAuthenticatedDataStoreCoordinator;
import us.physion.ovation.ui.ScrollableTableTree.TableInTreeCellRenderer;

/**
 *
 * @author huecotanks
 */
public class TreeNodePanelFactory {
    
    public static JPanel getPanel(ScrollableTableTree t, TableNode node) {
        TableTreeKey k = (TableTreeKey) node.getUserObject();
        String id = k.getID();
        //TODO move or get rid of table lookup?
        TableInTreeCellRenderer cr = t.getCellRenderer();
        TablePanel panel;
        if (cr.tableLookup.containsKey(id)) {
            panel = cr.tableLookup.get(id);
        } else {
            JTable table = new ZebraTable() {

                public Dimension getPreferredSize() {
                    //gets the width of the TreeCellRenderer, which is set in the BasicTreeUI classs
                    //the BasicTreeUI class is affected by the 'nodeStructureChanged' event, fired on window resize
                    return new Dimension(getWidth(), getRowCount() * getRowHeight());
                }

                public Dimension getPreferredScrollableViewportSize() {
                    return getPreferredSize();
                }
            };
            table.setGridColor(new Color(211, 211, 211, 180));
            table.setBorder(new CompoundBorder(new EmptyBorder(new Insets(1, 4, 1, 4)), table.getBorder()));
            //table.setBorder(BorderFactory.createEmptyBorder());
            table.getTableHeader().setBackground(Color.WHITE);

            //table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            //table.setRowSorter(new TableRowSorter());
            table.getTableHeader().setVisible(false);
            table.getTableHeader().setPreferredSize(new Dimension(-1, 0));
            if (k.isEditable()) {
                panel = new EditableTable(table, t);
            } else {
                panel = new NonEditableTable(table, t);
            }

            cr.tableLookup.put(id, panel);
        }
        node.setPanel(panel);
        TableModel tableModel = k.createTableModel();

        JTable table = panel.getTable();
        table.setModel(tableModel);

        TableModelListener l = k.createTableModelListener(t, node);
        if (l != null)
            tableModel.addTableModelListener(l);

        return panel.getPanel();
    }
}
