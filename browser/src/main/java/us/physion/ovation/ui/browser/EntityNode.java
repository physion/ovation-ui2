package us.physion.ovation.ui.browser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.swing.Action;
import org.openide.actions.RenameAction;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import us.physion.ovation.domain.*;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.ui.actions.OpenInSeparateViewAction;
import us.physion.ovation.ui.actions.RevealElementAction;
import static us.physion.ovation.ui.browser.ActionUtils.appendToArray;
import us.physion.ovation.ui.interfaces.*;

/**
 *
 * @author huecotanks
 */
public class EntityNode extends AbstractNode implements ResettableNode, URINode {

    private Action[] actionList;
    private IEntityWrapper parent;
    private static Map<String, Class> insertableMap = createMap();
    private URI uri;
    
    private static class URITreePathProviderImpl implements URITreePathProvider {

        private EntityNode delegate;

        public void setDelegate(EntityNode delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<URI> getTreePath() {
            if (delegate != null) {
                return delegate.buildURITreePath();
            } else {
                return Collections.EMPTY_LIST;
            }
        }
    }
        
    public EntityNode(Children c, Lookup l, IEntityWrapper parent) {
        this(c, l, new URITreePathProviderImpl(), parent);
    }
    
    private EntityNode(Children c, Lookup l, URITreePathProviderImpl pathProvider, IEntityWrapper parent) {
        super (c, new ProxyLookup(l, Lookups.singleton(pathProvider)));
        
        pathProvider.setDelegate(this);
        this.parent = parent;
        loadURI();
    }

    @Override
    public boolean canRename() {
        return parent != null ? parent.canRename() : super.canRename();
    }

    @Override
    public void setName(String s) {
        super.setName(s);
        if (parent != null) {
            String oldDisplay = getDisplayName();

            parent.setName(s);

            setDisplayName(parent.getDisplayName());
            fireDisplayNameChange(oldDisplay, getDisplayName());
        }
    }

    @Override
    public String getName() {
        return parent != null ? parent.getName() : super.getName();
    }
  
   public EntityNode(Children c, IEntityWrapper parent)
   {
       super(c);
       this.parent = parent;
       loadURI();
   }
   
   private void loadURI() {
       try {
           this.uri = (parent != null && parent.getURI() != null) ? new URI(parent.getURI()) : null;
       } catch (URISyntaxException ex) {
           //XXX: Log?
           this.uri = null;
       }
   }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public List<URI> getFilteredParentURIs() {
        return parent == null ? Collections.EMPTY_LIST : parent.getFilteredParentURIs();
    }
    
    private List<URI> buildURITreePath() {
        List<URI> paths = new ArrayList<URI>();

        Node n = this;
        while (n != null) {
            if (n instanceof URINode) {
                //put in reverse
                paths.add(((URINode) n).getURI());
                
                paths.addAll(((URINode) n).getFilteredParentURIs());
            }
            n = n.getParentNode();
        }

        Collections.reverse(paths);

        return paths;
    }

   @Override
   public void resetNode()
   {
       Children c = getChildren();
       if (c == null || this.isLeaf())
           return;
       if (c instanceof EntityChildren)
       {
           ((EntityChildren)c).initKeys();
       }
   }
   
   protected void setActionList(Action[] actions)
   {
       actionList = actions;
   }

    @Override
    public Action getPreferredAction() {
        if (!DataElement.class.isAssignableFrom(parent.getType())) {
            return super.getPreferredAction();
        }
        
        DataElement data = (DataElement) parent.getEntity();
        return new OpenInSeparateViewAction(data, buildURITreePath());
    }
    
   @Override
    public Action[] getActions(boolean popup) {
       if (actionList == null)
       {
           if (parent == null)// root node
           {
               Collection<? extends RootInsertable> insertables = Lookup.getDefault().lookupAll(RootInsertable.class);
               List<RootInsertable> l = new ArrayList(insertables);
               Collections.sort(l);
               actionList = l.toArray(new RootInsertable[l.size()]);
           }
           else{
               Class entityClass = parent.getType();
               Class insertableClass = insertableMap.get(entityClass.getSimpleName());
               if (insertableClass == null)
               {
                   actionList = new Action[0];
               } else {
                   Collection insertables = Lookup.getDefault().lookupAll(insertableClass);
                   List<? extends Comparable> l = new ArrayList(insertables);
                   Collections.sort(l);
                   actionList = l.toArray(new EntityInsertable[l.size()]);
               }
               
               if(DataElement.class.isAssignableFrom(entityClass)){
                   actionList = appendToArray(actionList, new RevealElementAction((DataElement) parent.getEntity()));
               }
               
               if(AnalysisRecord.class.isAssignableFrom(entityClass)) {
                   actionList = appendToArray(actionList, SystemAction.get(AnalysisRecordInputsAction.class));
               }
               
               if(Epoch.class.isAssignableFrom(entityClass)) {
                   actionList = appendToArray(actionList, SystemAction.get(EpochInputSourcesAction.class));
               }
               
               if(Measurement.class.isAssignableFrom(entityClass)) {
                   actionList = appendToArray(actionList, SystemAction.get(MeasurementInputSourcesAction.class));
               }
               
               //XXX: right now canRename will never change for a given node so it's safe to use it during initialization
               if (canRename()) {
                   actionList = appendToArray(actionList, SystemAction.get(RenameAction.class));
               }
               
               if(OvationEntity.class.isAssignableFrom(entityClass)){
                   actionList = appendToArray(actionList, null, SystemAction.get(TrashEntityAction.class));
               }
               
           }
       }
        return actionList;
    }
   
    private static Map<String, Class> createMap() {
        Map<String, Class> insertables = new HashMap<String, Class>();
        insertables.put(Project.class.getSimpleName(), ProjectInsertable.class);
        insertables.put(Source.class.getSimpleName(), SourceInsertable.class);
        insertables.put(Experiment.class.getSimpleName(), ExperimentInsertable.class);
        insertables.put(EpochGroup.class.getSimpleName(), EpochGroupInsertable.class);
        insertables.put(Epoch.class.getSimpleName(), EpochInsertable.class);
        insertables.put(Measurement.class.getSimpleName(), ResponseInsertable.class);
        insertables.put(AnalysisRecord.class.getSimpleName(), AnalysisRecordInsertable.class);
        return insertables;
    }
}
