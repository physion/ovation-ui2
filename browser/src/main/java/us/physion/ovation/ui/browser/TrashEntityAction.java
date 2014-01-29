package us.physion.ovation.ui.browser;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.ResettableNode;

@ActionID(
    category = "Edit",
id = "us.physion.ovation.ui.browser.TrashEntityAction")
@ActionRegistration(
    displayName = "#CTL_TrashEntityAction")
@Messages({
    "CTL_TrashEntityAction=Move to Trash",
    "Progress_MovingToTrash=Moving to Trash",
    "# {0} - delete entity UUID",
    "Deleted=Deleted {0}"
})
public final class TrashEntityAction extends SystemAction {
    private final static Logger log = LoggerFactory.getLogger(TrashEntityAction.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        DataContext c = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        if (c == null) {
            log.warn("Null DataContext");
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();

        List<OvationEntity> entities = Lists.newArrayList(Collections2.transform(Arrays.asList(nodes), new Function<Node, OvationEntity>() {
            @Override
            public OvationEntity apply(Node n) {
                IEntityWrapper wrapper = n.getLookup().lookup(IEntityWrapper.class);

                if (wrapper == null) {
                    return null;
                }
                
                OvationEntity entity = wrapper.getEntity();
                
                if (entity != null && entity.isTrashed()) {
                    return null;
                }

                return entity;
            }
        }));
        int nonNullEntitites = Collections2.filter(entities, Predicates.notNull()).size();

        if (nonNullEntitites == 0) {
            //nothing to delete?
            log.warn("Nothing to delete among the " + nodes.length + " nodes selected");
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        final AtomicInteger maxDelete = new AtomicInteger(nonNullEntitites);
        
        //XXX: Remove this call when trashing will automatically refresh the entity (and entity parents) properly.
        c.getRepository().clear();
        
        final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Progress_MovingToTrash());
        ph.start();

        for (int i = 0; i < nodes.length; i++) {
            final OvationEntity entity = entities.get(i);
            if (entity == null) {
                continue;
            }

            final Node node = nodes[i];

            ListenableFuture<Iterable<UUID>> future = c.trash(entity);
            Futures.addCallback(future, new FutureCallback<Iterable<UUID>>() {
                @Override
                public void onSuccess(Iterable<UUID> v) {
                    if (v.iterator().hasNext()) {
                        ph.progress(Bundle.Deleted(v.iterator().next()));
                    }
                    finish();
                    Node n = node.getParentNode();
                    if (n instanceof ResettableNode) {
                        ((ResettableNode) n).resetNode();
                    }
                }

                private void finish() {
                    if (maxDelete.decrementAndGet() == 0) {
                        ph.finish();
                    }
                }

                @Override
                public void onFailure(Throwable thrwbl) {
                    finish();
                    ErrorManager.getDefault().notify(thrwbl);
                }
            });
        }
    }

    @Override
    public String getName() {
        return Bundle.CTL_TrashEntityAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
