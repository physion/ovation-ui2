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

    JPanel panel;

    ImageJVisualization(String url) {
        url = url.substring("file:".length());
        // open a file with ImageJ
        try {

            ImageJ context = new ImageJ();
            final IOService ioService = context.getService(IOService.class);
            final Dataset data = ioService.loadDataset(url);

            final ImageDisplay display = new DefaultImageDisplay();
            display.setContext(context);
            display.display(data);

            final SwingImageDisplayViewer displayViewer = new SwingSdiImageDisplayViewer();
            final SwingDisplayWindow displayWindow = new SwingDisplayWindow();
            displayViewer.view(displayWindow, display);
            SwingDisplayPanel displayPanel = displayViewer.getPanel();

            panel = displayPanel;
        } catch (Exception e) {
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

