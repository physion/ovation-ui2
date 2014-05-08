package us.physion.ovation.ui.browser;

import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EntityRootNode extends EntityNode {

    private final static Logger log = LoggerFactory.getLogger(EntityRootNode.class);
    private final Callable<List<EntityWrapper>> childrenKeysFactory;

    public EntityRootNode(Callable<List<EntityWrapper>> childrenKeysFactory, TreeFilter filter) {
        super(new EntityChildren(safeCall(childrenKeysFactory), filter), null);
        this.childrenKeysFactory = childrenKeysFactory;
    }

    private static <T> T safeCall(Callable<T> c) {
        try {
            return c.call();
        } catch (Exception e) {
            log.error("Cannot get children keys", e);
            return null;
        }
    }

    /**
     * This is the whole reason this class exists: there has to be a way to
     * reload the children keys in case a child is deleted.
     */
    @Override
    public void refresh() {
        EntityChildren children = (EntityChildren) getChildren();
        children.refreshKeys(); //updateWithKeys(safeCall(childrenKeysFactory));
    }

    @Override
    public Action[] getActions(boolean context) {
        return ActionUtils.appendToArray(super.getActions(context), new Action[]{null, new ResettableAction(this)});
    }
}