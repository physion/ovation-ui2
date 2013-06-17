/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author huecotanks
 */
public class ClickableCellEditor extends AbstractCellEditor implements TableCellEditor{//extends AbstractCellEditor -- see note below
    //extend the AbstractCellEditor, if you need to actually edit the values in the clickableCellEditor
    //it handles the default event firing when the cell is being edited
    //You'll also have delete the methods below getCellEditorValue
    TableCellRenderer r;
    Component current;
    public ClickableCellEditor(TableCellRenderer r)
    {
        this.r = r;
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (table.getRowCount() > row && table.getColumnCount() > column)
            current = r.getTableCellRendererComponent(table, value, isSelected, isSelected, row, column);
        else{
            current = null;
        }
        return current;
    }

    @Override
    public Object getCellEditorValue() {
        return current;
    }
//DELETE ME if you use the AbstractCellEditor -------------------------------------------
    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    @Override
    public boolean stopCellEditing() {
        this.fireEditingStopped();
        current = null;
        return true;
    }

    @Override
    public void cancelCellEditing() {
        this.fireEditingCanceled();
        current = null;
    }
    //End deletions -------------------------------------------
}
