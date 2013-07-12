/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.sql.Timestamp;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author huecotanks
 */
public class NoteValue implements Comparable<NoteValue> {

    String text;
    String uri;
    DateTime timestamp;

    /*public NoteValue(IAnnotation ann) {
        
        uri = ann.getURIString();
        text = ann.getText();
        Timestamp ts = (Timestamp) (ann.getMyProperty("ovation_timestamp"));
        String timezone = (String) (ann.getMyProperty("ovation_timezone"));
        if (ts != null && timezone != null && !timezone.isEmpty()) {
            timestamp = new DateTime(ts, DateTimeZone.forID(timezone));
        }
        if (timestamp == null && ann instanceof TimelineAnnotation) {
            timestamp = ((TimelineAnnotation) ann).getStartTime();
        }
    }

    @Override
    public int compareTo(NoteValue t) {
        if (timestamp == null) {
            return -1;
        }
        if (t.timestamp == null) {
            return 1;
        }

        return timestamp.compareTo(t.timestamp);
    }

    public IAnnotation getAnnotation(IAuthenticatedDataStoreCoordinator dsc) {
        return (IAnnotation) dsc.getContext().objectWithURI(uri);
    }

    public void update(IAuthenticatedDataStoreCoordinator dsc) {
        IAnnotation ann = getAnnotation(dsc);
        text = ann.getText().split("\n")[0];
        Timestamp ts = (Timestamp) (ann.getMyProperty("ovation_timestamp"));
        String timezone = (String) (ann.getMyProperty("ovation_timezone"));
        if (ts != null && timezone != null && !timezone.isEmpty()) {
            timestamp = new DateTime(ts, DateTimeZone.forID(timezone));
        }
    }*/

    @Override
    public int compareTo(NoteValue t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
