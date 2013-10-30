package us.physion.ovation.ui.editor;

import java.util.List;
import javax.swing.table.DefaultTableModel;

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
