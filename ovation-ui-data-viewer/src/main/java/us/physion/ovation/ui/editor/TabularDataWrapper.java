/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

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
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.exceptions.OvationException;

public class TabularDataWrapper implements Visualization {

    String[] columnNames;
    String[][] tabularData;

    TabularDataWrapper(){};
    
    TabularDataWrapper(Measurement r) 
    {
        try {
            InputStream in;
            in = new FileInputStream(r.getData().get());
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
            in = new FileInputStream(r.getData().get());
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
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            throw new OvationException(ex.getLocalizedMessage());
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
            throw new OvationException(ex.getLocalizedMessage());
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            throw new OvationException(ex.getLocalizedMessage());
        }
    }

    @Override
    public Component generatePanel() {
        return new TabularDataPanel(tabularData, columnNames);
    }

    @Override
    public boolean shouldAdd(Measurement r) {
        return false;
    }

    @Override
    public void add(Measurement r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
