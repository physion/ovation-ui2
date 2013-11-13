package us.physion.ovation.ui.editor;

import us.physion.ovation.ui.actions.OpenInNativeAppAction;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author huecotanks
 */
public class TabularPanel extends JPanel implements StrictSizePanel {
   
    private static class UnEditableTableModel extends DefaultTableModel {

        private UnEditableTableModel(String[][] tabularData, String[] columnNames) {
            super(tabularData, columnNames);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }
    
    /**
     * Creates new form TabularDataPanel
     */
    
    public TabularPanel(TabularDataWrapper w) {
        this.dataWrapper = w;
        initComponents();
        jTable1.setModel(new UnEditableTableModel(dataWrapper.tabularData, dataWrapper.columnNames));
        
        jTable1.getTableHeader().setBorder(new LineBorder(Color.GRAY));
        //jTable1.getCellRenderer(). return text areas instead of labels
    }
    
    private TabularDataWrapper dataWrapper;
    private JScrollPane jScrollPane1;
    private JTable jTable1;
    private JButton openInExcelButton;
    private JButton nextButton;
    private JButton prevButton;

    private void initComponents() {
        jTable1 = new JTable() {

            @Override
            public boolean getScrollableTracksViewportWidth() {
                if (getPreferredSize().width < getParent().getWidth()) {
                    //fill the scrollpane horizontally
                    return true;
                } else {
                    //force horizontal scroll bars
                    return false;
                }
            }
        };
        jScrollPane1 = new JScrollPane(jTable1);
        jTable1.setGridColor(new java.awt.Color(204, 204, 204));
        
        openInExcelButton = new JButton(new OpenInNativeAppAction(dataWrapper.file));
        
        if (dataWrapper.hasNext()) {
            nextButton = new JButton(">>>");
            prevButton = new JButton("<<<");
            prevButton.setEnabled(false);

            nextButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dataWrapper.next();
                    jTable1.setModel(new UnEditableTableModel(dataWrapper.tabularData, dataWrapper.columnNames));
                    nextButton.setEnabled(dataWrapper.hasNext());
                    prevButton.setEnabled(dataWrapper.hasPrevious());
                }
            });
            prevButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dataWrapper.previous();
                    jTable1.setModel(new UnEditableTableModel(dataWrapper.tabularData, dataWrapper.columnNames));
                    nextButton.setEnabled(dataWrapper.hasNext());
                    prevButton.setEnabled(dataWrapper.hasPrevious());
                }
            });
        }

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(jScrollPane1);
        JPanel buttons = new JPanel();
        buttons.add(Box.createRigidArea(new Dimension(25, 0)));
        buttons.setBackground(Color.WHITE);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.add(openInExcelButton, Component.LEFT_ALIGNMENT);
        buttons.add(Box.createHorizontalGlue());
        if (dataWrapper.hasNext()) {
            buttons.add(prevButton, Component.RIGHT_ALIGNMENT);
            buttons.add(nextButton, Component.RIGHT_ALIGNMENT);
            buttons.add(Box.createRigidArea(new Dimension(25, 0)));
        }
        add(buttons);
    }              


    @Override
    public Dimension getStrictSize() {
        int tableHeight = (jTable1.getRowCount() +1)*jTable1.getRowHeight();
        return new Dimension(getWidth(), tableHeight + 56);
    }
}
