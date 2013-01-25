/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.awt.Component;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author jackie
 */
class NotesTableRenderer implements TableCellRenderer {

    public NotesTableRenderer() {
    }

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object o, boolean bln, boolean bln1, int row, int column) {
        if (o instanceof DateTime)
        {
            DateTimeFormatter dtf =  DateTimeFormat.forStyle("SS").withLocale(Locale.getDefault());
            return new JLabel(((DateTime)o).toString(dtf));
        }else{
            return new JTextArea((String)o);
        }
    }
    
}
