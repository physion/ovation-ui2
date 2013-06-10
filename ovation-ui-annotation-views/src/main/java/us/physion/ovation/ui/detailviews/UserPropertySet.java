/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.util.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.mixin.Owned;
import us.physion.ovation.domain.mixin.PropertyAnnotatable;
import us.physion.ovation.ui.*;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author huecotanks
 */
class UserPropertySet extends PerUserAnnotationSet {

    Map<String, Object> properties;

    UserPropertySet(User u, boolean isOwner, boolean currentUser, Map<String, Object> props, Set<String> uris)
    {
        super(u, currentUser, isOwner, uris);
        properties = props;
    }

    
    public void refreshAnnotations(User u, Iterable<OvationEntity> entities) {
        this.properties = new HashMap<String, Object>();
        for (OvationEntity eb : entities)
        {
            properties.putAll(((PropertyAnnotatable)eb).getUserProperties(u));
        }
    }

    public String getDisplayName() {
        return getDisplayName("Properties");
    }

    Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public TableModel createTableModel() {
        EditableTableModel m = new EditableTableModel(true);
        m.setParams(properties);
        return m;
    }

    @Override
    public TableModelListener createTableModelListener(us.physion.ovation.ui.ScrollableTableTree t, us.physion.ovation.ui.TableNode n) {
        if (isEditable())
        {
            return new PropertyTableModelListener(uris, (ExpandableJTree)t.getTree(), n, Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext());
        }
        return null;
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
}
