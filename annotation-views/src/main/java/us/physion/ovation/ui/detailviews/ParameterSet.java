/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.util.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import us.physion.ovation.DataContext;
import us.physion.ovation.ui.*;

/**
 *
 * @author huecotanks
 */
class ParameterSet implements TableTreeKey {

    private String displayName;
    Object[][] data;
    
    public ParameterSet(String displayName, Map<String, Object> params) {
        this.displayName = displayName;
        data = new Object[params.size()][2];
        
        ArrayList<String> keys = new ArrayList();
        keys.addAll(params.keySet());
        Collections.sort(keys);
        int i =0;
        for (String key : keys)
        {
            data[i][0] = key;
            data[i++][1] = params.get(key);
        }
    }

    @Override
    public void refresh(DataContext c) {}

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public Object[][] getData() {
        return data;
    }

    @Override
    public String getID() {
        return displayName;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isExpandedByDefault() {
        return true;
    }

    @Override
    public int compareTo(Object t) {
        if (t instanceof ParameterSet)
        {
            return this.getDisplayName().compareTo(((ParameterSet)t).getDisplayName());
        }
        else{
            throw new UnsupportedOperationException("Object type '" + t.getClass() + "' cannot be compared with object type " + this.getClass());
        }
    }

    @Override
    public TableModelListener createTableModelListener(ScrollableTableTree t, TableNode n) {
        return null;
    }
    @Override
    public TableModel createTableModel() {
        return new DefaultTableModel(getData(), new String[]{"Name", "Property"});
    }
}
