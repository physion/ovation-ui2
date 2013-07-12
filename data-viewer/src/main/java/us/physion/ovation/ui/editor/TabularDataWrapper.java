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
import java.util.LinkedList;
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
    
    List<String[]> myEntries;
    int nextChunkPosition;
    final int CHUNK_SIZE = 100;
    File file;
    
    TabularDataWrapper(){};
    
    TabularDataWrapper(DataElement r) 
    {
        try {
            file = r.getData().get();
            InputStream in = new FileInputStream(file);

            CSVReader reader = new CSVReader(new FileReader(r.getData().get()));
            myEntries = reader.readAll();
            columnNames = myEntries.remove(0);

            nextChunkPosition = 0;
            next();
            
            int size = Math.min(myEntries.size(), CHUNK_SIZE);
            tabularData = new String[size][columnNames.length];
            for (int lineCount = 0; lineCount<size; lineCount++)
            {
                String[] elements = myEntries.get(lineCount);
                int entryCount = 0;
                for (String entry : elements)
                {
                    tabularData[lineCount][entryCount++] = entry; 
                }
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
    
    public void next()
    {
        int size = Math.min(myEntries.size() - nextChunkPosition, CHUNK_SIZE);
        tabularData = new String[size][columnNames.length];
        for (int lineCount = 0; lineCount < size; lineCount++) {
            String[] elements = myEntries.get(nextChunkPosition + lineCount);
            int entryCount = 0;
            for (String entry : elements) {
                tabularData[lineCount][entryCount++] = entry;
            }
        }
        nextChunkPosition = nextChunkPosition + size;
    }  
    
    public void previous()
    {
        nextChunkPosition = nextChunkPosition - tabularData.length;
        int size = CHUNK_SIZE;
        
        int previousChunkPosition = nextChunkPosition  - size;
        
        tabularData = new String[size][columnNames.length];
        for (int lineCount = 0; lineCount < size; lineCount++) {
            String[] elements = myEntries.get(previousChunkPosition + lineCount);
            int entryCount = 0;
            for (String entry : elements) {
                tabularData[lineCount][entryCount++] = entry;
            }
        }
    }  
    
    public boolean hasNext()
    {
        return (nextChunkPosition < myEntries.size());
    }
    
    public boolean hasPrevious()
    {
        return (nextChunkPosition > CHUNK_SIZE);
    }
    
    @Override
    public Component generatePanel() {
        return new TabularPanel(this);
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
