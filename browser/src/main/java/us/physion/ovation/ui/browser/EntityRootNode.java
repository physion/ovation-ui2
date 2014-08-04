package us.physion.ovation.ui.browser;

import javax.swing.Action;
import org.openide.nodes.Children;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EntityRootNode extends EntityNode {

    private final static Logger log = LoggerFactory.getLogger(EntityRootNode.class);
    private final EntityChildrenChildFactory childrenKeysFactory;

    public EntityRootNode(EntityChildrenChildFactory childrenKeysFactory) {
        super(Children.create(childrenKeysFactory, true), null);
        this.childrenKeysFactory = childrenKeysFactory;
    }

    /**
     * This is the whole reason this class exists: there has to be a way to
     * reload the children keys in case a child is deleted.
     */
    @Override
    public void refresh() {
        childrenKeysFactory.refresh();
    }

    @Override
    public Action[] getActions(boolean context) {
        return ActionUtils.appendToArray(super.getActions(context), new Action[]{null, new ResettableAction(this)});
    }
}