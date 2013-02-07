/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui;

import java.util.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author jackie
 */
public class EditableTableModel extends DefaultTableModel{

    JTable table;
    Map<String, Object> params;
    boolean editable;
    Map<Integer, String> oldKeys;
    List<String> keys;
    int columnCount;
    String[] columnHeaders;

    public EditableTableModel() {
        this(true);
    }
    
    public EditableTableModel(boolean editable) {
        this(editable, 2, new String[]{"Key", "Value"});
    }
    
    public EditableTableModel(boolean editable, int numberOfColumns, String[] headers)
    {
        super();
        params = new HashMap<String, Object>();
        keys = new ArrayList<String>();
        oldKeys = new HashMap<Integer, String>();
        this.editable = editable;
        columnHeaders = headers;
        columnCount = numberOfColumns;
    }

    public void setTable(JTable t)
    {
        table = t;
    }
    public void setParams(Map<String, Object> pp) {
        params = pp;
        keys = new ArrayList<String>();
        keys.addAll(pp.keySet());
        oldKeys = new HashMap<Integer, String>();
        Collections.sort(keys);
    }

    public int getRowCount() {
        if (keys == null) {
            return 0;
        }
        int blankRow = editable ? 1 : 0;
        return keys.size() + blankRow;
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
        return editable;
    }
    
    public int getColumnCount() {
        return columnCount;
    }

    public String getColumnName(int i) {
        if (columnHeaders == null)
            return "";
        if (i < columnHeaders.length)
            return columnHeaders[i];
        return "";
    }
    
    public void setColumn(int column, List<String> data)
    {
        params = new HashMap<String, Object>();
        keys = data;
        oldKeys = new HashMap<Integer, String>();
        Collections.sort(keys);
    }
    
    @Override
    public void removeRow(int row) {
        if (row >= keys.size()) {
            return;
        }

        params.remove(keys.get(row));
        keys.remove(row);

        this.fireTableRowsDeleted(row, row);
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (row >= keys.size()) {
            return "";
        }

        if (column == 0) {
            return keys.get(row);
        }
        if (column == 1) {
            return params.get(keys.get(row));
        } else {
            return "";
        }
    }

    public String getOldKey(int row)
    {
        return oldKeys.get(row);
    }
    public void removeOldKey(int row)
    {
        if (oldKeys.containsKey(row))
            oldKeys.remove(row);
    }
    
    @Override
    public void setValueAt(Object val, int row, int column) {
        if (table == null || ((String)val).isEmpty())
            return;

        if (getValueAt(row, column).equals(val))//nothing changed
            return;
        
        if (row >= keys.size()) {
            //we are adding a key or value
            if (column == 0 && params.containsKey(val))
            {
               int start = keys.indexOf((String)val); 
               keys.remove((String)val);
               params.put((String)val, (String) getValueAt(row, 1));
               this.fireTableCellUpdated(start, row);
            }else {
                if (column == 0)
                {
                    params.put((String)val, (String) getValueAt(row, 1));
                    keys.add((String)val);
                }else{
                    params.put((String) getValueAt(row, 0), val);
                    keys.add((String) getValueAt(row, 0));
                }
                this.fireTableRowsInserted(row, row);

            }
            table.getSelectionModel().clearSelection();
            return;
        }
        
        if (column == 0) {
            Object propVal = getValueAt(row, 1);
            oldKeys.put(row, (String)getValueAt(row, 0));
            keys.set(row, (String)val);
            params.put((String) val, propVal);
            
        } else {
            params.put((String) getValueAt(row, 0), val);
        }
        this.fireTableCellUpdated(row, row);
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
