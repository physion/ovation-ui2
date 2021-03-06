package us.physion.ovation.ui.browser;

import com.google.common.collect.Lists;
import java.util.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import us.physion.ovation.domain.*;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
public class EntityWrapperUtilities {

    public static Node createNode(IEntityWrapper key, EntityChildrenChildFactory cf) {
        return createNewNode(key, cf);
    }

    public static Node createNode(IEntityWrapper key, Children c) {

        boolean forceCreateNode = false;
        if (key instanceof PerUserEntityWrapper) {
            //per user entity wrappers are "user" nodes
            //their uri's correspond to the "User" object they represent, and therefore
            //are not unique nodes
            return createNewNode(key, c);
        }

        Map<String, Node> treeMap = Collections.EMPTY_MAP; // BrowserUtilities.getNodeMap();
        String uri = key.getURI();
        if (treeMap.containsKey(uri)) {
            return new FilterNode(treeMap.get(uri));
        }

        //otherwise, create an AbstractNode representing this object
        EntityNode n = createNewNode(key, c);
//        treeMap.put(uri, n);
        return n;
    }

    public static Node createNewNode(IEntityWrapper key, EntityChildrenChildFactory cf) {
        EntityNode n = new EntityNode(cf, key);
        n.setDisplayName(key.getDisplayName());
        setIconForType(n, key.getType());
        return n;
    }

    public static EntityNode createNewNode(IEntityWrapper key, Children c)
    {
        EntityNode n = new EntityNode(key.isLeaf() ? Children.LEAF : c, Lookups.singleton(key), key);
        n.setDisplayName(key.getDisplayName());
        setIconForType(n, key.getType());
        return n;
    }

    protected static void setIconForType(AbstractNode n, Class entityClass) {
        if (Source.class.isAssignableFrom(entityClass)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/source.png");
        } else if (Project.class.isAssignableFrom(entityClass)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/project.png");
        } else if (Experiment.class.isAssignableFrom(entityClass)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/experiment.png");
        } else if (EpochGroup.class.isAssignableFrom(entityClass)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/epochGroup.png");
        } else if (Epoch.class.isAssignableFrom(entityClass)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/epoch.png");
        } else if (AnalysisRecord.class.isAssignableFrom(entityClass)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/analysis-record.png");
        } else if (User.class.isAssignableFrom(entityClass)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/user.png");
        } else if (Folder.class.isAssignableFrom(entityClass)) {
            n.setIconBaseWithExtension("us/physion/ovation/ui/browser/folder.png");
        }
    }

    public static List<EntityWrapper> wrap(Iterable<? extends OvationEntity> entities)    {
        List<EntityWrapper> wrapped = Lists.newArrayList();
         for (OvationEntity entity : entities)
         {
             wrapped.add(new EntityWrapper(entity));
        }

        Collections.sort(wrapped, new EntityComparator());

         return wrapped;
    }

    public static void wrap(List<EntityWrapper> list, Iterable<? extends OvationEntity> entities) {
        for (OvationEntity entity : entities) {
            list.add(new EntityWrapper(entity));
        }
    }
}
