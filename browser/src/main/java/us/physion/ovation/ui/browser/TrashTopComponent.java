/*
 * Copyright (C) 2014 Physion Consulting LLC
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
package us.physion.ovation.ui.browser;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.util.concurrent.ExecutionException;
import javax.swing.Action;
import javax.swing.SwingWorker;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.RefreshableNode;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//us.physion.ovation.ui.browser//Trash//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "TrashTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "us.physion.ovation.ui.browser.TrashTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_TrashAction",
preferredID = "TrashTopComponent")
@Messages({
    "CTL_TrashAction=Trash",
    "CTL_TrashTopComponent=Trash",
    "HINT_TrashTopComponent=Trash",
    "CTL_ReloadTrash=Reload"
})
public final class TrashTopComponent extends TopComponent implements ExplorerManager.Provider {

    private final static Logger log = LoggerFactory.getLogger(TrashTopComponent.class);
    private final ExplorerManager explorerManager = new ExplorerManager();

    static class TopTrashChildren extends Children.Array {

        public TopTrashChildren(Iterable<OvationEntity> roots) {
            super();
            for (OvationEntity e : roots) {
                add(createNode(e));
            }
        }

        private Node[] createNode(final OvationEntity e) {
            final EntityWrapper wrapper = new EntityWrapper(e);
            return new Node[]{
                        new AbstractNode(Children.LEAF, Lookups.singleton(wrapper)) {
                            {
                                setDisplayName(wrapper.getDisplayName());
                                EntityWrapperUtilities.setIconForType(this, wrapper.getType());
                            }

                            @Override
                            public Action[] getActions(boolean context) {
                                return ActionUtils.appendToArray(super.getActions(context), SystemAction.get(UnTrashEntityAction.class));
                            }
                        }
                    };
        }
    }

    class TrashRootNode extends AbstractNode implements RefreshableNode {

        TrashRootNode(Children c) {
            super(c);
        }

        @Override
        public ListenableFuture<Void> refresh() {
            return populateTrash();
        }

        @Override
        public Action[] getActions(boolean context) {
            return ActionUtils.appendToArray(new Action[]{new ResettableAction(this), null}, super.getActions(context));
        }

    }

    public TrashTopComponent() {
        setName(Bundle.CTL_TrashTopComponent());
        setToolTipText(Bundle.HINT_TrashTopComponent());

        Lookup lookup = ExplorerUtils.createLookup(explorerManager, getActionMap());
        associateLookup(lookup);

        final BeanTreeView tree = new BeanTreeView();
        tree.setRootVisible(false);

        setLayout(new BorderLayout());
        add(tree, BorderLayout.CENTER);

        populateTrash();
    }

    @Override
    public Action[] getActions() {
        return ActionUtils.appendToArray(new Action[]{new ResettableAction(this), null}, super.getActions());
    }

    private ListenableFuture<Void> populateTrash() {
        final DataContext c = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();

        if (c == null) {
            //adding this node just to have a RefreshableNode as root
            explorerManager.setRootContext(new TrashRootNode(Children.LEAF));

            log.warn("Null DataContext");
            Toolkit.getDefaultToolkit().beep();
            return Futures.immediateFuture(null);
        }

        final SettableFuture<Void> result = SettableFuture.create();
        new SwingWorker<Iterable<OvationEntity>, Void>() {
            @Override
            protected Iterable<OvationEntity> doInBackground() throws Exception {
                return c.getTrashRoots();
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    result.set(null);
                    return;
                }
                try {
                    Iterable<OvationEntity> roots = get();

                    explorerManager.setRootContext(new TrashRootNode(new TopTrashChildren(roots)));
                    
                    result.set(null);
                } catch (InterruptedException ex) {
                    log.warn("Cannot load trash roots", ex);
                    result.setException(ex);
                } catch (ExecutionException ex) {
                    log.warn("Cannot load trash roots", ex);
                    result.setException(ex);
                }
            }
        }.execute();
        
        return result;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
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
}
