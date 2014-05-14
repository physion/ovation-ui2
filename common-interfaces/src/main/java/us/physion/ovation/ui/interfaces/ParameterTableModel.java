/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.awt.Point;
import java.util.*;
import javax.swing.table.DefaultTableModel;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author jackie
 */
@Messages({
    "Key_column_name=Key",
    "Value_column_name=Value"
})
public class ParameterTableModel extends DefaultTableModel {

    private Map<String, Object> params;
    private boolean uiEditable;// can the values be edited by clicking on them?
    private List<String> keys;
    private String[] columnNames;
    private Set<String> removedKeys;

    Function<Point, Boolean> editableFunction;

    public ParameterTableModel() {
        this(true);
    }

    public ParameterTableModel(final boolean uiEditable) {
        super();
        params = Maps.newHashMap();
        keys = Lists.newArrayList();
        removedKeys = Sets.newHashSet();
        this.uiEditable = uiEditable;

        columnNames = new String[]{Bundle.Key_column_name(), Bundle.Value_column_name()};

        editableFunction = new Function<Point, Boolean>() {

            @Override
            public Boolean apply(Point input) {
                return uiEditable;
            }
        };
    }

    public synchronized Iterable<String> getAndClearRemovedKeys() {
        Set<String> removed = ImmutableSet.copyOf(removedKeys);
        removedKeys.clear();
        
        return removed;
    }

    public void setParams(Map<String, Object> pp) {
        params = Maps.newHashMap(pp);
        keys = Lists.newArrayList(pp.keySet());
        Collections.sort(keys);
    }

    public void addParameter(String key, Object value) {
        if (keys.contains(key)) {
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

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public void setEditableFunction(Function<Point, Boolean> editableFunction) {
        this.editableFunction = editableFunction;
    }

    @Override
    public int getRowCount() {
        if (params == null) {
            return 0;
        }
        int blankRow = uiEditable ? 1 : 0;
        return params.size() + blankRow;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return editableFunction.apply(new Point(row, column));
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
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

    public int countKeys(String name) {
        if (!keys.contains(name)) {
            return 0;
        }

        for (int i = 1; i < keys.size(); i++) {
            if (!keys.contains(name + "." + i)) {
                return i;
            }
        }
        return keys.size();
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (row >= keys.size()) {
            return "";
        }

        Object value;
        switch (column) {
            case 0:
                value = keys.get(row);
                break;
            case 1:
                value = params.get(keys.get(row));
                break;
            default:
                value = "";
        }

        return value;
    }

    @Override
    public void setValueAt(Object o, int row, int column) {

        if (column == 0)//setting a key
        {
            String key = (String) o;
            if (params.containsKey(key)) {
                //key already exists, something weird is happening
                return;
            }

            if (row >= keys.size()) {
                if (o == null || key.isEmpty()) {
                    return;
                }
                keys.add((String) o);
                removedKeys.remove(((String) o));
                params.put((String) o, "");
            } else {
                if (o == null || key.isEmpty()) {
                    removedKeys.add((String) getValueAt(row, column));
                    remove(row);
                    return;
                }

                String oldKey = keys.get(row);
                Object oldValue = params.get(oldKey);
                params.remove(oldKey);
                removedKeys.add(oldKey);
                params.put((String) o, oldValue);
                keys.set(row, (String) o);
                //Collections.sort(keys);
            }

        } else {//setting a value
            if (row >= keys.size()) {
                //this is weird. users should insert key first, then value
                return;
            } else {
                //setting a value on an existing key
                params.put(keys.get(row), o);
                //Collections.sort(keys);// we sort, in case people have added a new key value pair
            }
        }

        this.fireTableCellUpdated(row, column);

    }
}
