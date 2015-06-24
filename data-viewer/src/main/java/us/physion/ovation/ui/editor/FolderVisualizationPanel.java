/*
 * Copyright (C) 2015 Physion LLC
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.domain.Folder;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.ui.browser.BrowserUtilities;
import static us.physion.ovation.ui.editor.AnalysisRecordVisualizationPanel.getResourcesFromEntity;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityNode;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.reveal.api.RevealNode;

/**
 * Folder visualization
 *
 * @author barry
 */
@Messages({
    "Folder_Drop_Files_To_Add_Resources=Drop files to upload",
    "Adding_resources=Adding files...",
    "Folder_New_Analysis_Record_Name=New Analysis"
})
public class FolderVisualizationPanel extends AbstractContainerVisualizationPanel {

    /**
     * Creates new form FolderVisualizationPanel
     */
    public FolderVisualizationPanel(IEntityNode n) {
        super(n);
        initComponents();
        initUI();
    }

    private void initUI() {
        newFolderLink.addActionListener((ActionEvent e) -> {
            addFolder(true);
        });

        fileWell.setDelegate(new FileWell.AbstractDelegate(Bundle.Folder_Drop_Files_To_Add_Resources()) {

            @Override
            public void filesDropped(final File[] files) {
                final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Adding_resources());

                ListenableFuture<OvationEntity> addResources = EventQueueUtilities.runOffEDT(() -> {
                    return  EntityUtilities.insertResources(getFolder(), 
                            files,
                            Lists.newLinkedList(),
                            Lists.newLinkedList());
                }, ph);

                Futures.addCallback(addResources, new FutureCallback<OvationEntity>() {

                    @Override
                    public void onSuccess(final OvationEntity result) {
                        if (result != null) {
                            RevealNode.forEntity(BrowserUtilities.PROJECT_BROWSER_ID, result);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        logger.error("Unable to display added file(s)", t);
                    }
                });
            }
        });

        analysisFileWell.setDelegate(new FileWell.AbstractDelegate(Bundle.Project_Drop_Files_To_Add_Analysis()) {

            @Override
            public void filesDropped(final File[] files) {

                Iterable<Resource> inputElements = showInputsDialog();
                if (inputElements == null) {
                    inputElements = Lists.newArrayList();
                }

                final List<Resource> inputs = Lists.newArrayList(inputElements);

                ListenableFuture<AnalysisRecord> addRecord = EventQueueUtilities.runOffEDT(() -> {
                    return addAnalysisRecord(files, inputs);
                });

                Futures.addCallback(addRecord, new FutureCallback<AnalysisRecord>() {

                    @Override
                    public void onSuccess(final AnalysisRecord ar) {
                        RevealNode.forEntity(BrowserUtilities.PROJECT_BROWSER_ID, ar);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        logger.error("Unable to display AnalysisRecord", t);
                    }
                });
            }
        });
    }

    private Iterable<Resource> showInputsDialog() {
        SelectResourcesDialog addDialog = new SelectResourcesDialog((JFrame) SwingUtilities.getRoot(this),
                true,
                null);

        addDialog.setVisible(true);

        List<Resource> result = Lists.newArrayList();
        if (addDialog.isSuccess()) {
            for (IEntityWrapper entityWrapper : addDialog.getSelectedEntities()) {
                for (Resource entity : getResourcesFromEntity(entityWrapper.getEntity())) {
                    result.add(entity);
                }
            }

            System.out.println(Sets.newHashSet(addDialog.getSelectedEntities()));
        } else {
            result = null;
        }

        addDialog.dispose();

        return result;
    }

    private AnalysisRecord addAnalysisRecord(final File[] files, final Iterable<Resource> inputs) {
        AnalysisRecord ar = getFolder().addAnalysisRecord(Bundle.Folder_New_Analysis_Record_Name(),
                inputs,
                null,
                Maps.<String, Object>newHashMap());

        final Set<String> outputNames = Sets.newHashSet(ar.getOutputs().keySet());

        for (File f : files) {
            String name1 = f.getName();
            int i = 1;
            while (outputNames.contains(name1)) {
                name1 = name1 + "_" + i++;
            }
            try {
                ar.addOutput(name1, f.toURI().toURL(), ContentTypes.getContentType(f));
                outputNames.add(name1);
            } catch (MalformedURLException ex) {
                logger.error("Unable to determine file URL", ex);
                Toolkit.getDefaultToolkit().beep();
            } catch (IOException ex) {
                logger.error("Unable to determine file content type", ex);
                Toolkit.getDefaultToolkit().beep();
            }
        }
        return ar;

    }

    private Folder addFolder(boolean reveal) {
        final Folder folder = getFolder().addFolder(Bundle.Default_Folder_Label());
        if (reveal) {
            node.refresh();
            RevealNode.forEntity(BrowserUtilities.PROJECT_BROWSER_ID, folder);
        }

        return folder;
    }

    public Folder getFolder() {
        return getNode().getEntity(Folder.class);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        jLabel1 = new javax.swing.JLabel();
        folderLabel = new javax.swing.JTextField();
        fileWellPanel = new javax.swing.JPanel();
        fileWell = new us.physion.ovation.ui.editor.FileWell();
        analysisFileWell = new us.physion.ovation.ui.editor.FileWell();
        newFolderLink = new org.jdesktop.swingx.JXHyperlink();

        setBackground(java.awt.Color.white);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FolderVisualizationPanel.class, "FolderVisualizationPanel.jLabel1.text")); // NOI18N

        folderLabel.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${folder.label}"), folderLabel, org.jdesktop.beansbinding.BeanProperty.create("text_ON_ACTION_OR_FOCUS_LOST"));
        bindingGroup.addBinding(binding);

        fileWellPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.select"));
        fileWellPanel.setLayout(new java.awt.GridLayout());

        fileWell.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        fileWellPanel.add(fileWell);

        analysisFileWell.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        fileWellPanel.add(analysisFileWell);

        org.openide.awt.Mnemonics.setLocalizedText(newFolderLink, org.openide.util.NbBundle.getMessage(FolderVisualizationPanel.class, "FolderVisualizationPanel.newFolderLink.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileWellPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(folderLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newFolderLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(folderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(newFolderLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(fileWellPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                .addContainerGap())
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private us.physion.ovation.ui.editor.FileWell analysisFileWell;
    private us.physion.ovation.ui.editor.FileWell fileWell;
    private javax.swing.JPanel fileWellPanel;
    private javax.swing.JTextField folderLabel;
    private javax.swing.JLabel jLabel1;
    private org.jdesktop.swingx.JXHyperlink newFolderLink;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
