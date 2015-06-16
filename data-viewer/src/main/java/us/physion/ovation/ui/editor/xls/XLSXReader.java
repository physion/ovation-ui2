/*
 * Copyright (C) 2014 Physion LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package us.physion.ovation.ui.editor.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.ui.editor.TabularData;
import us.physion.ovation.ui.editor.TabularPanel;

public abstract class XLSXReader {
    private final static Logger log = LoggerFactory.getLogger(XLSXReader.class);

    protected abstract void addSheet(String sheetName, JComponent c);
    
    public interface LoadHandler {

        void handle(String sheetName, TabularData data);
    }
    
    public static void load(File f, LoadHandler handler) throws IOException{
            try (FileInputStream fis = new FileInputStream(f)) {
                XSSFWorkbook workbook = new XSSFWorkbook(fis); // for xls HSSFWorkbook

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    List<String[]> entries = new ArrayList<>();

                    Sheet sheet = workbook.getSheetAt(i);
                    for (Row row : sheet) {
                        List<String> rowData = new ArrayList<>();
                        for (Iterator<Cell> cells = row.cellIterator(); cells.hasNext();) {
                            Cell cell = cells.next();

                            switch (cell.getCellType()) {
                                case Cell.CELL_TYPE_STRING:
                                    rowData.add(cell.getStringCellValue().trim());
                                    break;
                                case Cell.CELL_TYPE_NUMERIC:
                                    rowData.add(Double.toString(cell.getNumericCellValue()));
                                    break;
                            }
                        }
                        entries.add(rowData.toArray(new String[0]));
                    }

                    int columnCount = 0;
                    for (String[] row : entries) {
                        columnCount = Math.max(columnCount, row.length);
                    }

                    entries = reallocEntries(entries, columnCount);

                    TabularData data = new TabularData(entries, getColumnNames(columnCount), f);
                    
                    handler.handle(sheet.getSheetName(), data);
                }
            }
    }
            
    public void readAll(File f) {
        try {
            load(f, (String sheetName, TabularData data) -> addSheet(sheetName, new TabularPanel(data)));
        } catch (IOException e) {
            log.warn("Error loading XLSX file", e);
        }
    }

    private static List<String[]> reallocEntries(List<String[]> input, int columnCount) {
        List<String[]> entries = new ArrayList<>();

        for (String[] row : input) {
            String[] newRow = new String[columnCount];
            System.arraycopy(row, 0, newRow, 0, row.length);
            
            entries.add(newRow);
        }

        return entries;
    }

    private static String[] getColumnNames(int columnCount) {
        String[] names = new String[columnCount];

        for (int i = 0; i < columnCount; i++) {
            names[i] = getStringBase(i, 'z' - 'a' + 1); //NOI18N
        }

        return names;
    }

    //A-Z, AA-ZZ, etc.
    private static String getStringBase(int n, int base) {
        if (n == 0) {
            return "A"; //NOI18N
        }

        StringBuilder sb = new StringBuilder();
        while (n >= 0) {
            int digit = n % base;
            sb.insert(0, (char) ('A' + digit)); //NOI18N
            n = n / base - 1;
        }

        return sb.toString();
    }

}
