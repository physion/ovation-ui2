package us.physion.ovation.ui.interfaces;

import java.text.DateFormat;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.calendar.SingleDaySelectionModel;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

//TODO: move this out into its own library
public final class DateTimePicker extends JXDatePicker {
    private DateFormat timeFormat;

    public DateTimePicker() {
        super();
        getMonthView().setSelectionModel(new SingleDaySelectionModel());
        setTimeZone(DateTimeZone.getDefault().toTimeZone());
    }

    public DateTimePicker( DateTime d ) {
        this();
        setDateTime(d);
        setTimeZone(d.getZone().toTimeZone());
    }

    public DateFormat getTimeFormat() {
        return timeFormat;
    }

    public void setDateTime(DateTime d) {
        setTimeZone(d.getZone().toTimeZone());
        setDate(d.toDate());
    }


    public DateTime getDateTime() {
        return new DateTime(getDate()).withZone(DateTimeZone.forTimeZone(getTimeZone()));
    }

    public void setTimeFormat(DateFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

}
