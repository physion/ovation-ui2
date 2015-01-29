/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import com.google.common.collect.Maps;
import org.apache.commons.io.FilenameUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.util.PlatformUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.awt.FileDialog.LOAD;
import us.physion.ovation.domain.mixin.AttachmentContainer;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//us.physion.ovation.detailviews//ResourceView//EN",
        autostore = false)
@TopComponent.Description(preferredID = "ResourceViewTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "properties", openAtStartup = true)
@ActionID(category = "Window", id = "us.physion.ovation.detailviews.ResourceViewTopComponent")
@ActionReference(path = "Menu/Window" /*
         * , position = 333
         */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ResourceViewAction",
        preferredID = "ResourceViewTopComponent")
@Messages({
    "CTL_ResourceViewAction=File Attachments",
    "CTL_ResourceViewTopComponent=Attachments",
    "HINT_ResourceViewTopComponent=This window displays the Resource objects associated with the selected Ovation entity"
})
public final class ResourceViewTopComponent extends TopComponent {

    private LookupListener listener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent le) {

            //TODO: we should have some other Interface for things that can update the tags view
            //then we could get rid of the Library dependancy on the Explorer API
            if (TopComponent.getRegistry().getActivated() instanceof ExplorerManager.Provider) {
                //closeEditedResourceFiles();
                updateResources();
            }
        }
    };
    protected Lookup.Result<IEntityWrapper> global;
    protected Collection<? extends IEntityWrapper> entities;
    protected ResourceListModel listModel;
    protected Set<IResourceWrapper> editedSet = new HashSet();
    boolean saveButtonEnabled;

    public ResourceViewTopComponent() {
        initComponents();
        setBackground(Color.white);
        setSavedButtonEnabled(false);
        resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setName(Bundle.CTL_ResourceViewTopComponent());
        setToolTipText(Bundle.HINT_ResourceViewTopComponent());

        global = Utilities.actionsGlobalContext().lookupResult(IEntityWrapper.class);
        global.addLookupListener(listener);

        resourceList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                int index = -1;
                if (evt.getClickCount() == 2 || evt.getClickCount() == 3) {
                    index = list.locationToIndex(evt.getPoint());
                    final IResourceWrapper rw = (IResourceWrapper) listModel.getElementAt(index);
                    EventQueueUtilities.runOffEDT(new Runnable() {
                        @Override
                        public void run() {
                            editResource(rw);
                        }
                    });
                }
            }
        });

        resourceList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {

                for (Object value : resourceList.getSelectedValues()) {
                    if (editedSet.contains(value)) {
                        setSavedButtonEnabled(true);
                        return;
                    }
                }
                setSavedButtonEnabled(false);
            }
        });

        if (PlatformUtils.isMac()) {
            insertResourceButton.putClientProperty("JButton.buttonType", "gradient");
            insertResourceButton.setPreferredSize(new Dimension(34, 34));

            removeResourceButton.putClientProperty("JButton.buttonType", "gradient");
            removeResourceButton.setPreferredSize(new Dimension(34, 34));
            invalidate();
        }
    }

    protected void editResource(IResourceWrapper rw) {
        Resource r = rw.getEntity();
        DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        try {
            Desktop.getDesktop().open(r.getData().get());
        } catch (InterruptedException e) {
            throw new OvationException("Unable to open Resource", e);
        } catch (ExecutionException e) {
            throw new OvationException("Unable to open Resource", e);
        } catch (IOException e) {
            throw new OvationException("Unable to open Resource", e);
        }

        //TODO: enable editing when the API catches up
        /*if (!editedSet.contains(rw)) {
         Resource r = rw.getEntity();
         try{
         r.edit();
         } catch(OvationException e)
         {
         //pass, for now - this can be deleted with ovation version 1.4
         } catch(UnsupportedOperationException e)
         {
         //pass, for now - this can be deleted with ovation version 1.4
         }
         editedSet.add(rw);

         setSavedButtonEnabled(true);
         }*/
    }

    protected void setSavedButtonEnabled(final boolean enable) {
        saveButtonEnabled = enable;
        EventQueueUtilities.runOnEDT(new Runnable() {
            @Override
            public void run() {
                saveButton.setEnabled(enable);
            }
        });
    }

    protected void closeEditedResourceFiles() {
        //TODO: close edited files when API catches up
        /*for (IResourceWrapper rw : editedSet)
         {
         rw.getEntity().releaseLocalFile();
         }
         editedSet = new HashSet();
         setSavedButtonEnabled(false);
         */
    }

    protected void updateResources() {
        entities = global.allInstances();
        EventQueueUtilities.runOffEDT(new Runnable() {
            @Override
            public void run() {
                updateResources(entities);
            }
        });
    }

    protected void updateResources(Collection<? extends IEntityWrapper> entities) {
        List<IResourceWrapper> resources = new LinkedList();
        for (IEntityWrapper e : entities) {
            if (AttachmentContainer.class.isAssignableFrom(e.getType())) {
                AttachmentContainer entity = (AttachmentContainer) e.getEntity();
                for (String name : entity.getResourceNames()) {
                    resources.add(new ResourceWrapper(name, entity.getResource(name)));
                }
            }
        }

        //TODO: remove from edited set
        /*
         LinkedList<IResourceWrapper> toRemove = new LinkedList();
         for (IResourceWrapper rw : editedSet)
         {
         if (!resources.contains(rw))
         {
         toRemove.add(rw);
         }
         }

         //TODO: wrap in a transaction? run on another thread?
         for (IResourceWrapper rw : toRemove)
         {
         editedSet.remove(rw);
         rw.getEntity().releaseLocalFile();
         }
         *
         */

        listModel.setResources(resources);

        if (editedSet.isEmpty()) {
            setSavedButtonEnabled(false);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        resourceList = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        saveButton = new javax.swing.JButton();
        removeResourceButton = new javax.swing.JButton();
        insertResourceButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        listModel = new ResourceListModel();
        resourceList.setModel(listModel);
        jScrollPane1.setViewportView(resourceList);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setBackground(java.awt.Color.white);

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(ResourceViewTopComponent.class, "ResourceViewTopComponent.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeResourceButton, org.openide.util.NbBundle.getMessage(ResourceViewTopComponent.class, "ResourceViewTopComponent.removeResourceButton.text")); // NOI18N
        removeResourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeResourceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(insertResourceButton, org.openide.util.NbBundle.getMessage(ResourceViewTopComponent.class, "ResourceViewTopComponent.insertResourceButton.text")); // NOI18N
        insertResourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertResourceButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(insertResourceButton)
                .addGap(4, 4, 4)
                .addComponent(removeResourceButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(insertResourceButton)
                    .addComponent(removeResourceButton)
                    .addComponent(saveButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        removeResourceButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ResourceViewTopComponent.class, "ResourceViewTopComponent.removeResourceButton.AccessibleContext.accessibleName")); // NOI18N
        removeResourceButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ResourceViewTopComponent.class, "ResourceViewTopComponent.removeResourceButton.AccessibleContext.accessibleDescription")); // NOI18N
        insertResourceButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ResourceViewTopComponent.class, "ResourceViewTopComponent.insertResourceButton.AccessibleContext.accessibleName")); // NOI18N
        insertResourceButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ResourceViewTopComponent.class, "ResourceViewTopComponent.insertResourceButton.AccessibleContext.accessibleDescription")); // NOI18N

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void insertResourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertResourceButtonActionPerformed
        //addButton
        /* Swing JFileChooser
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(new JPanel());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            //add to preferences
            //TODO add a dialog that asks for uti type
            String path = chooser.getSelectedFile().getAbsolutePath();
            addResource(entities, path);
        }
         */

        JFrame mainFrame = (JFrame) WindowManager.getDefault().getMainWindow();

        FileDialog chooser = new FileDialog(mainFrame,
                NbBundle.getMessage(ResourceViewTopComponent.class, "ResourceViewTopComponent.addResourceFileDialog.title"),
                LOAD);

        //chooser.setMultipleMode(false); Java 1.7 only
        chooser.setVisible(true);
        String filename = chooser.getFile();
        if (filename != null) {
            addResource(entities, new File(chooser.getDirectory(), filename).getAbsolutePath());
        }

    }//GEN-LAST:event_insertResourceButtonActionPerformed

    private void removeResourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeResourceButtonActionPerformed
        //Delete selected resources
        EventQueueUtilities.runOffEDT(new Runnable() {
            public void run() {
                removeResources(resourceList.getSelectedValues(), entities);
            }
        });
    }//GEN-LAST:event_removeResourceButtonActionPerformed

    //TODO: save button should work when the API catches up
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        /*final Object[] rws = resourceList.getSelectedValues();
        EventQueueUtilities.runOffEDT(new Runnable() {
            @Override
            public void run() {
                for (Object rw : rws) {
                    Resource r = ((IResourceWrapper) rw).getEntity();
                    if (r.canWrite()) {
                        r.sync();
                    }
                }
            }
        });*/
    }//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton insertResourceButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeResourceButton;
    private javax.swing.JList resourceList;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        this.setSavedButtonEnabled(false);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
        closeEditedResourceFiles();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    //TODO: when API caatches up
    protected void removeResources(Object[] selectedValues, Collection<? extends IEntityWrapper> entities) {
        for (Object o : selectedValues) {
            if (o instanceof IResourceWrapper) {
                String rName = ((IResourceWrapper) o).getName();
                for (IEntityWrapper e : entities) {
                    if (AttachmentContainer.class.isAssignableFrom(e.getType())) {
                        AttachmentContainer eb = (AttachmentContainer) e.getEntity();
                        if (((OvationEntity) eb).canWrite(((OvationEntity) eb).getDataContext().getAuthenticatedUser())) {
                            eb.removeResource(rName);
                        }
                    }
                }
            }
        }
        updateResources(entities);// don't regrab entities from the current TopComponent
    }

    protected boolean saveButtonIsEnabled() {
        return saveButtonEnabled;
    }

    protected List<IResourceWrapper> getResources() {
        return listModel.getResources();
    }

    protected void addResource(Collection<? extends IEntityWrapper> entities, String path) {
        File resourceFile = new File(path);
        String name = resourceFile.getName();

        final Map<String,String> customContentTypes = Maps.newHashMap();
        customContentTypes.put("doc", "application/msword");
        customContentTypes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        customContentTypes.put("xls", "application/vnd.ms-excel");
        customContentTypes.put("xlsx",  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        customContentTypes.put("ppt", "application/vnd.ms-powerpoint");
        customContentTypes.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");


        for (IEntityWrapper e : entities) {
            AttachmentContainer entity = (AttachmentContainer) e.getEntity();
            Resource r;
            try {
                String contentType;
                final String guessedType = URLConnection.guessContentTypeFromName(resourceFile.getName());
                if (guessedType == null) {
                    final String extension = FilenameUtils.getExtension(resourceFile.getName());
                    if (customContentTypes.containsKey(extension)) {
                        contentType = customContentTypes.get(extension);
                    } else {
                        contentType = "application/octet-stream"; // fallback to binary
                    }
                } else {
                    contentType = guessedType;
                }

                r = entity.addResource(name,
                                       resourceFile.toURI().toURL(),
                        contentType);
            } catch (MalformedURLException ex) {
                throw new OvationException("Unable to add Resource", ex);
            }
            listModel.addResource(new ResourceWrapper(name, r));
        }
    }

    private class ResourceListModel extends AbstractListModel {

        List<IResourceWrapper> resources = new LinkedList<IResourceWrapper>();

        public List<IResourceWrapper> getResources() {
            return resources;
        }

        @Override
        public int getSize() {
            return resources.size();
        }

        @Override
        public Object getElementAt(int i) {
            if (i < resources.size()) {
                return resources.get(i);
            }
            return null;
        }

        protected void setResources(List<IResourceWrapper> newResources) {
            int length = Math.max(resources.size(), newResources.size());
            resources = newResources;
            this.fireContentsChanged(this, 0, length);
        }

        protected void addResource(IResourceWrapper resource) {
            resources.add(resource);
            this.fireContentsChanged(this, resources.size(), resources.size());
        }
    };
}
