/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.openide.util.Lookup;
import ovation.*;
import us.physion.ovation.ui.*;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

/**
 *
 * @author huecotanks
 */
class TagTableModelListener implements EditableTableModelListener {

    Set<String> uris;
    ResizableTree tree;
    IAuthenticatedDataStoreCoordinator dsc;
    TableNode node;
    public TagTableModelListener(Set<String> uriSet, ResizableTree expandableJTree, TableNode n, IAuthenticatedDataStoreCoordinator connection) {
        this.dsc = connection;
        uris = uriSet;
        this.tree = expandableJTree;
        this.node = n;
    }

    @Override
    public void tableChanged(TableModelEvent tme) {
        
        EditableTableModel t = (EditableTableModel)tme.getSource();
        int firstRow = tme.getFirstRow();
        int lastRow = tme.getLastRow();
                         
       if (tme.getType() == TableModelEvent.UPDATE || tme.getType() == TableModelEvent.INSERT)
        {
            List<String> old = new ArrayList<String>();
            List<String> newTags = new ArrayList<String>();
            for (int i = firstRow; i <= lastRow; i++) {
                String key = (String) t.getValueAt(i, 0);
                if (key == null || key.isEmpty())
                    continue;
                String oldKey = t.getOldKey(i);
                if (oldKey != null)
                {
                    old.add(oldKey);
                    t.removeOldKey(i);
                }
                newTags.add(key);
            }
            if (tme.getType() == TableModelEvent.INSERT) {
                EditableTable p = (EditableTable) node.getPanel();
                p.resize();
                tree.resizeNode(node);//this resizes the tree cell that contains the editable table that just deleted a row
            }
            final List<String> tags = newTags;
            final List<String> oldTags = old;
            EventQueueUtilities.runOffEDT(new Runnable() {

                @Override
                public void run() {
                    
                    DataContext c = dsc.getContext();
                    for (String tag: oldTags)
                    {
                        for (String uri : uris) {
                            IEntityBase eb = c.objectWithURI(uri);
                            if (eb instanceof ITaggableEntityBase)
                            {
                                if (tag != null && !tag.isEmpty()) {
                                    ((ITaggableEntityBase) eb).removeTag(tag.trim());
                                }
                            }
                        }
                    }
                    for (String tag: tags)
                    {
                        for (String uri : uris) {
                            IEntityBase eb = c.objectWithURI(uri);
                            if (eb instanceof ITaggableEntityBase) {
                                if (tag != null && !tag.isEmpty()) {
                                    ((ITaggableEntityBase) eb).addTag(tag.trim());
                                }
                            }
                        }
                    }
                    node.reset(dsc);
                }
            });
        }
    }
    //TODO: move these methods out into the other class
    protected static List<String> getTags(TableNode node)
    {
        return ((TagsSet) node.getUserObject()).getTags();
    }
    
    protected static void updateTagList(final Set<String> newTags, final Set<String> uris, final TableNode node, final IAuthenticatedDataStoreCoordinator dsc)
    {
        if (newTags.isEmpty()) {
            return;
        }
        //add new tags
        EventQueueUtilities.runOffEDT(new Runnable() {

            @Override
            public void run() {
                List<String> originalTags = getTags(node);
                List<String> toRemove = new ArrayList<String>();
                DataContext c = dsc.getContext();

                for (String original : originalTags) {
                    if (!newTags.contains(original)) {
                        toRemove.add(original);
                    } else {
                        newTags.remove(original);
                    }
                }
                for (String uri : uris) {
                    IEntityBase eb = c.objectWithURI(uri);
                    if (eb instanceof ITaggableEntityBase) {
                        for (String tag : newTags) {
                            if (tag != null && !tag.isEmpty()) {
                                ((ITaggableEntityBase) eb).addTag(tag.trim());
                            }
                        }
                        for (String tag : toRemove) {
                            if (tag != null && !tag.isEmpty()) {
                                ((ITaggableEntityBase) eb).removeTag(tag.trim());
                            }
                        }
                    }
                }
                node.reset(dsc);
            }
        });
    }

    public void deleteRows(final DefaultTableModel model, int[] rowsToRemove) {
        
        Arrays.sort(rowsToRemove);
        final int[] rows = rowsToRemove;
        EventQueueUtilities.runOffEDT(new Runnable() {

            @Override
            public void run() {

                Set<String> tags = new HashSet<String>();
                for (int i = rows.length - 1; i >= 0; i--) {
                    tags.add(((String) model.getValueAt(rows[i], 0)).trim());
                }

                final Set<String> toRemove = tags;

                DataContext c = dsc.getContext();
                for (String tag : toRemove) {
                    for (String uri : uris) {
                        IEntityBase eb = c.objectWithURI(uri);
                        if (eb instanceof ITaggableEntityBase)
                        {
                            ((ITaggableEntityBase)eb).removeTag(tag);
                        }
                    }
                }
                node.reset(dsc);

                //remove rows and resize
                EventQueueUtilities.runOnEDT(new Runnable() {

                    @Override
                    public void run() {
                        for (int i = rows.length - 1; i >= 0; i--) {
                            model.removeRow(rows[i]);
                        }
                        EditableTable p = (EditableTable)node.getPanel();
                        p.resize();
                        tree.resizeNode(node);//this resizes the tree cell that contains the editable table that just deleted a row
                   }
                });
            }
        });
    }
}
