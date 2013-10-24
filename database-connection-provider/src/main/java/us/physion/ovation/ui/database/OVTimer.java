/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.Timer;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

/**
 *
 * @author jackie
 */
public class OVTimer extends JPanel{
    Timer t;
    int position = 0;
    
    public void start()
    {
        t = new Timer(1000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                spin();
            }
        });
    }
    
    public void stop()
    {
        EventQueueUtilities.runOnEDT(new Runnable() {

            @Override
            public void run() {
                t.stop();
                OVTimer.this.setVisible(false);
                position = 0;
            }
        });
        
    }
    
    private void spin()
    {
        repaint();
        position++;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        boolean paintWhite = false;
        if (position % 4 == position % 8) {
            paintWhite = true;
        }
        for (int quadrant =0; quadrant <= position % 4; quadrant++)
        {
            if (paintWhite)
            {
                paintWhite(g, quadrant);
            }
            else{
                paintGreen(g, quadrant);
            }
        }
    }
    
    private void paintGreen(Graphics g, int position)
    {
        int offset = 10;
        int startAngle = 90*(1 + position) + offset;
        g.setColor(Color.GREEN);
        g.fillArc(0, 0, 100, 100, startAngle, startAngle + 90 - offset);
        g.setColor(Color.WHITE);
        g.fillArc(0, 0, 50, 50, startAngle, startAngle + 90 - offset);
    }
    
    private void paintWhite(Graphics g, int position)
    {
        int offset = 10;
        int startAngle = 90*(1 + position) + offset;
        g.setColor(Color.WHITE);
        g.fillArc(0, 0, 100, 100, startAngle, startAngle + 90 - offset);
    }
    
}
