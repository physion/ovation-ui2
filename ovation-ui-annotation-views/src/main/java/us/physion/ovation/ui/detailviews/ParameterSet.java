/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.util.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import ovation.DataContext;
import ovation.IAuthenticatedDataStoreCoordinator;
import ovation.IEntityBase;
import ovation.User;
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
    public void refresh(IAuthenticatedDataStoreCoordinator dsc) {
        /*DataContext c = dsc.getContext();
        User u = (User)c.objectWithURI(userURI);
        
        boolean owner = false;
        String uuid = u.getUuid();
        Map<String, Object> props = new HashMap<String, Object>();
        for (String uri: uris)
        {
            IEntityBase eb = c.objectWithURI(uri);
            if (eb.getOwner().getUuid().equals(uuid))
            {
                owner = true;
            }
            props.putAll(eb.getUserProperties(u));
        }
        
        username = u.getUsername();
        this.isOwner = owner;
        this.properties = props;
        this.current = c.currentAuthenticatedUser().getUuid().equals(u.getUuid());
        * 
        */
    }

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
