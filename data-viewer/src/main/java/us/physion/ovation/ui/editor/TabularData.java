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
package us.physion.ovation.ui.editor;

import java.io.File;
import java.util.List;

public class TabularData {
    final String[] columnNames;
    String[][] tabularData;

    final List<String[]> myEntries;
    private int nextChunkPosition;
    private final int CHUNK_SIZE = 100;
    final File file;

    public TabularData(List<String[]> entries, String[] columnNames, File file) {
        this.myEntries = entries;
        this.columnNames = columnNames;
        this.file = file;
        
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
}
