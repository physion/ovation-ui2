package us.physion.ovation.ui.editor;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.exceptions.OvationException;

/**
 *
 * @author huecotanks
 */
public class DefaultImageWrapper extends AbstractDataVisualization {

    private final static Logger log = LoggerFactory.getLogger(DefaultImageWrapper.class);

    final String name;

    final DataElement entity;

    DefaultImageWrapper(DataElement r) {
        entity = r;
        this.name = r.getName();
    }

    @Override
    public JComponent generatePanel() {
        ScaledImagePanel pan = new ScaledImagePanel(entity.getData());

        pan.setAlignmentX(Component.CENTER_ALIGNMENT);
        return new ImagePanel(name, pan);
    }

    @Override
    public boolean shouldAdd(DataElement r) {
        return false;
    }

    @Override
    public void add(DataElement r) {
        throw new UnsupportedOperationException("Images are currently implemented one per panel");
    }

    @Override
    public Iterable<? extends OvationEntity> getEntities() {
        return Sets.newHashSet(entity);
    }

}

class ScaledImagePanel extends JPanel {

    final ListenableFuture<File> imgFile;

    ScaledImagePanel(ListenableFuture<File> imgFile) {
        this.imgFile = imgFile;
    }

    Logger logger = LoggerFactory.getLogger(ScaledImagePanel.class);
    
    @Override
    public void paint(Graphics g) {
        try {
            BufferedImage scaledImg = createImage(imgFile.get(),
                    getHeight(), getWidth());

            double height = scaledImg.getHeight();
            double width = scaledImg.getWidth();
            if (this.getHeight() < height) {
                height = this.getHeight();
                width = height / scaledImg.getHeight() * scaledImg.getWidth();
            }
            if (this.getWidth() < width) {
                width = this.getWidth();
                height = width / scaledImg.getWidth() * scaledImg.getHeight();
            }
            int startX = (int) ((this.getWidth() - width) / 2);
            int startY = (int) ((this.getHeight() - height) / 2);
            g.drawImage(scaledImg, startX, Math.min(10, startY), (int) width, (int) height, this);
        } catch (OvationException ex) {
            logger.error("Unable to load image", ex);
        } catch (IOException ex) {
            logger.error("Unable to load image", ex);
        } catch (InterruptedException ex) {
            logger.error("Image download interrupted", ex);
        } catch (ExecutionException ex) {
            logger.error("Error getting image file", ex);
        }
    }

    BufferedImage img = null;

    BufferedImage createImage(File f, int h, int w) throws IOException {
        if (img != null
                && img.getWidth() == getWidth()
                && img.getHeight() == getHeight()) {

            return img;
        }

        ImageInputStream iis = null;

        try {
            iis = ImageIO.createImageInputStream(f);
            Iterator iter = ImageIO.getImageReaders(iis);
            if (!iter.hasNext()) {
                throw new OvationException("Unable to load image. No ImageIO readers available.");
            }

            ImageReader reader = (ImageReader) iter.next();

            reader.setInput(iis);
            
            float scaleW = reader.getWidth(0) / ((float) w);
            float scaleH = reader.getHeight(0) / ((float) h);

            ImageReadParam params = reader.getDefaultReadParam();

            if (scaleW > 1 || scaleH > 1) {
                params.setSourceSubsampling(scaleW > 1 ? Math.round(scaleW) : 1,
                        scaleH > 1 ? Math.round(scaleH) : 1,
                        0, 0);
            }

            img = reader.read(0, params);

            return img;

        } catch (IOException ex) {
            throw new OvationException("Unable to load image", ex);
        } finally {
            if (iis != null) {
                iis.close();
            }
        }
    }
}

/**
 * <b>Note</b>: This component is not opaque. Use with an opaque container.
 */
class BufferedImagePanel extends JPanel {

    BufferedImage img;

    BufferedImagePanel(BufferedImage buf) {
        img = buf;
    }

    @Override
    public void paint(Graphics g) {
        double height = img.getHeight();
        double width = img.getWidth();
        if (this.getHeight() < height) {
            height = this.getHeight();
            width = height / img.getHeight() * img.getWidth();
        }
        if (this.getWidth() < width) {
            width = this.getWidth();
            height = width / img.getWidth() * img.getHeight();
        }
        int startX = (int) ((this.getWidth() - width) / 2);
        int startY = (int) ((this.getHeight() - height) / 2);
        g.drawImage(img, startX, Math.min(10, startY), (int) width, (int) height, this);
    }
}
