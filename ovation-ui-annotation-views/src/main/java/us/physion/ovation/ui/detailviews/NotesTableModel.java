/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.sql.Timestamp;
import java.util.*;
import javax.swing.table.DefaultTableModel;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import ovation.*;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.AnnotatableEntity;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.detailviews.NoteValue;

/**
 *
 * @author huecotanks
 */
class NotesTableModel extends DefaultTableModel {

    DataStoreCoordinator dsc;
    List<NoteValue> notes;

    NotesTableModel() {
        super();
        notes = new ArrayList<NoteValue>();
    }

    public void setDSC(DataStoreCoordinator dsc) {
        this.dsc = dsc;
    }

    public void setEntities(Collection<? extends IEntityWrapper> entities) {
        Set<String> annotationUUIDs = new HashSet();
        final int oldSize;
        if (notes != null)
        {
            oldSize = notes.size();
        }else{
            oldSize = 0;
        }
        notes = new ArrayList<NoteValue>();
        for (IEntityWrapper ew : entities) {
            if (AnnotatableEntity.class.isAssignableFrom(ew.getType())) {
                /*IAnnotation[] anns = ((IAnnotatableEntityBase) ew.getEntity()).getMyAnnotations();
                for (IAnnotation ann : anns) {
                    if (!annotationUUIDs.contains(ann.getUuid())) {
                        notes.add(new NoteValue(ann));
                        annotationUUIDs.add(ann.getUuid());
                    }
                }*/
            }
        }
        Collections.sort(notes);
        
        EventQueueUtilities.runOnEDT(new Runnable(){
            @Override
            public void run() {
                NotesTableModel.this.fireTableStructureChanged();
            }
        });
    }

    public int getRowCount() {
        if (notes == null) {
            return 0;
        }
        return notes.size();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int i) {
        if (i == 0) {
            return "Note";
        }
        return "";
    }

    public void remove(final int row) {
        if (row >= notes.size()) {
            return;
        }

        EventQueueUtilities.runOffEDT(new Runnable() {

            @Override
            public void run() {
                /*NoteValue note = notes.get(row);

                IAnnotation a = (IAnnotation) dsc.getContext().objectWithURI(note.uri);
                a.delete();
                notes.remove(row);

                EventQueueUtilities.runOnEDT(new Runnable(){

                    @Override
                    public void run() {
                        NotesTableModel.this.fireTableRowsDeleted(row, row);
                    }
                });
                * 
                */
            }
        });
    }

    public Object getValueAt(int row, int column) {

        if (row >= notes.size()) {
            return "";
        }

        if (column == 0) {
            return notes.get(row);
        }
        return "";
    }

    @Override
    public void setValueAt(final Object val, final int row, final int column) {
        if (row > notes.size()) {
            return;
        }

        EventQueueUtilities.runOffEDT(new Runnable() {

            @Override
            public void run() {
                /*NoteValue note = notes.get(row);
                
                if (val instanceof String)
                {
                    IAnnotation a = note.getAnnotation(dsc);
                    a.setText((String)val);
                    DateTime now = new DateTime();
                    a.addProperty("ovation_timestamp", new Timestamp(now.getMillis()));
                    a.addProperty("ovation_timezone", Calendar.getInstance().getTimeZone().getID());
                    note.update(dsc);
                }
                
                if (val instanceof DateTime)
                {
                    IAnnotation a = note.getAnnotation(dsc);
                    a.addProperty("ovation_timestamp", new Timestamp(((DateTime) val).getMillis()));
                    a.addProperty("ovation_timezone", Calendar.getInstance().getTimeZone().getID());
                }
                */
            }
        });
    }
  

    /*public void addNote(final IAnnotation ann) {
        
        EventQueueUtilities.runOffEDT(new Runnable() {

            @Override
            public void run() {
                NoteValue v = new NoteValue(ann);
                notes.add(v);
                Collections.sort(notes);
                final int row = notes.size() - 1;
                
                EventQueueUtilities.runOnEDT(new Runnable() {

                    @Override
                    public void run() {
                        NotesTableModel.this.fireTableRowsInserted(row, row);
                        NotesTableModel.this.fireTableDataChanged();
                    }
                });
            }
        });
    }*/
}
