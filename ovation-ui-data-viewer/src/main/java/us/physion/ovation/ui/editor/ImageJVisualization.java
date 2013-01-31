/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.DefaultImageDisplay;
import imagej.data.display.ImageDisplay;
import imagej.io.IOService;
import imagej.ui.swing.mdi.viewer.SwingMdiImageDisplayViewer;
import imagej.ui.swing.sdi.viewer.SwingDisplayWindow;
import imagej.ui.swing.sdi.viewer.SwingSdiImageDisplayViewer;
import imagej.ui.swing.viewer.image.SwingDisplayPanel;
import imagej.ui.swing.viewer.image.SwingImageDisplayViewer;
import ovation.OvationException;
import ovation.Response;

import javax.swing.*;
import java.awt.*;

//import ij.ImagePlus;
//import ij.io.Opener;


/**
 * @author huecotanks
 */
public class ImageJVisualization implements Visualization {

    final JPanel panel;

    final IOService ioService;
    final Dataset data;
    final ImageDisplay display;
    final SwingImageDisplayViewer displayViewer;
    final SwingDisplayWindow displayWindow;
    final ImageJ context;
    
    ImageJVisualization(String url) {
        url = url.substring("file:".length());
        // open a file with ImageJ
        try {
            context = new ImageJ();

            ioService = context.getService(IOService.class);
            data = ioService.loadDataset(url);

            display = new DefaultImageDisplay();
            display.setContext(context);
            display.display(data);
            
            displayViewer = new SwingMdiImageDisplayViewer();
            displayWindow = new SwingDisplayWindow();
            
            displayViewer.setContext(context);
            displayViewer.view(displayWindow, display);
            
            panel = displayViewer.getPanel();
        } catch (Throwable e) {
            System.out.println(e);
            throw new OvationException("Unable to open image", e);
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
    public boolean shouldAdd(Response r) {
        return false;
    }

    @Override
    public void add(Response r) {
        throw new UnsupportedOperationException("Not supported for this image visualization.");
    }
}

