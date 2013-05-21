/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import com.google.common.collect.Lists;
import ij.ImageJ;
import ij.ImagePlus;
import ij.io.Opener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.imglib2.img.*;
import net.imglib2.io.ImgOpener;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.Measurement;


/**
 *
 * @author huecotanks
 */
public class ImageJVisualization implements Visualization{

    JPanel panel;
    ImageJVisualization(String url)
    {
        url = url.substring("file:".length());
        // open a file with ImageJ
        try {
            //ImageJ ij = new ImageJ();
            //Opener.setOpenUsingPlugins(true);
            final ImagePlus imp = new Opener().openImage(url);
            if (imp != null)
                panel = new BufferedImagePanel(imp.getBufferedImage());
            else{
                panel = new JPanel();
                panel.setBackground(Color.WHITE);
                panel.add(new JLabel("Currently unable to open image type."));
            }
            //ImgPlus ip  = ImgOpener.open(url);
            /*ImageCanvas ic = new ImageCanvas(imp);
panel = new JPanel();
panel.add(ic);
	    */
        } catch (Throwable e) {
            /*try {
ImgPlus ip = ImgOpener.open(url);
// display the dataset
DisplayService displayService = new ImageJ().getService(DisplayService.class);
displayService.getActiveDisplay().display(ip);
} catch (Exception ex){
System.out.println(ex);
}*/
            panel = new JPanel();
            panel.setBackground(Color.WHITE);
            if ( e instanceof java.lang.OutOfMemoryError )
            {
                panel.add(new JLabel("Image too large to open"));
            }else
            {
                panel.add(new JLabel("Unable to open image"));
            }
            System.out.println("Unable to open image at '" + url + "'  \n" + e.getMessage());
        }
    }
    
    @Override
	public Component generatePanel() {
        return panel;
    }

    @Override
	public boolean shouldAdd(Measurement r) {
        return false;
    }

    @Override
	public void add(Measurement r) {
        throw new UnsupportedOperationException("Not supported for this image visualization.");
    }    
}

