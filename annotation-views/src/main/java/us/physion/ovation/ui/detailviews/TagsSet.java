/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import com.google.common.collect.Sets;
import java.util.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.openide.util.Lookup;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.mixin.KeywordAnnotatable;
import us.physion.ovation.ui.*;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author huecotanks
 */
public class TagsSet extends PerUserAnnotationSet{

    List<String> tags;
    Map<String, Object> properties;

    TagsSet(User u, boolean isOwner, boolean currentUser, List<String> tags, Set<String> uris)
    {
        super(u, isOwner, currentUser, uris);
        Collections.sort(tags);
        this.tags = tags;
    }
    
    List<String> getTags()
    {
        return tags;
    }

    @Override
    protected void refreshAnnotations(User u, Iterable<OvationEntity> entities) {
        this.tags = new ArrayList<>();
        for (OvationEntity eb: entities)
        {
            tags.addAll(Sets.newHashSet(((KeywordAnnotatable)eb).getUserTags(u)));
        }
        Collections.sort(tags);
    }

    @Override
    public String getDisplayName() {
        return getDisplayName("Tags");
    }

    @Override
    public int compareTo(Object t) {
        if (t instanceof TagsSet)
        {
            TagsSet s = (TagsSet)t;
            
            if (s.isExpandedByDefault())
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
    
    @Override
    public TableModelListener createTableModelListener(ScrollableTableTree t, TableNode n) {
        if (isEditable())
        {
            return new TagTableModelListener(uris,  (ExpandableJTree)t.getTree(), n, Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext());
        }
        return null;
    }

    @Override
    public TableModel createTableModel() {
        EditableTableModel m = new EditableTableModel(isCurrentUser(), 1, new String[]{"Tags"});
        m.setColumn(0, getTags());
        return m;
    }
    @Override
    public Object[][] getData()
    {
        Object[][] data = new Object[tags.size()][1];
        int i = 0;
        for (String tag: tags)
        {
            data[i++][0] = tag;
        }
        return data;
    }
}
