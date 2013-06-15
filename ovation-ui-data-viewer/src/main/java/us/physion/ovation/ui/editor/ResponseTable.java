/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.awt.Rectangle;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;

/**
 *
 * @author huecotanks
 */
public class ResponseTable extends JTable{
    int[] heights;
    public void setHeights(int[] heights)
    {
        this.heights = heights;
    }
    @Override
   public void tableChanged(TableModelEvent e) {
        //if just an update, and not a data or structure changed event or an insert or delete, use the fixed row update handling
        //otherwise call super.tableChanged to let the standard JTable update handling manage it
        if ( e != null &&
            e.getType() == TableModelEvent.UPDATE &&
            e.getFirstRow() != TableModelEvent.HEADER_ROW &&
            e.getLastRow() != Integer.MAX_VALUE) {

            handleRowUpdate(e);
        } else {
            super.tableChanged(e);
        }
        //handleRowUpdate(e);
    }

    /**
     * This borrows most of the logic from the superclass handling of update events, but changes the calculation of the height
     * for the dirty region to provide proper handling for repainting custom height rows
     */
    private void handleRowUpdate(TableModelEvent e) {
        int modelColumn = e.getColumn();
        int start = e.getFirstRow();
        int end = e.getLastRow();

        Rectangle dirtyRegion;
        if (modelColumn == TableModelEvent.ALL_COLUMNS) {
            // 1 or more rows changed
            dirtyRegion = new Rectangle(0, start * getRowHeight(),
                                        getColumnModel().getTotalColumnWidth(), 0);
        }
        else {
            // A cell or column of cells has changed.
            // Unlike the rest of the methods in the JTable, the TableModelEvent
            // uses the coordinate system of the model instead of the view.
            // This is the only place in the JTable where this "reverse mapping"
            // is used.
            int column = convertColumnIndexToView(modelColumn);
            dirtyRegion = getCellRect(start, column, false);
        }

        // Now adjust the height of the dirty region
        dirtyRegion.height = 0;
        for ( int row=start; row <= end; row ++ ) {
            dirtyRegion.height += getRowHeight(row);  //THIS IS CHANGED TO CALCULATE THE DIRTY REGION HEIGHT CORRECTLY
        }
        repaint(dirtyRegion.x, dirtyRegion.y, dirtyRegion.width, dirtyRegion.height);
    }
    
}