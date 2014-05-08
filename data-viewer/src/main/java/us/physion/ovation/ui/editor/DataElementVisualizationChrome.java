/*
 * Copyright (C) 2014 Physion LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package us.physion.ovation.ui.editor;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.OvationEntity;

/**
 *
 * @author barry
 */
@Messages({
    "Measurement_Inputs_Label=Sources",
    "AnalsysArtifact_Inputs_Label=Inputs"
})
public class DataElementVisualizationChrome<T extends OvationEntity> extends javax.swing.JPanel {


    /**
     * Creates new form AbstractDataElementVisualizationChrome
     */
    public DataElementVisualizationChrome(JComponent content, JComponent info) {


        initComponents();

        contentPanel.add(content, BorderLayout.CENTER);
        contentPanel.revalidate();

        if (info != null) {
            infoPanel.add(info);
        }

        infoPanel.setVisible(false);
        infoPanelRoot.revalidate();

        infoButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (infoButton.isSelected()) {
                    infoPanel.setVisible(true);
                } else {
                    infoPanel.setVisible(false);
                }

                infoPanelRoot.revalidate();
            }
        });
    }

    private static Area createShape() {
        Area shape = new Area(new RoundRectangle2D.Double(0, 20, 500, 200, 20, 20));

        GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gp.moveTo(230, 20);
        gp.lineTo(250, 0);
        gp.lineTo(270, 20);
        gp.closePath();
        shape.add(new Area(gp));

        return shape;
    }

    public static class WindowMoveAdapter extends MouseAdapter {

        private boolean dragging = false;
        private int prevX = -1;
        private int prevY = -1;

        public WindowMoveAdapter() {
            super();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                dragging = true;
            }
            prevX = e.getXOnScreen();
            prevY = e.getYOnScreen();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (prevX != -1 && prevY != -1 && dragging) {
                Window w = SwingUtilities.getWindowAncestor(e.getComponent());
                if (w != null && w.isShowing()) {
                    Rectangle rect = w.getBounds();
                    w.setBounds(rect.x + (e.getXOnScreen() - prevX),
                            rect.y + (e.getYOnScreen() - prevY), rect.width, rect.height);
                }
            }
            prevX = e.getXOnScreen();
            prevY = e.getYOnScreen();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dragging = false;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPanel = new javax.swing.JPanel();
        infoPanelRoot = new javax.swing.JPanel();
        infoButton = new javax.swing.JToggleButton();
        infoPanel = new javax.swing.JPanel();

        setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        contentPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        contentPanel.setLayout(new java.awt.BorderLayout());

        infoPanelRoot.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        infoPanelRoot.setBorder(new javax.swing.border.LineBorder(javax.swing.UIManager.getDefaults().getColor("InternalFrame.background"), 1, true));

        org.openide.awt.Mnemonics.setLocalizedText(infoButton, org.openide.util.NbBundle.getMessage(DataElementVisualizationChrome.class, "DataElementVisualizationChrome.infoButton.text")); // NOI18N

        infoPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        infoPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout infoPanelRootLayout = new javax.swing.GroupLayout(infoPanelRoot);
        infoPanelRoot.setLayout(infoPanelRootLayout);
        infoPanelRootLayout.setHorizontalGroup(
            infoPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelRootLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(infoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE)
                .addContainerGap())
        );
        infoPanelRootLayout.setVerticalGroup(
            infoPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelRootLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(infoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(infoPanelRoot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoPanelRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JToggleButton infoButton;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JPanel infoPanelRoot;
    // End of variables declaration//GEN-END:variables

    private void BalloonTip(JLabel inputsLabel, String edit_Inputs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
