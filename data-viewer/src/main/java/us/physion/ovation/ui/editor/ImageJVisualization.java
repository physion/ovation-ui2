package us.physion.ovation.ui.editor;

import com.google.common.collect.Sets;
import ij.ImagePlus;
import loci.plugins.BF;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.util.concurrent.ExecutionException;
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

    ImageJVisualization(DataElement d) {
        super(Sets.newHashSet(d));

        File imageFile;
        try {
            imageFile = d.getData().get();
        } catch (InterruptedException | ExecutionException ex) {
            imageFile = null;
        }

        // open a file with ImageJ
        try {
            final ImagePlus[] imps = BF.openImagePlus(imageFile.getAbsolutePath());
            if (imps.length > 0) {
                panel = new JPanel();
                int n = (int) Math.ceil(Math.sqrt(imps.length));
                panel.setLayout(new GridLayout(n, n));
                
                for(ImagePlus imp : imps) {
                    JPanel imagePanel = new ImagePanel(imageFile.getName(), new BufferedImagePanel(imp.getBufferedImage()));
                    panel.add(imagePanel);
                }
            } else {
                panel = new JPanel();
                panel.setBackground(Color.WHITE);
                panel.add(new JLabel(Bundle.Unable_to_open_image()));
            }
        } catch (OutOfMemoryError e) {
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
            panel.add(new JLabel(Bundle.Out_of_memory()));

        } catch (Throwable e) {
            panel.add(new JLabel(Bundle.Unable_to_open_image()));

            if (imageFile != null) {
                panel.add(new JButton(new OpenInNativeAppAction(imageFile)));
                logger.error(Bundle.Unable_to_open_image() + " at " + imageFile.getAbsolutePath(), e);
            } else {
                logger.error(Bundle.Unable_to_open_image(), e);
            }
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
