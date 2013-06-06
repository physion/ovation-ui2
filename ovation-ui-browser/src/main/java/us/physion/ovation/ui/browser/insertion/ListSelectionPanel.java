/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;
import org.openide.util.ChangeSupport;
import us.physion.ovation.ui.browser.insertion.ListSelectionPanel.CheckboxTableModel.CheckboxElement;
import us.physion.ovation.ui.interfaces.ZebraTable;

/**
 *
 * @author jackie
 */
public class ListSelectionPanel extends javax.swing.JPanel {

    private CheckboxTableModel tableModel;
    private ChangeSupport cs;
    private String name;

    /**
     * Creates new form ListSelectionPanel
     */
    public ListSelectionPanel(ChangeSupport cs, String label, String name) {
        this.cs = cs;
        initComponents();
        jTable1.setTableHeader(null);
        jLabel1.setText(label);
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public void setNames(List<String> names, Collection<String> selectedNames)
    {
        
        tableModel = new CheckboxTableModel(names, selectedNames);
        jTable1.setModel(tableModel);
        jTable1.setEnabled(true);
        jTable1.setDefaultRenderer(Object.class, tableModel);
        TableCellEditor tce = new TableCellEditor() {

            Component c;
            @Override
            public Component getTableCellEditorComponent(JTable jtable, Object o, boolean bln, int row, int column) {
                TableModel model = jtable.getModel();
                c = ((CheckboxElement) model.getValueAt(row, column)).getPanel();
                return c;
            }

            @Override
            public Object getCellEditorValue() {
                return c;
            }

            @Override
            public boolean isCellEditable(EventObject eo) {
                return true;
            }

            @Override
            public boolean shouldSelectCell(EventObject eo) {
                return true;
            }

            @Override
            public boolean stopCellEditing() {
                return true;
            }

            @Override
            public void cancelCellEditing() {
            }

            @Override
            public void addCellEditorListener(CellEditorListener cl) {
            }
            
            @Override
            public void removeCellEditorListener(CellEditorListener cl) {
            }
        };
        
       jTable1.setCellEditor(tce);
       jTable1.setDefaultEditor(Object.class, tce);
    }
    
    public Set<String> getNames()
    {
        return tableModel.getSelectedNames();
    }

    class CheckboxTableModel extends DefaultTableModel implements TableCellRenderer{

        List<CheckboxElement> elements = new ArrayList();

        CheckboxTableModel(List<String> names, Collection<String> selectedNames) {
            super();
            Object[][] dataVector = new Object[names.size()][1];
            elements = new ArrayList();
            int count = 0;
            for (String n : names) {
                boolean sel = false;
                if (selectedNames == null || selectedNames.contains(n))
                    sel = true;
                CheckboxElement e = new CheckboxElement(n, sel);
                dataVector[count++][0] = e;
                elements.add(e);
            }
            //This is really lame. I added this call, because I had to extend a 
            //DefaultTableModel instead of an AbstractTableModel, to get the
            //setTableEditor functionality working. As an AbstractTableModel,
            //the tableCellEditor I set wasn't being called
            setDataVector(dataVector, new String[]{"Checkbox"});
        }
        
        public Set<String> getSelectedNames()
        {
            Set<String> names = new HashSet();
            for (CheckboxElement e : elements)
            {
                if (e.isSelected())
                    names.add(e.getName());
            }
            return names;
        }

        @Override
        public Component getTableCellRendererComponent(JTable jtable, Object o, boolean bln, boolean bln1, int row, int column) {
            TableModel model = jtable.getModel();
            return ((CheckboxElement)model.getValueAt(row, column)).getPanel();
        }

        class CheckboxElement{

            String name;
            boolean selected = true;
            JPanel p;

            CheckboxElement(String name, boolean select) {
                this.name = name;
                this.selected = select;
                p = createPanel();
            }
            
            JPanel createPanel()
            {
                JPanel panel = new JPanel();
                panel.setEnabled(true);
                panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
                JCheckBox box = new JCheckBox();
                box.setEnabled(true);
                panel.add(box);
                panel.add(new JLabel(name));
                box.setSelected(selected);
                box.addChangeListener(new ChangeListener() {

                    @Override
                    public void stateChanged(ChangeEvent ce) {
                        selected = !selected;
                    }
                });
                return panel;
            }
            
            JPanel getPanel()
            {
                return p;
            }

            boolean isSelected() {
                return selected;
            }
            public String getName()
            {
                return name;
            }
        }
    }
   
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ListSelectionPanel.class, "ListSelectionPanel.jLabel1.text")); // NOI18N

        jScrollPane2.setViewportView(jTable1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}

