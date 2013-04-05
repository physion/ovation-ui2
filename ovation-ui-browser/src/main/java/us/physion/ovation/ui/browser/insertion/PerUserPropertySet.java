/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.util.Map;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.User;
import us.physion.ovation.ui.browser.EntityWrapper;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.ScrollableTableTree;
import us.physion.ovation.ui.TableNode;
import us.physion.ovation.ui.TableTreeKey;

/**
 *
 * @author huecotanks
 */
class PerUserPropertySet implements TableTreeKey {

    String displayName;
    IEntityWrapper selected;
    IEntityWrapper user;
    boolean isOwner = false;
    Object[][] properties;
    public PerUserPropertySet(User u, IEntityWrapper selected) {
        this.selected = selected;
        user = new EntityWrapper(u);
        displayName = u.getUsername() + "'s Properties";
        if (selected.getEntity().getOwner().getUuid().equals(u.getUuid()))
        {
            isOwner = true;
            displayName += " (owner)";
        }
        
        Map<String, Object> props = selected.getEntity().getUserProperties(u);
        properties = new Object[props.size()][2];
        int i =0;
        for (String key : props.keySet())
        {
            properties[i][0] = key;
            properties[i++][1] = props.get(key);
        }
    }

    @Override
    public void refresh(DataStoreCoordinator dsc) {
    }

    @Override
    public String getDisplayName() {
        return displayName;
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
        return isOwner;
    }

    @Override
    public TableModelListener createTableModelListener(ScrollableTableTree stt, TableNode tn) {
        return null;
    }

    @Override
    public TableModel createTableModel() {
        return new DefaultTableModel(getData(), new String[] {"Name", "Value"});
    }

    @Override
    public Object[][] getData() {
        return properties;
    }

    @Override
    public int compareTo(Object t) {
        if (t instanceof PerUserPropertySet)
        {
            if (isOwner)
            {
                return -1;
            }else if(((PerUserPropertySet)t).isOwner)
            {
                return 1;
            }   
            return displayName.compareTo(((PerUserPropertySet)t).getDisplayName());
        }
        return -1;
    }
    
}
