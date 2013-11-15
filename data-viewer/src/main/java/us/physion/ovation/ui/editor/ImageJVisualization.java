package us.physion.ovation.ui.editor;

import us.physion.ovation.ui.actions.OpenInNativeAppAction;
import ij.ImagePlus;
import ij.io.Opener;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.physion.ovation.domain.mixin.DataElement;

/**
 *
 * @author huecotanks
 */
public class ImageJVisualization implements Visualization{

    JPanel panel;
    ImageJVisualization(File f)
    {
        // open a file with ImageJ
        try {
            final ImagePlus imp = new Opener().openImage(f.getAbsolutePath());
            if (imp != null) {
                panel = new ImagePanel(f.getName(), new BufferedImagePanel(imp.getBufferedImage()));
            } else {
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
            
            panel.add(new JButton(new OpenInNativeAppAction(f)));
            System.out.println("Unable to open image at '" + f.getAbsolutePath() + "'  \n" + e.getMessage());
        }
    }
    
    @Override
	public Component generatePanel() {
        return panel;
    }

    @Override
	public boolean shouldAdd(DataElement r) {
        return false;
    }

    @Override
	public void add(DataElement r) {
        throw new UnsupportedOperationException("Not supported for this image visualization.");
    }    
}

