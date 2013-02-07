/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui;

import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author jackie
 */
public class CellEditor extends DefaultCellEditor{
    public CellEditor(JTextField f)
    {
        super(f);
    }
    @Override
    public boolean isCellEditable(EventObject o)
    {
        if (o instanceof MouseEvent) {
            int clickCount;
            clickCount = 2;
            return ((MouseEvent) o).getClickCount() >= clickCount;
        }
        return true;
    }
}
