/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import javax.swing.JScrollPane;
import us.physion.ovation.ui.interfaces.DateTimePicker;

/**
 *
 * @author huecotanks
 */
public class DateTimePickerPanel {

    private DateTimePicker picker;
    private JScrollPane pane;

    public DateTimePickerPanel() {
        this(DatePickers.createDateTimePicker());
    }
    
    public DateTimePickerPanel(DateTimePicker p) {
        picker = p;
        pane = new JScrollPane();
        pane.setViewportView(picker);
    }

    public DateTimePicker getPicker() {
        return picker;
    }

    public JScrollPane getPane() {
        return pane;
    }
}