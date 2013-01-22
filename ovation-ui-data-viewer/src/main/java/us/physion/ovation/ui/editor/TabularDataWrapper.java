/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import com.pixelmed.dicom.DicomInputStream;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import ovation.Response;
import ovation.URLResponse;

public class TabularDataWrapper implements Visualization {

    String[] columnNames;
    String[][] tabularData;

    TabularDataWrapper(){};
    
    TabularDataWrapper(Response r) 
    {
        InputStream in;
        if (r instanceof URLResponse) {
            in = r.getDataStream();
        } else {
            in = new ByteArrayInputStream(r.getDataBytes());
        }
        Scanner s = new Scanner(in, "UTF-8");
        if (!s.hasNextLine())
        {
            throw new RuntimeException("Empty response data!");
        }
        String line = s.nextLine();
        columnNames = line.split(",");
        int lineCount=0; 
        while (s.hasNextLine())
        {
            line = s.nextLine();
            if (line.charAt(0) == '#') {
                continue;
            }
            lineCount++;
        }
        
        tabularData = new String[lineCount][columnNames.length];
        if (r instanceof URLResponse) {
            in = r.getDataStream();
        } else {
            in = new ByteArrayInputStream(r.getDataBytes());
        }
        s = new Scanner(in, "UTF-8");
        s.nextLine();
        lineCount = 0;
        while (s.hasNextLine())
        {
            line = s.nextLine();
            if (line.charAt(0) == '#') {
                continue;
            }
            String[] values = line.split(",");
            for (int i=0; i<columnNames.length; ++i)
            {
                tabularData[lineCount][i] = values[i];
            }
            lineCount++;
        }
    }

    @Override
    public Component generatePanel() {
        return new TabularDataPanel(tabularData, columnNames);
    }

    @Override
    public boolean shouldAdd(Response r) {
        return false;
    }

    @Override
    public void add(Response r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
