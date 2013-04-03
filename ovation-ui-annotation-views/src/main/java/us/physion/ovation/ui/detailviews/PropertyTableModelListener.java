/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.AnnotatableEntity;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.ui.*;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

/**
 *
 * @author huecotanks
 */
class PropertyTableModelListener implements us.physion.ovation.ui.EditableTableModelListener{

    ResizableTree tree;
    Set<String> uris;
    DataStoreCoordinator dsc;
    TableNode node;
    public PropertyTableModelListener(Set<String> uriSet, ResizableTree tree, TableNode node) {
        this.dsc = Lookup.getDefault().lookup(ConnectionProvider.class).getConnection();
        uris = uriSet;
        this.tree = tree;
        this.node = node;
    }

    // this contructor is used in unit tests
    public PropertyTableModelListener(Set<String> uriSet, ResizableTree tree, TableNode node,
            DataStoreCoordinator dsc) {
        this.dsc = dsc;
        uris = uriSet;
        this.tree = tree;
        this.node = node;
    }

    @Override
    public void tableChanged(TableModelEvent tme) {
        EditableTableModel t = (EditableTableModel)tme.getSource();
        int firstRow = tme.getFirstRow();
        int lastRow = tme.getLastRow();
                         
       if (tme.getType() == TableModelEvent.UPDATE || tme.getType() == TableModelEvent.INSERT)
        {
            List<String> old = new ArrayList<String>();
            Map<String, Object> newProperties = new HashMap<String, Object>();
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
                Object value = t.getValueAt(i, 1);
                newProperties.put(key, value);
            }
            if (tme.getType() == TableModelEvent.INSERT) {
                EditableTable p = (EditableTable) node.getPanel();
                p.resize();
                tree.resizeNode(node);//this resizes the tree cell that contains the editable table that just deleted a row
            }
            final Map<String, Object> props = newProperties;
            final List<String> oldKeys = old;
            EventQueueUtilities.runOffEDT(new Runnable() {

                @Override
                public void run() {
                    
                    DataContext c = dsc.getContext();
                    for (String key: oldKeys)
                    {
                        for (String uri : uris) {
                            OvationEntity eb = c.getObjectWithURI(uri);
                            if (eb instanceof AnnotatableEntity)
                                ((AnnotatableEntity)eb).removeProperty(key);
                        }
                    }
                    for (String key: props.keySet())
                    {
                        for (String uri : uris) {
                            OvationEntity eb = c.getObjectWithURI(uri);
                            if (eb instanceof AnnotatableEntity)
                                parseAndAdd((AnnotatableEntity)eb, key, props.get(key));
                        }
                    }
                    node.reset(dsc);
                }
            });
        }
    }

    public void deleteRows(final DefaultTableModel model, int[] rowsToRemove) {
        
        Arrays.sort(rowsToRemove);
        final int[] rows = rowsToRemove;
        EventQueueUtilities.runOffEDT(new Runnable() {

            @Override
            public void run() {
                DataContext c = dsc.getContext();
                for (int i = rows.length - 1; i >= 0; i--) {
                    String key = (String) model.getValueAt(rows[i], 0);
                    final Object value = model.getValueAt(rows[i], 1);

                    for (String uri : uris) {
                        OvationEntity entity = c.getObjectWithURI(uri);
                        if (entity instanceof AnnotatableEntity)
                        {
                            AnnotatableEntity eb = (AnnotatableEntity) entity;
                            Map<String, Object> properties = eb.getUserProperties(c.getAuthenticatedUser());
                            if (properties.containsKey(key) && properties.get(key).equals(value)) {
                                eb.removeProperty(key);
                            }
                        }
                    }

                }
                node.reset(dsc);
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

    void parseAndAdd(AnnotatableEntity eb, String key, Object value)
    {
        if (value == null){
            eb.addProperty(key, "");
            return;
        }
        if (value instanceof String) {
            String s = (String) value;
            try {
                int v = Integer.parseInt(s);
                eb.addProperty(key, v);
                return;
            } catch (NumberFormatException e) {
            }
            try {
                long v = Long.parseLong(s);
                eb.addProperty(key, v);
                return;
            } catch (NumberFormatException e) {
            }
            try {
                double v = Double.parseDouble(s);
                eb.addProperty(key, v);
                return;
            } catch (NumberFormatException e) {
            }
            if (s.toLowerCase().equals("true"))
            {
                eb.addProperty(key, true);
                return;
            }
            if (s.toLowerCase().equals("false"))
            {
                eb.addProperty(key, false);
                return;
            }
            
            try{
                DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
                DateTime dt = fmt.parseDateTime(s);
                eb.addProperty(key, new Timestamp(dt.getMillis()));
                return;
            } catch (IllegalArgumentException e) {
            }
            ArrayList<String> patterns = new ArrayList();
            String pattern1 = ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())).toLocalizedPattern();
            String pattern2 = ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())).toLocalizedPattern();
            String pattern3 = ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())).toLocalizedPattern();
            String pattern4 = pattern1 + " " + ((SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())).toLocalizedPattern();
            String pattern5 = pattern2 + " " + ((SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.getDefault())).toLocalizedPattern();
            String pattern6 = pattern3 + " " + ((SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.LONG, Locale.getDefault())).toLocalizedPattern();

            patterns.add(pattern1);
            patterns.add(pattern2);
            patterns.add(pattern3);
            patterns.add(pattern4);
            patterns.add(pattern5);
            patterns.add(pattern6);

            for (String pattern : patterns) {
                try {

                    DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
                    DateTime dt = fmt.parseDateTime(s);
                    eb.addProperty(key, new Timestamp(dt.getMillis()));
                    return;
                } catch (IllegalArgumentException e) {}
            }
            
            //byte array
            //NumericData
            //EntityBase
        }
        eb.addProperty(key, value);//string case
    }
}
