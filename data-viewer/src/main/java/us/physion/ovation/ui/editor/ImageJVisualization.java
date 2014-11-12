package us.physion.ovation.ui.editor;

import com.google.common.collect.Sets;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
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

    File imageFile = null;
    IFormatReader reader = null;

    ImageJVisualization(DataElement d) {
        super(Sets.newHashSet(d));

        try {
            imageFile = d.getData().get();
        } catch (InterruptedException | ExecutionException ex) {
            imageFile = null;
        }

        // open a file with ImageJ
        if (imageFile != null) {

            reader = new ImageReader();
            try {
                reader.setId(imageFile.getCanonicalPath());
            } catch (IOException | FormatException ex) {
                reader = null;
            }
        }

    }

    @Override
    public JComponent generatePanel() {
        JPanel panel = new JPanel();

        if (reader == null) {
            panel.setBackground(Color.WHITE);
            panel.add(new JLabel(Bundle.Unable_to_open_image()));
        }

        try {
            try (BufferedImageReader r = BufferedImageReader.makeBufferedImageReader(reader);) {
                panel = new JPanel();
                int n = (int) Math.ceil(Math.sqrt(r.getImageCount()));
                panel.setLayout(new GridLayout(n, n));

                for (int i = 0; i < n; i++) {
                    JPanel imagePanel = new ImagePanel(imageFile.getName(), new CroppedImagePanel(imageFile.getCanonicalPath(), i));
                    panel.add(imagePanel);
                }
            }
        } catch (OutOfMemoryError e) {
            panel.setBackground(Color.WHITE);
            panel.add(new JLabel(Bundle.Out_of_memory()));

        } catch (IOException | FormatException e) {
            panel.add(new JLabel(Bundle.Unable_to_open_image()));

            if (imageFile != null) {
                panel.add(new JButton(new OpenInNativeAppAction(imageFile)));
                logger.error(Bundle.Unable_to_open_image() + " at " + imageFile.getAbsolutePath(), e);
            } else {
                logger.error(Bundle.Unable_to_open_image(), e);
            }
        }

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

    class CroppedImagePanel extends JPanel {

        private final IFormatReader reader;
        private final int imageNumber;

        CroppedImagePanel(String file, int imageNumber) throws FormatException, IOException {
            this.reader = new ImageReader();
            this.reader.setId(file);
            this.imageNumber = imageNumber;
        }

        Logger logger = LoggerFactory.getLogger(ScaledImagePanel.class);

        @Override
        public void paint(Graphics g) {
            try (BufferedImageReader r = BufferedImageReader.makeBufferedImageReader(reader);) {
                double height = r.getSizeX();
                double width = r.getSizeY();

                if (this.getHeight() < height) {
                    height = this.getHeight();
                    width = height / r.getSizeY() * r.getSizeX();
                }
                if (this.getWidth() < width) {
                    width = this.getWidth();
                    height = width / r.getSizeX() * r.getSizeY();
                }
                int startX = (int) ((this.getWidth() - width) / 2);
                int startY = (int) ((this.getHeight() - height) / 2);

                BufferedImage img = r.openImage(imageNumber, 0, 0, Math.min((int) width, r.getSizeX()), Math.min((int) height, r.getSizeY()));

                g.drawImage(img, startX, Math.min(10, startY), (int) width, (int) height, this);

            } catch (IOException | FormatException e) {
                logger.error(Bundle.Unable_to_open_image(), e);
            }

        }
    }
}
