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

import com.google.common.collect.Sets;
import java.awt.GraphicsDevice;
import static java.awt.GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT;
import static java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import us.physion.ovation.DataContext;
import us.physion.ovation.ui.browser.EntityChildren;
import us.physion.ovation.ui.browser.EntityComparator;
import us.physion.ovation.ui.browser.EntityWrapper;
import us.physion.ovation.ui.browser.FilteredEntityChildren;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 * Dialog for selecting input data elements from AnalysisRecord visualization
 *
 * @author barry
 */
public class SelectDataElementsDialog extends javax.swing.JDialog implements ExplorerManager.Provider, Lookup.Provider {

    private final ExplorerManager explorerManager;
    private final Lookup lookup;
    private boolean success = false;
    private final Set<IEntityWrapper> selectedEntities;

    /**
     * Creates new form SelectDataElementsDialog
     */
    public SelectDataElementsDialog(java.awt.Frame parent,
            boolean modal,
            final Area border) {

        super(parent, modal);

        explorerManager = new ExplorerManager();
        lookup = ExplorerUtils.createLookup(explorerManager, getRootPane().getActionMap());

        selectedEntities = Sets.newHashSet();

        setUndecorated(true);

        if (border != null) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsDevice gd = ge.getDefaultScreenDevice();

            //If shaped windows are supported, set shape else set size
            addComponentListener(new ComponentAdapter() {
                // If the window is resized, the shape is recalculated here.
                @Override
                public void componentResized(ComponentEvent e) {
                    //setSize(border.getBounds().getSize());
                    if (gd.isWindowTranslucencySupported(PERPIXEL_TRANSPARENT)) {

                        Point loc = border.getBounds().getLocation();
                        Area windowBorder = border.createTransformedArea(AffineTransform.getTranslateInstance(-loc.getX(), -loc.getY()));

                        //setShape(windowBorder);
                        setSize(windowBorder.getBounds().getSize());
                    } else {
                        setSize(border.getBounds().getSize());
                    }
                }
            });

            setLocation(border.getBounds().getLocation());

            if (gd.isWindowTranslucencySupported(TRANSLUCENT)) {
                setOpacity(0.9f);
            }
        }

        initComponents();
        initUi();
        
        if(border == null) { // No border, center on screen
            setLocationRelativeTo(null);
        }
    }

    private void initUi() {

        getRootPane().setDefaultButton(addButton);

        addButton.setEnabled(false);

        final Lookup.Result<IEntityWrapper> selectionResult
                = getLookup().lookupResult(IEntityWrapper.class);
        selectionResult.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent le) {
                selectedEntities.clear();
                selectedEntities.addAll(selectionResult.allInstances());
                addButton.setEnabled(selectedEntities.size() > 0);
            }
        });

        BeanTreeView entitiesTree = new BeanTreeView();
        entitiesTree.setRootVisible(false);

        contentScrollPane.setViewportView(entitiesTree);

        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                success = true;
                setVisible(false);
            }
        });

        // Allow ESC to close the dialog
        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };

        getRootPane().registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        EventQueueUtilities.runOffEDT(new Runnable() {

            @Override
            public void run() {
                DataContext ctx = Lookup.getDefault()
                        .lookup(ConnectionProvider.class)
                        .getDefaultContext();

                List<EntityWrapper> wrappers = FilteredEntityChildren.wrap(
                        ctx.getProjects());

                Collections.sort(wrappers, new EntityComparator());

                getExplorerManager().setRootContext(new AbstractNode(new EntityChildren(wrappers)));
            }
        });
    }

    public Iterable<IEntityWrapper> getSelectedEntities() {
        return selectedEntities;
    }

    public boolean isSuccess() {
        return success;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        contentScrollPane = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SelectDataElementsDialog.class, "SelectDataElementsDialog.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentScrollPane)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SelectDataElementsDialog.class, "SelectDataElementsDialog.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(169, Short.MAX_VALUE)
                .addComponent(addButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(addButton))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JScrollPane contentScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }
}
