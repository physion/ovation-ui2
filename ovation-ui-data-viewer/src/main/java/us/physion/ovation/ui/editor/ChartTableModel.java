/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.awt.Component;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartPanel;

class ChartTableModel extends DefaultTableModel {
  List<ResponsePanel> data;

  public ChartTableModel(List<ResponsePanel> data) {
    this.data = data;
  }
  
  public void setCharts(List<ResponsePanel> charts)
  {
      data = charts;
  }

  public Class<?> getColumnClass(int columnIndex) { return ResponsePanel.class; }
  public int getColumnCount() { return 1; }
  public String getColumnName(int columnIndex) { return ""; }
  public int getRowCount() { return (data == null) ? 0 : data.size(); }
  public Object getValueAt(int rowIndex, int columnIndex) { return data.get(rowIndex).getPanel(); }
  public boolean isCellEditable(int rowIndex, int columnIndex) { return true; }

}
