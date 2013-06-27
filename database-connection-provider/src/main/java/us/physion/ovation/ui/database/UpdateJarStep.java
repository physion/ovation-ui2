/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.util.Exceptions;
import us.physion.ovation.ui.interfaces.UpdateStep;

/**
 *
 * @author huecotanks
 */
public class UpdateJarStep implements UpdateStep{

    String jarfile;
    
    UpdateJarStep(String jar)
    {
        jarfile = jar;
    }
    @Override
    public String getStepDescriptor() {
        
        return jarfile;
    }
    
    @Override public int hashCode()
    {
        return getStepDescriptor().hashCode();
    }
    
    @Override public boolean equals(Object o)
    {
        if (o instanceof UpdateJarStep)
            return getStepDescriptor().equals(((UpdateJarStep)o).getStepDescriptor());
        return false;
    }
    
}
