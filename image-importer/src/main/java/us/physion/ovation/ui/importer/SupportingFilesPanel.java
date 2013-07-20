/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import us.physion.ovation.ui.browser.insertion.ListSelectionPanel;

/**
 *
 * @author jackie
 */
public class SupportingFilesPanel extends JPanel {

    private ListSelectionPanel fileList;
    List<URL> urls;
    private JButton chooseFilesButton;
    private ChangeSupport cs;
    File mainImageFile;

    public SupportingFilesPanel(ChangeSupport cs) {
        this.cs = cs;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        fileList = new ListSelectionPanel(cs, "<html>If the image you wish to import spans many files, select the other files here. If you choose a folder, you will be selecting all the files in the folder</html>", "Select supporting files");
        this.add(fileList);
        chooseFilesButton = new JButton("Select supporting files...");
        this.add(chooseFilesButton);
        chooseFilesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.setMultiSelectionEnabled(true);
                int returnVal = chooser.showOpenDialog(new JPanel());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    ArrayList<File> files = new ArrayList<File>();
                    File file = chooser.getSelectedFile();
                    if (file != null) {
                        addFiles(files, file);
                    } else {
                        for (File f : chooser.getSelectedFiles()) {
                            addFiles(files, f);
                        }
                    }
                    setFiles(files);
                }
            }
        });
    }
    private void addFiles(ArrayList<File> fileList, File fileToAdd) {
        if (fileToAdd.equals(mainImageFile))
            return;
        if (fileToAdd.isDirectory())
        {
            //add all the files in the directory
            for (File f : fileToAdd.listFiles())
            {
                addFiles(fileList, f);
            }
        }else{
            fileList.add(fileToAdd);
        }
    }
    private void setFiles(Iterable<File> files) 
    {
        List<URL> fileURLs = new ArrayList();
        List<String> names = new ArrayList();
        for (File file : files) {
            try {
                fileURLs.add(file.toURI().toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            String relativePath = getRelativePath(file, mainImageFile);
            if (relativePath == null)
            //    showMessage
                System.out.println("Supporting files should be in the same folder as the main data file '" + mainImageFile + "' or a subdirectory");
            else
                names.add(relativePath);
        }
        Collections.sort(names);
        urls = fileURLs;
        fileList.setNames(names, names);
    }

    public static String getRelativePath(File file, File mainImageFile) 
    {
        int filenameLength = mainImageFile.getName().length();
        String dirToRemove = mainImageFile.getAbsolutePath().substring(0, (mainImageFile.getAbsolutePath().length() - filenameLength));
        if (file.getAbsolutePath().startsWith(dirToRemove)) {
            return file.getAbsolutePath().substring(dirToRemove.length());
        } else {
            return null;
        }
    }

    void setMainImage(File mainImage) {
        mainImageFile = mainImage;
    }
    
    public List<URL> getFiles()
    {
        return urls;
    }
}
