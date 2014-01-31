package us.physion.ovation.ui.browser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.swing.Action;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import us.physion.ovation.domain.*;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.ui.actions.OpenInSeparateViewAction;
import us.physion.ovation.ui.actions.RevealElementAction;
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
        
    public EntityNode(Children c, Lookup l, IEntityWrapper parent) {
        super (c, l);
        this.parent = parent;
        loadURI();
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
               
               if(OvationEntity.class.isAssignableFrom(entityClass)){
                   actionList = appendToArray(actionList, null, SystemAction.get(TrashEntityAction.class));
               }
               
           }
       }
        return actionList;
    }
   
   private Action[] appendToArray(Action[] list, Action... e) {
       Action[] expanded = new Action[list.length + e.length];
       System.arraycopy(list, 0, expanded, 0, list.length);
       System.arraycopy(e, 0, expanded, list.length, e.length);

       return expanded;
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
   
   /*@Override
   public Sheet createSheet()
   {
       Sheet sheet = Sheet.createDefault();
       IEntityWrapper obj = getLookup().lookup(IEntityWrapper.class);

       IAuthenticatedDataStoreCoordinator dsc = Lookup.getDefault().lookup(ConnectionProvider.class).getConnection();
       DataContext c = dsc.getContext();
       IEntityBase e = obj.getEntity();
       
       Sheet.Set myProperties = createPropertySetForUser(obj, e, c.currentAuthenticatedUser());
       
       if (e.getOwner().getUuid().equals(c.currentAuthenticatedUser().getUuid()))
       {
           myProperties.setDisplayName("My Properties (Owner)");
           sheet.put(myProperties);
       }
       else{
           myProperties.setDisplayName("My Properties");
           sheet.put(myProperties);

           Sheet.Set ownerProperties = createPropertySetForUser(obj, e, e.getOwner());
           
           ownerProperties.setDisplayName("Owner Properties (" + e.getOwner().getUsername() + ")");
           sheet.put(ownerProperties);
       }
       
       Iterator<User> userItr = c.getUsersIterator();
       while (userItr.hasNext())
       {
           User u = userItr.next();
           
           Sheet.Set userProperties = createPropertySetForUser(obj, e, u);
           userProperties.setDisplayName(u.getUsername() + "'s Properties");
           sheet.put(userProperties);
       }
       
       return sheet;
   }
   
   protected Sheet.Set createPropertySetForUser(IEntityWrapper obj, IEntityBase e, User u)
   {
       Sheet.Set properties = Sheet.createPropertiesSet();
       properties.setName(u.getUsername() + "'s Properties");
       Map<String, Object> props = e.getUserProperties(u);
       for (String propKey :props.keySet())
       {
           Property entityProp = new EntityProperty(obj, propKey, props.get(propKey), e.canWrite());
           entityProp.setName(propKey);
           properties.put(entityProp);
       }
       return properties;
   }
   
    @Override
    public Action[] getActions(boolean popup) {
        return actionList;
    }*/
    //TODO: figure out how to make this work
    /*
    @Override
    public boolean canCopy() {
        return true;
    }
    
     @Override
    public Transferable clipboardCopy() throws IOException {
        Transferable deflt = super.clipboardCopy();
        ExTransferable added = ExTransferable.create(deflt);
        added.put(new ExTransferable.Single(DataFlavor.stringFlavor) {
            @Override
            protected String getData() {
                Lookup.Result global = Utilities.actionsGlobalContext().lookupResult(IEntityWrapper.class);
                Collection<? extends IEntityWrapper> entities = global.allInstances();
                String selection = "";
                if (entities.size() == 1) {
                    selection += entities.iterator().next().getURI();
                } else {
                    for (IEntityWrapper ew : entities) {
                        selection += ew.getURI() + "\n";
                    }
                }
                System.out.println("Selection: " + selection);
                return selection;
            }
        });
        return added;
    }*/
}
