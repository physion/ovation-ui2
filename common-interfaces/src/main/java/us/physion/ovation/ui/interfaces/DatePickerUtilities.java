/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimeZone;
import org.joda.time.DateTimeZone;

/**
 *
 * @author huecotanks
 */
public class DatePickerUtilities {
    static String[] availableIDs;
    public static DateTimePicker createDateTimePicker()
    {
        DateTimePicker startPicker = new DateTimePicker();
	startPicker.setTimeZone(TimeZone.getTimeZone("UTC"));
        startPicker.setFormats(
                new DateFormat[]{DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM),
                        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)}
        );
        startPicker.setTimeFormat(DateFormat.getTimeInstance(DateFormat.MEDIUM));
        return startPicker;
    }
    public static String getID(DateTimePicker p)
    {
        boolean found = false;
        String id = TimeZone.getDefault().getID();
        for (String s : availableIDs)
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
    public static String[] getTimeZoneIDs()
    {
        if (availableIDs == null)
        {
            ArrayList<String> ids = new ArrayList(DateTimeZone.getAvailableIDs());
            Collections.sort(ids);
            availableIDs = ids.toArray(new String[ids.size()]);
        }
        return availableIDs;
    }
}
