/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import ij.ImageJ;
import ij.ImagePlus;
import ij.io.Opener;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JPanel;
import net.imglib2.img.*;
import net.imglib2.io.ImgOpener;
import org.openide.util.lookup.ServiceProvider;
import ovation.OvationException;
import ovation.Response;
import ovation.URLResponse;


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
            }
            //ImgPlus ip  = ImgOpener.open(url);
            /*ImageCanvas ic = new ImageCanvas(imp);
panel = new JPanel();
panel.add(ic);
	    */
        } catch (Exception e) {
            /*try {
ImgPlus ip = ImgOpener.open(url);
// display the dataset
DisplayService displayService = new ImageJ().getService(DisplayService.class);
displayService.getActiveDisplay().display(ip);
} catch (Exception ex){
System.out.println(ex);
}*/
            System.out.println(e);
            throw new OvationException(e.getMessage());
        }
    }
    
    @Override
	public Component generatePanel() {
        return panel;
    }

    @Override
	public boolean shouldAdd(Response r) {
        return false;
    }

    @Override
	public void add(Response r) {
        throw new UnsupportedOperationException("Not supported for this image visualization.");
    }    
}

