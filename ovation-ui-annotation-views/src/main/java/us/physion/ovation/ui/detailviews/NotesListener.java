/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author huecotanks
 */
class NotesListener implements TableModelListener {

    public NotesListener() {
    }

    @Override
    public void tableChanged(TableModelEvent tme) {
        NotesTableModel model = (NotesTableModel)tme.getSource();
        if (tme.getType() == TableModelEvent.UPDATE)
        {
            model.writeToDatabase(tme.getFirstRow(), tme.getLastRow());
        }
    }
    
}
