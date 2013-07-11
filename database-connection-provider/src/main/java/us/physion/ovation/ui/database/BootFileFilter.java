/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;


import java.io.File;
import javax.swing.filechooser.FileFilter;
/**
 *
 * @author huecotanks
 */
class BootFileFilter extends FileFilter {

    public String getDescription() {
        return "Ovation database connection files";
    }

    public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(".connection") || f.getName().endsWith(".boot");
    }
}
