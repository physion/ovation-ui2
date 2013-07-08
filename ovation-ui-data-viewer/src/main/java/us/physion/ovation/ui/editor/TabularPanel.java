/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

/**
 *
 * @author huecotanks
 */
public class TabularPanel extends JPanel implements StrictSizePanel {
   
    /**
     * Creates new form TabularDataPanel
     */
    
    public TabularPanel(TabularDataWrapper w) {
        this.dataWrapper = w;
        initComponents();
        jTable1.setModel(new DefaultTableModel(dataWrapper.tabularData, dataWrapper.columnNames));
        
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

        jScrollPane1 = new JScrollPane();
        jTable1 = new JTable();

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 25, 0, 25));

        jTable1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jTable1.setGridColor(new java.awt.Color(204, 204, 204));
        jScrollPane1.setViewportView(jTable1);
        
        openInExcelButton = new JButton("Open in Excel...");
        openInExcelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueueUtilities.runOffEDT(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().edit(dataWrapper.file);
                            } else {
                                throw new OvationException("Java Desktop not supported on this machine");
                            }
                        } catch (IOException ex) {
                            throw new OvationException(ex);
                        }
                    }
                });
            }
        });

        if (dataWrapper.hasNext()) {
            nextButton = new JButton(">>>");
            prevButton = new JButton("<<<");
            prevButton.setEnabled(false);

            nextButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dataWrapper.next();
                    jTable1.setModel(new DefaultTableModel(dataWrapper.tabularData, dataWrapper.columnNames));
                    nextButton.setEnabled(dataWrapper.hasNext());
                    prevButton.setEnabled(dataWrapper.hasPrevious());
                }
            });
            prevButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dataWrapper.previous();
                    jTable1.setModel(new DefaultTableModel(dataWrapper.tabularData, dataWrapper.columnNames));
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
        this.add(buttons);
    }              


    @Override
    public Dimension getStrictSize() {
        int tableHeight = (jTable1.getRowCount() +1)*jTable1.getRowHeight();
        return new Dimension(getWidth(), tableHeight + 56);
    }
}
