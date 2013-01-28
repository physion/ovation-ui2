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
class NotesTableRenderer implements TableCellRenderer {

    NotesPanel editor;
    NotesTableModel m;
    int row,column;
    public NotesTableRenderer(NotesTableModel model) {
        m = model;
        editor = new NotesPanel();
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
    }

    /*@Override
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
        return super.stopCellEditing();
    }*/

    void delete() {
        m.remove(row);
    }
}
