package us.physion.ovation.ui.editor;

import com.google.common.collect.Sets;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.mixin.DataElement;


/**
 *
 * @author huecotanks
 */
public class DefaultImageWrapper extends AbstractDataVisualization {
    private final static Logger log = LoggerFactory.getLogger(DefaultImageWrapper.class);

    String name;
    BufferedImage img;
    final DataElement entity;
    DefaultImageWrapper(DataElement r)
    {
        entity = r;
        InputStream in = null;
        try {
            in = new FileInputStream(r.getData().get());
            img = ImageIO.read(in);
            this.name = r.getName();
        } catch (InterruptedException ex) {
            log.info("", ex);
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
        } catch (ExecutionException ex) {
            log.info("", ex);
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    log.warn("", ex);
                    throw new RuntimeException(ex.getLocalizedMessage(), ex);
                }
            }
        }
    }

    @Override
    public JComponent generatePanel() {
        BufferedImagePanel pan = new BufferedImagePanel(img);
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
/**
 * <b>Note</b>: This component is not opaque. Use with an opaque container.
 */
class BufferedImagePanel extends JPanel
{
    BufferedImage img;
    BufferedImagePanel(BufferedImage buf)
    {
        img = buf;
    }

    @Override
    public void paint(Graphics g)
    {
        double height = img.getHeight();
        double width = img.getWidth();
        if (this.getHeight() < height)
        {
            height = this.getHeight();
            width = height/img.getHeight()*img.getWidth();
        }
        if (this.getWidth() < width)
        {
            width = this.getWidth();
            height = width/img.getWidth()*img.getHeight();
        }
        int startX = (int)((this.getWidth() - width)/2);
        int startY = (int)((this.getHeight() - height)/2);
        g.drawImage(img, startX, Math.min(10, startY), (int)width, (int)height, this);
    }
}
