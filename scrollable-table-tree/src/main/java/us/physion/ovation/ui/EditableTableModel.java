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
    List<String> keys;

    public EditableTableModel() {
        this(true);
    }
    
    public EditableTableModel(boolean editable) {
        super();
        params = new HashMap<String, Object>();
        keys = new ArrayList<String>();
        this.editable = editable;
    }

    public void setTable(JTable t)
    {
        table = t;
    }
    public void setParams(Map<String, Object> pp) {
        params = pp;
        keys = new ArrayList<String>();
        keys.addAll(pp.keySet());
        Collections.sort(keys);
    }

    public int getRowCount() {
        if (params == null) {
            return 0;
        }
        int blankRow = editable ? 1 : 0;
        return params.size() + blankRow;
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
        return editable;
    }
    
    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int i) {
        if (i == 0) {
            return "Key";
        }
        if (i == 1) {
            return "Value";
        }
        return "";
    }

    public void remove(int row) {
        if (row >= keys.size()) {
            return;
        }

        params.remove(keys.get(row));
        keys.remove(row);

        this.fireTableRowsDeleted(row, row);
    }

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

    @Override
    public void setValueAt(Object val, int row, int column) {
        if (table == null || !table.isEditing())
            return;
        
        if (((String)val).isEmpty())
            return;
        if (row >= keys.size()) {
            //we are adding a key or value
            if (column == 1) {
                if (getValueAt(row, 0).equals("")) {
                    params.put("<empty>", val);
                    keys.add("<empty>");
                    this.fireTableRowsInserted(row, row);
                } else {
                    params.put((String) getValueAt(row, 0), val);
                    this.fireTableRowsInserted(row, row);
                }

            } else {
                if (!params.containsKey(val)) {
                    keys.add((String) val);
                    //delete the row?
                    params.put((String) val, "");
                    this.fireTableCellUpdated(row, row);
                }else{
                    params.put((String) val, "");
                    this.fireTableRowsInserted(row, row);
                }
            }
            return;
        }
        if (column == 1) {
            params.put((String) getValueAt(row, 0), val);
            this.fireTableCellUpdated(row, row);
        } else {
            if (!params.containsKey(val)) {
                keys.add((String) val);
            }
            params.put((String) val, getValueAt(row, 1));
            this.fireTableCellUpdated(row, row);
        }
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
