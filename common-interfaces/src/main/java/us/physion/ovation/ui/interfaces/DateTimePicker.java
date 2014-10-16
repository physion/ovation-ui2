package us.physion.ovation.ui.interfaces;

import java.text.DateFormat;
import java.util.Locale;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.calendar.SingleDaySelectionModel;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

//TODO: move this out into its own library
public final class DateTimePicker extends JXDatePicker {
    private boolean displayTime;

    public DateTimePicker() {
        super();
        getMonthView().setSelectionModel(new SingleDaySelectionModel());
        setTimeZone(DateTimeZone.getDefault().toTimeZone());
        displayTime = false;
    }
    
    
    public DateTimePicker( DateTime d ) {
        this();
        setDateTime(d);
        setTimeZone(d.getZone().toTimeZone());  
    }

    public void setDateTime(DateTime d) {
        setTimeZone(d.getZone().toTimeZone());
        setDate(d.toDate());
    }


    public DateTime getDateTime() {
        return new DateTime(getDate()).withZone(DateTimeZone.forTimeZone(getTimeZone()));
    }

    public boolean isDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(boolean displayTime) {
        this.displayTime = displayTime;
        if(displayTime) {
            setFormats(new DateFormat[] {DateFormat.getDateTimeInstance(
                DateFormat.LONG,
                DateFormat.MEDIUM,
                Locale.getDefault())});
        } else {
            setFormats(new DateFormat[]{DateFormat.getDateInstance(
                DateFormat.LONG)});
        }
    }
    
}
