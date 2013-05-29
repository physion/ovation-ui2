/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;
import com.google.common.base.Function;
import java.awt.Point;
import java.util.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author jackie
 */
public class ParameterTableModel extends DefaultTableModel {

    Map<String, Object> params;
    boolean uiEditable;// can the values be edited by clicking on them?
    List<String> keys;
    String[] columnNames;
    
    Function<Point, Boolean> editableFunction;

    public ParameterTableModel() {
        this(true);
    }
    
    public ParameterTableModel(final boolean uiEditable) {
        super();
        params = new HashMap<String, Object>();
        keys = new ArrayList<String>();
        this.uiEditable = uiEditable;
        setColumnNames(new String[]{"Key", "Value"});
        editableFunction = new Function<Point, Boolean>() {

            @Override
            public Boolean apply(Point input) {
                return uiEditable;
            }
        };
    }

    public void setParams(Map<String, Object> pp) {
        params = pp;
        keys = new ArrayList<String>();
        keys.addAll(pp.keySet());
        Collections.sort(keys);
    }
    
    public void setColumnNames(String[] columnNames)
    {
        this.columnNames= columnNames;
    }

    public void setEditableFunction(Function<Point, Boolean> editableFunction)
    {
        this.editableFunction = editableFunction;
    }
    
    public int getRowCount() {
        if (params == null) {
            return 0;
        }
        int blankRow = uiEditable ? 1 : 0;
        return params.size() + blankRow;
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
        return editableFunction.apply(new Point(row, column));
    }
    
    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int i) {
        return columnNames[i];
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
    public void setValueAt(Object o, int row, int column) {

        if(column == 0)//setting a key
        {
            if (params.containsKey(o))
            {
                //key already exists, something weird is happening
                return;
            }
            if (row >= keys.size())
            {
                if (o == null || ((String)o).isEmpty())
                    return;
                keys.add((String)o);
                params.put((String)o, "");
            }else{
                if (o == null || ((String)o).isEmpty())
                {
                    remove(row);
                    return;
                }
                String oldKey = keys.get(row);
                Object oldValue = params.get(oldKey);
                params.remove(oldKey);
                params.put((String)o, oldValue);
                keys.set(row, (String)o);
                Collections.sort(keys);
            }
            
        }else{//setting a value
            if (row >= keys.size())
            {
                //this is weird. users should insert key first, then value
                return;
            }else{
                //setting a value on an existing key
                params.put(keys.get(row), o);
                Collections.sort(keys);// we sort, in case people have added a new key value pair
            }
        }
        /*
        if (row >= keys.size()) {
            if (column == 0)
            {
                if(params.containsKey(o))
                {
                    return;
                }
                keys.add((String) o);
                params.put((String) o, "");
            }
            else {
                String key = (String)getValueAt(row, 0);
                if (key.isEmpty()) {
                    params.put("<empty>", o);
                    keys.add("<empty>");
                    Collections.sort(keys);
                } else {
                    params.put(key, o);
                }

            } 
        }
        else {//modifying an existing row
            if (column == 1) {
                params.put((String) getValueAt(row, 0), o);
                Collections.sort(keys);
            } else {
                if (o == null || o.equals(""))
                {
                    remove(row);
                    return;
                }
                if (!params.containsKey(o)) {
                    keys.add((String) o);
                }
                params.put((String) o, getValueAt(row, 1));
            }
        }*/
    }
    
    public void addParameter(String key, Object value)
    {
        if (keys.contains(key))
        {
            setValueAt(value, keys.indexOf(key), 1);
        } else {
            int num = getRowCount();
            setValueAt(key, num, 0);
            setValueAt(value, num, 1);
            this.fireTableRowsInserted(num, num);
        }
    }

    public Map<String, Object> getParams() {
        return params;
    }
}