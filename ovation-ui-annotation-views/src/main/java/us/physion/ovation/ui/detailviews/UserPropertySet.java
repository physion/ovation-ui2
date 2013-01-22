/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.util.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.openide.util.Lookup;
import ovation.DataContext;
import ovation.IAuthenticatedDataStoreCoordinator;
import ovation.IEntityBase;
import ovation.User;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author huecotanks
 */
class UserPropertySet implements TableTreeKey{
    String username;
    String userURI;
    boolean isOwner;
    boolean current;
    Map<String, Object> properties;
    Set<String> uris;

    UserPropertySet(User u, boolean isOwner, boolean currentUser, Map<String, Object> props, Set<String> uris)
    {
        username = u.getUsername();
        this.isOwner = isOwner;
        this.properties = props;
        this.current = currentUser;
        this.uris = uris;
        userURI = u.getURIString();
    }
    
    public void refresh(IAuthenticatedDataStoreCoordinator dsc) {
        DataContext c = dsc.getContext();
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
    }

    public String getDisplayName() {
        String s = username + "'s Properties";
        if (isOwner) {
            return s + " (owner)";
        }
        return s;
    }
    
    public String getID()
    {
        return userURI;
    }

    boolean isCurrentUser() {
        return current;
    }

    public Object[][] getData()
    {
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(properties.keySet());
        Collections.sort(keys);
        
        Object[][] data = new Object[keys.size()][2];
        int i = 0;
        for (String key : keys)
        {
            data[i][0] = key;
            data[i++][1] = properties.get(key);
        }
        return data;
    }
    
    Map<String, Object> getProperties() {
        return properties;
    }
    
    String getUsername()
    {
        return username;
    }
    
    @Override
    public int compareTo(Object t) {
        if (t instanceof UserPropertySet)
        {
            UserPropertySet s = (UserPropertySet)t;
            
            if (s.isCurrentUser())
            {
                if (this.isCurrentUser())
                    return 0;
                return 1;
            }
            if (this.isCurrentUser())
                return -1;
            return this.getUsername().compareTo(s.getUsername());
        }
        else{
            throw new UnsupportedOperationException("Object type '" + t.getClass() + "' cannot be compared with object type " + this.getClass());
        }
    }
    
    public boolean isEditable()
    {
        return isCurrentUser();
    }
    public boolean isExpandedByDefault()
    {
        return isCurrentUser();
    }
    
    @Override
    public TableModelListener createTableModelListener(ScrollableTableTree t, TableNode n) {
        if (isEditable())
        {
            return new PropertyTableModelListener(uris, (ExpandableJTree)t.getTree(), n, Lookup.getDefault().lookup(ConnectionProvider.class).getConnection());
        }
        return null;
    }

    @Override
    public TableModel createTableModel() {
        return new DefaultTableModel(getData(), new String[]{"Name", "Property"});
    }
}
