/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.DatasetView;
import imagej.data.display.DefaultImageDisplay;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.util.awt.AWTImageTools;
import imagej.io.IOService;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;

import ovation.Response;

import javax.swing.*;
import java.awt.image.BufferedImage;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import ovation.OvationException;

/**
 * @author huecotanks
 */
public class ImageJVisualization implements Visualization {

    final JPanel panel;
    final IOService ioService;
    final Dataset data;
    final ImageJ context;

    ImageJVisualization(String url, String name) {
        url = url.substring("file:".length());
        // open a file with ImageJ
        try {
            context = new ImageJ();

            ioService = context.getService(IOService.class);
            data = ioService.loadDataset(url);

            final ImageDisplay display = new DefaultImageDisplay();
            display.setContext(context);
            display.display(data);
            final ImageDisplayService imageDisplayService =
                    display.getContext().getService(ImageDisplayService.class);
            final DatasetView datasetView =
                    imageDisplayService.getActiveDatasetView(display);
            final ARGBScreenImage screenImage = datasetView.getScreenImage();
            final Image pixels = screenImage.image();

            final BufferedImage bufferedImage = AWTImageTools.makeBuffered(pixels);
            final BufferedImagePanel imgPanel = new BufferedImagePanel(bufferedImage);

            this.panel = new ImagePanel(name, imgPanel);

        } catch (Throwable e) {
            /*
             result = new JPanel();
             result.setBackground(Color.WHITE);

             if (e instanceof java.lang.OutOfMemoryError) {
             result.add(new JLabel("Image too large to open"));
             } else {
             result.add(new JLabel("Unable to open image"));
             }
             */

            throw new OvationException("Unable to open image", e);
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
