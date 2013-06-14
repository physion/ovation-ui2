/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.Lists;
import com.pixelmed.dicom.DicomInputStream;
import java.awt.Component;
import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.exceptions.OvationException;

public class TabularDataWrapper implements Visualization {

    String[] columnNames;
    String[][] tabularData;
    static Logger logger = LoggerFactory.getLogger(TabularDataWrapper.class);

    TabularDataWrapper(){};
    
    TabularDataWrapper(DataElement r) 
    {
        try {
            InputStream in;
            in = new FileInputStream(r.getData().get());

            CSVReader reader = new CSVReader(new FileReader(r.getData().get()));
            List<String[]> myEntries = reader.readAll();
            
            columnNames = myEntries.remove(0);

            tabularData = new String[myEntries.size()][columnNames.length];
            int lineCount = 0;
            for (String[] elements : myEntries)
            {
                int entryCount = 0;
                for (String entry : elements)
                {
                    tabularData[lineCount][entryCount++] = entry; 
                }
                lineCount++;
            }
        } catch (Exception ex) {
            String rId = "";
            if (r != null)
            {
                rId = "'" + r.getUuid() + "'";
            }
            logger.debug("Error parsing tabular data file " + rId + ":" + ex.getLocalizedMessage());
            throw new OvationException(ex);
        }
    }
    
  

    @Override
    public Component generatePanel() {
        return new TabularDataPanel(tabularData, columnNames);
    }

    @Override
    public boolean shouldAdd(DataElement r) {
        return false;
    }

    @Override
    public void add(DataElement r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
