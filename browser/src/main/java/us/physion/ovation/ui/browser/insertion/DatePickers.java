/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import us.physion.ovation.ui.interfaces.DateTimePicker;

/**
 *
 * @author huecotanks
 */
public class DatePickers {
    static String[] availableIDs;
    static DateTimePicker createDateTimePicker()
    {
        DateTimePicker startPicker = new DateTimePicker();
	startPicker.setTimeZone(TimeZone.getTimeZone("UTC"));
        startPicker.setFormats(
                new DateFormat[]{DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM),
                        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)}
        );
        
        return startPicker;
    }

    static String getID(DateTimePicker p)
    {
        boolean found = false;
        String id = TimeZone.getDefault().getID();
        for (String s : getTimeZoneIDs())
        {
            if (s.equals(id))
            {
                found = true;
                break;
            }
        }
        if (!found)
        {
            id = p.getTimeZone().getID();
        }
        return id;
    }

    static String[] getTimeZoneIDs()
    {
        if (availableIDs == null)
        {
            ArrayList<String> ids = new ArrayList(DateTimeZone.getAvailableIDs());
            Collections.sort(ids);
            availableIDs = ids.toArray(new String[ids.size()]);
        }
        return availableIDs;
    }

    public static DateTime zonedDate(DateTimePicker datePicker, javax.swing.JComboBox zonePicker) {

        // datePicker.getDate() is giving us in local zone, so convert back to UTC
        DateTime pickedDate = new DateTime(datePicker.getDate()).withZone(DateTimeZone.forID("UTC"));
        //User entered date in UTC, but we want it in given zone
        return pickedDate.withZoneRetainFields(
                DateTimeZone.forID((String) zonePicker.getSelectedItem()));
    }
}
