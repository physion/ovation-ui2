package us.physion.ovation.ui.browser.insertion;

import java.text.DateFormat;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.calendar.SingleDaySelectionModel;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

//TODO: move this out into its own library
public class DateTimePicker extends JXDatePicker {
    private DateFormat timeFormat;

    public DateTimePicker() {
        super();
        getMonthView().setSelectionModel(new SingleDaySelectionModel());
    }

    public DateTimePicker( DateTime d ) {
        this();
        setDate(d.toDate());
    }

    public DateFormat getTimeFormat() {
        return timeFormat;
    }

    public void setDate(DateTime d) {
        super.setDate(d.withZone(DateTimeZone.forTimeZone(getTimeZone())).toDate());
    }

    public void setTimeFormat(DateFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

}
