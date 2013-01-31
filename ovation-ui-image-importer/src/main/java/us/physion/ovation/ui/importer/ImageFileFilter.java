/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author huecotanks
 */
class ImageFileFilter extends FileFilter
{

    public String getDescription()
    {
        return "Image Files for Import";
    }

    public boolean accept(File f)
    {
        return true;
        //return f.isDirectory() || f.getName().endsWith(".tif");
    }

}
