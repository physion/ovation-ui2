/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author huecotanks
 */
public interface EditableTableModelListener extends TableModelListener{
    public void deleteRows(DefaultTableModel model, int[] rowsToRemove);
}
