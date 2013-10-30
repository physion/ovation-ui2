package us.physion.ovation.ui.editor;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.openide.util.Exceptions;
import us.physion.ovation.domain.mixin.DataElement;


/**
 *
 * @author huecotanks
 */
public class DefaultImageWrapper implements Visualization{

    String name;
    BufferedImage img;
    DefaultImageWrapper(DataElement r)
    {
        InputStream in = null;
        try {
            in = new FileInputStream(r.getData().get());
            img = ImageIO.read(in);
            this.name = r.getName();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex.getLocalizedMessage());
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex.getLocalizedMessage());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex.getLocalizedMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    throw new RuntimeException(ex.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public Component generatePanel() {
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
    
}
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
