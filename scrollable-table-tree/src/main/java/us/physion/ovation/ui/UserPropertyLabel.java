/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui;

import java.awt.*;
import javax.swing.JLabel;

/**
 *
 * @author jackie
 */
public class UserPropertyLabel extends JLabel{
    UserPropertyLabel(String label)
    {
        super(label);
    }
    protected void paintComponent(Graphics graphics) {
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
 
                GradientPaint gp = new GradientPaint(0, 0,
                        getBackground().brighter(), 0, getHeight(),
                        getBackground());
 
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
 
                super.paintComponent(graphics);
            }
}
