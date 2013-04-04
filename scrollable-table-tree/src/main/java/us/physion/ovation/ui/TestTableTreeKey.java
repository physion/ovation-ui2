/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import us.physion.ovation.DataStoreCoordinator;

/**
 *
 * @author huecotanks
 */
public class TestTableTreeKey implements TableTreeKey{

    boolean isEditable;
    String name;
    String id;
    TestTableTreeKey(String name, String id, boolean isEditable)
    {
        this.name = name;
        this.id = id;
        this.isEditable = isEditable;
    }
    @Override
    public void refresh(DataStoreCoordinator dsc) {
        
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public boolean isEditable() {
        return isEditable;
    }

    @Override
    public boolean isExpandedByDefault() {
        return isEditable;
    }

    @Override
    public TableModelListener createTableModelListener(ScrollableTableTree t, TableNode n) {
        return new EditableTableModelListener() {

            @Override
            public void deleteRows(DefaultTableModel model, int[] rowsToRemove) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void tableChanged(TableModelEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    @Override
    public TableModel createTableModel() {
        return new DefaultTableModel(getData(), new String[] {"Col 1", "Col 2"});
    }

    @Override
    public Object[][] getData() {
        return new Object[][]{{"key 1", "key 2"}, {"value 1", "value 2"}};
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof TestTableTreeKey)
            return name.compareTo(((TestTableTreeKey)o).name);
        return -1;
    }
    
}
