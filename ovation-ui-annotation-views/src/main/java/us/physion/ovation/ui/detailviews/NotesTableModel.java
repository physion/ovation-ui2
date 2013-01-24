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
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
class NotesTableModel extends DefaultTableModel {

    void writeToDatabase(int firstRow, int lastRow) {
        
        /*for (int i = firstRow; i<=lastRow; i++)
        {
            NoteValue v = notes.get(i);
            IAnnotation n = (IAnnotation) dsc.getContext().objectWithURI(v.uri);
            n.setText(v.text);
        }*/
    }

    private static class NoteValue implements Comparable<NoteValue>{

        String text;
        String uri;
        DateTime timestamp;
        public NoteValue(IAnnotation ann) {
            uri = ann.getURIString();
            text = ann.getText();
            Timestamp ts = (Timestamp)(ann.getMyProperty("ovation_timestamp"));
            String timezone = (String)(ann.getMyProperty("ovation_timezone"));
            if (ts != null && timezone != null && !timezone.isEmpty())
            {
                timestamp = new DateTime(ts, DateTimeZone.forID(timezone));
            }
            if (timestamp == null && ann instanceof TimelineAnnotation)
            {
                timestamp = ((TimelineAnnotation) ann).getStartTime();
            }
        }

        @Override
        public int compareTo(NoteValue t) {
            if (timestamp == null)
                return -1;
            if (t.timestamp == null)
                return 1;
            
            return timestamp.compareTo(t.timestamp);
        }
        
        public IAnnotation getAnnotation(IAuthenticatedDataStoreCoordinator dsc) {
            return (IAnnotation)dsc.getContext().objectWithURI(uri);
        }
        
        public void update(IAuthenticatedDataStoreCoordinator dsc) {
            IAnnotation ann = getAnnotation(dsc);
            text = ann.getText();
            Timestamp ts = (Timestamp)(ann.getMyProperty("ovation_timestamp"));
            String timezone = (String)(ann.getMyProperty("ovation_timezone"));
            if (ts != null && timezone != null && !timezone.isEmpty())
            {
                timestamp = new DateTime(ts, DateTimeZone.forID(timezone));
            }
        }
    }

    IAuthenticatedDataStoreCoordinator dsc;
    List<NoteValue> notes;

    NotesTableModel() {
        super();
        notes = new ArrayList<NoteValue>();
    }
    
    public void setDSC(IAuthenticatedDataStoreCoordinator dsc)
    {
        this.dsc = dsc;
    }

    public void setEntities(Collection<? extends IEntityWrapper> entities) {
        Set<String> annotationUUIDs = new HashSet();
        notes = new ArrayList<NoteValue>();
        for (IEntityWrapper ew : entities)
        {
            if (IAnnotatableEntityBase.class.isAssignableFrom(ew.getType()))
            {
                IAnnotation[] anns = ((IAnnotatableEntityBase)ew.getEntity()).getMyAnnotations();
                for (IAnnotation ann : anns)
                {
                    if (!annotationUUIDs.contains(ann.getUuid()))
                    {
                        notes.add(new NoteValue(ann));
                        annotationUUIDs.add(ann.getUuid());
                    }
                }
            }
        }
        //fire rows inserted/deleted?
        this.fireTableDataChanged();
    }

    public int getRowCount() {
        if (notes == null) {
            return 0;
        }
        return notes.size();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column ==0)
            return false;
        return true;
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int i) {
        if (i == 0) {
            return "Time";
        }
        if (i == 1) {
            return "Note";
        }
        return "";
    }

    public void remove(int row) {
        if (row >= notes.size()) {
            return;
        }
        NoteValue note = notes.get(row);

        //TODO: run this on another thread
        IAnnotation a = (IAnnotation)dsc.getContext().objectWithURI(note.uri);
        a.delete();
        notes.remove(row);

        this.fireTableRowsDeleted(row, row);
    }

    public Object getValueAt(int row, int column) {

        if (row >= notes.size()) {
            return "";
        }

        NoteValue note = notes.get(row);
        if (column == 0) {
            return note.timestamp;
        }
        if (column == 1) {
            return note.text;
        } else {
            return "";
        }
    }

    @Override
    public void setValueAt(Object val, int row, int column) {
        if (row > notes.size())
            return;
        NoteValue note = notes.get(row);
        if (column == 0)
        {
            //run in another thread;
            IAnnotation a = note.getAnnotation(dsc);
            if (val instanceof DateTime)
            {
                a.addProperty("ovation_timestamp", new Timestamp(((DateTime)val).getMillis()));
                a.addProperty("ovation_timezone", Calendar.getInstance().getTimeZone().getID());
            }
            else{
                //convert string
            }
            note.update(dsc);
        }
        if (column == 1) {
            //run in another thread;
            IAnnotation a = note.getAnnotation(dsc);
            a.setText(val.toString());
            note.update(dsc);
        }
    }
    
    public void addNote(IAnnotation ann)
    {
        NoteValue v = new NoteValue(ann);
        notes.add(v);
        Collections.sort(notes);
        int row = notes.size() -1;
        
        this.fireTableRowsInserted(row, row);
        this.fireTableDataChanged();
    }


}
