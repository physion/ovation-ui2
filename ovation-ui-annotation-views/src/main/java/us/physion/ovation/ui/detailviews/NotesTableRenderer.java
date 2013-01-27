/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.awt.Component;
import java.awt.Dimension;
import java.util.EventObject;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author jackie
 */
class NotesTableRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

    NotesPanel editor;
    NotesTableModel m;
    int row,column;
    public NotesTableRenderer(NotesTableModel model) {
        m = model;
        editor = new NotesPanel();
        editor.setEditable(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object o, boolean isSelected, boolean hasFocus, int row, int column) {
        if (o instanceof NoteValue)
        {
            NoteValue n = (NoteValue)o;
            NotesPanel p = new NotesPanel(n);
            
            int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
            int prefH = p.getPreferredSize().height;
            p.setSize(new Dimension(cWidth, prefH));
            table.setRowHeight(row, prefH);
            return p;
        }
        return new JLabel(o.toString());
        /*
        if (o instanceof DateTime)
        {
            DateTimeFormatter dtf =  DateTimeFormat.forStyle("SM").withLocale(Locale.getDefault());
            JLabel l = new JLabel(((DateTime) o).toString(dtf));
            l.setVerticalAlignment(JLabel.TOP);
            return l;
        } else {
            JTextArea a = new JTextArea((String) o);
            a.setLineWrap(true);
            a.setWrapStyleWord(true);
            a.setOpaque(true);
*/
            /*if (isSelected) {
                a.setForeground(table.getSelectionForeground());
                a.setBackground(table.getSelectionBackground());
            } else {
                a.setForeground(table.getForeground());
                a.setBackground(table.getBackground());
            }
            a.setFont(table.getFont());
            if (hasFocus) {
                a.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                if (table.isCellEditable(row, column)) {
                    a.setForeground(UIManager.getColor("Table.focusCellForeground"));
                    a.setBackground(UIManager.getColor("Table.focusCellBackground"));
                }
            } else {
                a.setBorder(new EmptyBorder(1, 2, 1, 2));
            }*/
       
  /*          int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
            int prefH = a.getPreferredSize().height;
            a.setSize(new Dimension(cWidth, prefH));
            table.setRowHeight(row, prefH);
            
            return a;
        }
        *
        */
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        ///this will only be used for String values, which correspond to TextAreas here
        if (editor == null)
        {
            editor = new NotesPanel();
            editor.setEditable(true);
            editor.addPropertyChangeListener(new NotesPropertyListener(this));
        }
        if (value != null)
        {
            editor.setNoteValue((NoteValue)value);
            int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
            int prefH = editor.getPreferredSize().height;
            editor.setSize(new Dimension(cWidth, prefH));
            table.setRowHeight(row, prefH);
        }
        this.row = row;
        this.column = column;
        return editor;
    }

    @Override
    public Object getCellEditorValue() {
        if (editor == null)
            return "";
        
        //return datetime if changed
        
        return editor.text;
    }

    @Override
    public boolean stopCellEditing() {
        boolean b = super.stopCellEditing();
        //if (b)
        //    m.setValueAt(getCellEditorValue(), row, column);
        return b;
    }
}
