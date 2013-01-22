/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.Exceptions;
import ovation.Response;
import ovation.URLResponse;


/**
 *
 * @author huecotanks
 */
public class DefaultImageWrapper implements Visualization{

    String name;
    BufferedImage img;
    DefaultImageWrapper(Response r)
    {
        InputStream in = null;
        try {
            if (r instanceof URLResponse)
                in = r.getDataStream();
            else{
                in = new ByteArrayInputStream(r.getDataBytes());
            }
            
            img = ImageIO.read(in);
            this.name = r.getExternalDevice().getName();
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
        ImagePanel p = new ImagePanel(name, pan);
        return p;
    }
    

    @Override
    public boolean shouldAdd(Response r) {
        return false;
    }

    @Override
    public void add(Response r) {
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
