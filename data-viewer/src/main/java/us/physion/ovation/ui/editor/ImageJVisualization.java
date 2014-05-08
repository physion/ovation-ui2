package us.physion.ovation.ui.editor;

import ij.ImagePlus;
import ij.io.Opener;
import java.awt.Color;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.NbBundle.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.ui.actions.OpenInNativeAppAction;

/**
 *
 * @author huecotanks
 */
@Messages({
    "Unable_to_open_image=Unable to open image",
    "Out_of_memory=Unable to open image (out of memory)"
})
public class ImageJVisualization extends AbstractDataVisualization {

    Logger logger = LoggerFactory.getLogger(ImageJVisualization.class);

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
                panel.add(new JLabel(Bundle.Unable_to_open_image()));
            }
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
                panel.add(new JLabel(Bundle.Out_of_memory()));
            }else
            {
                panel.add(new JLabel(Bundle.Unable_to_open_image()));
            }

            panel.add(new JButton(new OpenInNativeAppAction(f)));
            logger.error(Bundle.Unable_to_open_image() + " at '" + f.getAbsolutePath(), e);
        }
    }

    @Override
    public JComponent generatePanel() {
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

