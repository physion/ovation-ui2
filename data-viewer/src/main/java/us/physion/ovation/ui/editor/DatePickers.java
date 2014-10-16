package us.physion.ovation.ui.editor;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import us.physion.ovation.ui.interfaces.DateTimePicker;

/*
 * Copyright (C) 2014 Physion LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


public class DatePickers {

    static String[] availableIDs;

    static DateTimePicker createDateTimePicker() {
        DateTimePicker startPicker = new DateTimePicker();
        startPicker.setTimeZone(TimeZone.getTimeZone("UTC"));
        startPicker.setFormats(
                new DateFormat[]{DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM),
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)}
        );
        return startPicker;
    }

    static String getID(DateTimePicker p) {
        boolean found = false;
        String id = TimeZone.getDefault().getID();
        for (String s : getTimeZoneIDs()) {
            if (s.equals(id)) {
                found = true;
                break;
            }
        }
        if (!found) {
            id = p.getTimeZone().getID();
        }
        return id;
    }

    static String[] getTimeZoneIDs() {
        if (availableIDs == null) {
            ArrayList<String> ids = new ArrayList(DateTimeZone.getAvailableIDs());
            Collections.sort(ids);
            availableIDs = ids.toArray(new String[ids.size()]);
        }
        return availableIDs;
    }

    public static DateTime zonedDate(DateTimePicker datePicker, javax.swing.JComboBox zonePicker) {

        // datePicker.getDate() is giving us in local zone, so convert back to UTC
        DateTime pickedDate = new DateTime(datePicker.getDate()); //.withZone(DateTimeZone.forID("UTC"));
        
        //User entered date in UTC, but we want it in given zone
        return pickedDate.withZoneRetainFields(
                DateTimeZone.forID((String) zonePicker.getSelectedItem()));
    }
}
