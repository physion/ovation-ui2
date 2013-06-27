/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.jfree.chart.ChartPanel;

/**
 *
 * @author huecotanks
 */
public class ResponseCellRenderer implements TableCellRenderer{
    int[] heights;
    JTable table;
    public void setTable(JTable t)
    {
        table = t;
    }
    public void setHeights(int[] heights)
    {
        /*for (int i=0; i< heights.length; i++)
        {
            System.out.println("Old row height "+ table.getRowHeight());
            if (table.getRowHeight(i)  != heights[i])
            {
                table.setRowHeight(i, heights[i]);
            }
            //table.setRowHeight(row, height);
            System.out.println("New row height: " + heights[i]);
        }*/
        this.heights = heights;
    }
     public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component panel = (Component) value;
        
        if (table.getRowCount() != 0 && table.getColumnCount() != 0);
        {
            int height = 10;
            if (heights == null || heights.length <= row)
                height = table.getHeight() / table.getRowCount();
            else{
                height = heights[row];
            }
            //System.out.println("Old row height "+ table.getRowHeight());
            if (table.getRowHeight(row)  != height)
            {
                table.setRowHeight(row, height);
            }
            //table.setRowHeight(row, height);
            //System.out.println("New row height: " + height);
            panel.setSize(new Dimension(table.getWidth() / table.getColumnCount(), table.getRowHeight(row)));
        }
        return panel;
    }
}
