/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.util.Comparator;

/**
 * @author jackie
 */
class FileMetadataComparator implements Comparator<FileMetadata>
{

    public FileMetadataComparator()
    {
    }

    @Override
    public int compare(FileMetadata t, FileMetadata t1)
    {
        if (t.getStart().isAfter(t1.getStart())) {
            return -1;
        }
        if (t.getStart().isEqual(t1.getStart())) {
            return 0;
        }
        return 1;
    }

}
