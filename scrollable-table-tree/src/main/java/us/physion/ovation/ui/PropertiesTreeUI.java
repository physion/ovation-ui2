/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author huecotanks
 */
public class PropertiesTreeUI extends BasicTreeUI{
    
    JScrollPane window;
    //int cellWidth = 0;
    PropertiesTreeUI(JScrollPane window)
    {
        this.window = window;
    }

    public int getCellWidth()
    {
        return window.getWidth() - 24;//2*getRowX(1, 2);
        /*cellWidth = cellWidth == 0 ? window.getViewport().getWidth()-12 : cellWidth;
        return cellWidth;
        */
    }
    
    @Override
    protected AbstractLayoutCache.NodeDimensions createNodeDimensions() {
        return new NodeDimensionsHandler() {

            @Override
            public Rectangle getNodeDimensions(
                    Object value, int row, int depth, boolean expanded,
                    Rectangle size) {
                Rectangle dimensions = super.getNodeDimensions(value, row,
                        depth, expanded, size);
                
                dimensions.width = window.getWidth() - 24;//window.getViewport().getWidth() - 2*getRowX(row, depth); 
                if (depth == 1)
                    dimensions.width -= (getRowX(row, depth) - 4);//+= getRowX(row, depth);//
                //cellWidth = dimensions.width;
                
                if (value instanceof TableNode)
                {
                    int height = ((TableNode)value).getHeight();
                    if (height > 0)
                        dimensions.height = height;
                }
                return dimensions;
            }

            @Override
            protected int getRowX(int row, int depth) {
                if (depth == 2) {
                    return 4;
                }

                return super.getRowX(row, depth);
            }
        };
    }

    @Override
    protected void paintHorizontalLine(Graphics g, JComponent c,
            int y, int left, int right) {
        // do nothing.
    }

    @Override
    protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds,
            Insets insets, TreePath path) {
        // do nothing.
    }
}
