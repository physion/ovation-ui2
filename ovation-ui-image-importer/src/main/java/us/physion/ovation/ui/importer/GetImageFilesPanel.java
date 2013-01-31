/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.openide.util.ChangeSupport;
import us.physion.ovation.ui.interfaces.DatePickerUtilities;
import us.physion.ovation.ui.interfaces.DateTimePicker;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author huecotanks
 */
public class GetImageFilesPanel extends javax.swing.JPanel
{

    private static class DateTimePickerPanel
    {

        private DateTimePicker picker;
        private JScrollPane pane;

        public DateTimePickerPanel(DateTimePicker p)
        {
            picker = p;
            pane = new JScrollPane();
            pane.setViewportView(picker);
        }

        public DateTimePicker getPicker()
        {
            return picker;
        }

        public JScrollPane getPane()
        {
            return pane;
        }
    }

    private FileTableModel fileTableModel;

    class FileTableModel extends DefaultTableModel
    {
        ArrayList<FileMetadata> files = new ArrayList<FileMetadata>();
        ArrayList<DateTimePickerPanel> startPickers = new ArrayList<DateTimePickerPanel>();
        ArrayList<DateTimePickerPanel> endPickers = new ArrayList<DateTimePickerPanel>();
        ArrayList<JComboBox> timezoneComboBoxes = new ArrayList<JComboBox>();

        String[] timezoneIDs;

        FileTableModel()
        {
            super();
            timezoneIDs = DatePickerUtilities.getTimeZoneIDs();
        }

        public int getSize()
        {
            return files.size();
        }

        public int getRowCount()
        {
            if (files == null) {
                return 0;
            }
            return files.size();
        }

        public int getColumnCount()
        {
            return 4;
        }

        public String getColumnName(int i)
        {
            if (i == 0) {
                return "File";
            }
            if (i == 1) {
                return "Epoch Start";
            }
            if (i == 2) {
                return "Epoch End";
            }
            if (i == 3) {
                return "Timezone";
            }
            return "";
        }

        public Object getValueAt(int row, int column)
        {
            FileMetadata meta = files.get(row);
            if (column == 0) {
                return new JLabel(meta.getFile().getName());
            }
            if (column == 1) {
                DateTimePickerPanel p = getPicker(row, column);
                return p.getPane();
            }
            if (column == 2) {
                DateTimePickerPanel p = getPicker(row, column);
                return p.getPane();
            }
            if (column == 3) {
                JComboBox b = timezoneComboBoxes.get(row);
                return b;
            } else {
                return null;
            }
        }

        public void add(FileMetadata meta)
        {
            files.add(meta);

            DateTimePickerPanel start = new DateTimePickerPanel(DatePickerUtilities.createDateTimePicker());
            start.getPicker().setDate(meta.getStart());
            startPickers.add(start);

            DateTimePickerPanel end = new DateTimePickerPanel(DatePickerUtilities.createDateTimePicker());
            end.getPicker().setDate(meta.getEnd(true));
            endPickers.add(end);

            JComboBox timezones = new JComboBox(new DefaultComboBoxModel(timezoneIDs));
            timezones.setSelectedItem(TimeZone.getDefault().getID());
            timezoneComboBoxes.add(timezones);

            fireTableRowsInserted(files.size() - 1, files.size() - 1);
            cs.fireChange();
        }

        public void remove(int i)
        {
            files.remove(i);
            startPickers.remove(i);
            endPickers.remove(i);
            timezoneComboBoxes.remove(i);
            fireTableRowsDeleted(files.size(), files.size());
            cs.fireChange();
        }

        public List<FileMetadata> getFiles()
        {
            return files;
        }

        private DateTimePickerPanel getPicker(int row, int column)
        {
            if (column == 1) {
                if (row < startPickers.size()) {
                    return startPickers.get(row);
                }

            } else if (column == 2) {
                if (row < endPickers.size()) {
                    return endPickers.get(row);
                }
            }
            return null;
        }
    }

    @Override
    public String getName()
    {
        return "Insert Epochs";
    }

    ChangeSupport cs;

    /**
     * Creates new form GetImageFilesPanel
     */
    public GetImageFilesPanel(ChangeSupport cs, ArrayList<FileMetadata> files)
    {
        this.cs = cs;
        fileTableModel = new FileTableModel();
        initComponents();
        jTable1.setRowHeight(40);
        jTable1.setDefaultRenderer(Object.class, new TableCellRenderer()
        {

            @Override
            public Component getTableCellRendererComponent(JTable jtable, Object o, boolean bln, boolean bln1, int row,
                                                           int column)
            {
                FileTableModel model = (FileTableModel) jtable.getModel();
                return (Component) model.getValueAt(row, column);
            }
        });

        jTable1.setDefaultEditor(Object.class, new TableCellEditor()
        {

            Component c;

            @Override
            public Component getTableCellEditorComponent(JTable jtable, Object o, boolean bln, int row, int column)
            {
                FileTableModel model = (FileTableModel) jtable.getModel();
                c = (Component) model.getValueAt(row, column);
                return c;
            }

            @Override
            public Object getCellEditorValue()
            {
                return c;
            }

            @Override
            public boolean isCellEditable(EventObject eo)
            {
                if (c instanceof JLabel) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean shouldSelectCell(EventObject eo)
            {
                return false;
            }

            @Override
            public boolean stopCellEditing()
            {
                return true;
            }

            @Override
            public void cancelCellEditing()
            {
            }

            @Override
            public void addCellEditorListener(CellEditorListener cl)
            {
            }

            @Override
            public void removeCellEditorListener(CellEditorListener cl)
            {
            }


        });

        Enumeration e = jTable1.getColumnModel().getColumns();
        int count = 0;
        while (e.hasMoreElements()) {
            TableColumn col = (TableColumn) e.nextElement();

            if (count == 1 || count == 2) {
                col.setPreferredWidth(180);
            }
            if (count == 3) {
                col.setPreferredWidth(180);
            }
            count++;
        }

        if (files != null) {
            for (FileMetadata file : files) {
                fileTableModel.add(file);
            }
        }
    }


    public DateTime getStart(int row)
    {
        DateTimePickerPanel p = ((FileTableModel) jTable1.getModel()).startPickers.get(row);
        JComboBox box = (JComboBox) (((FileTableModel) jTable1.getModel()).getValueAt(row, 3));
        return new DateTime(p.getPicker().getDate(), DateTimeZone.forID(((String) box.getSelectedItem())));
    }

    public DateTime getEnd(int row)
    {
        DateTimePickerPanel p = ((FileTableModel) jTable1.getModel()).endPickers.get(row);
        JComboBox box = (JComboBox) (((FileTableModel) jTable1.getModel()).getValueAt(row, 3));
        return new DateTime(p.getPicker().getDate(), DateTimeZone.forID(((String) box.getSelectedItem())));
    }

    public List<FileMetadata> getFiles()
    {
        return fileTableModel.getFiles();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jTable1.setModel(fileTableModel
        );
        jScrollPane2.setViewportView(jTable1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 769, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2,
                             org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
