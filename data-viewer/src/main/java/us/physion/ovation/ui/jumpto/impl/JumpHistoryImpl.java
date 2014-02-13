package us.physion.ovation.ui.jumpto.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.jumpto.api.JumpHistory;

@ServiceProvider(service = JumpHistory.class)
public class JumpHistoryImpl extends JumpHistory {

    static class Link<T> {

        T value;
        Link<T> prev, next;

        private Link(T item) {
            this.value = item;
            prev = next = null;
        }
    }
    private Link<Item> cursor = null;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final LookupListener lookupListener;
    
    public JumpHistoryImpl() {
        final Lookup.Result<IEntityWrapper> result = Utilities.actionsGlobalContext().lookupResult(IEntityWrapper.class);
        
        //clear history if user manually selects something
        lookupListener = new LookupListener() {
            @Override
            public void resultChanged(LookupEvent le) {
                Collection<? extends IEntityWrapper> selection = result.allInstances();
                if (selection.size() > 1) {
                    //only single selection supported
                    clear();
                    return;
                }

                for (IEntityWrapper w : selection) {
                    URI u = w.getEntity().getURI();

                    if (hasForward()) {
                        List<URI> next = cursor.next.value.getProjectNavigatorTreePath();
                        if (next.get(next.size() - 1).equals(u)) {
                            //ok, we selected the next item
                            goForward();
                            return;
                        }
                    }
                    if (hasBack()) {
                        List<URI> prev = cursor.prev.value.getProjectNavigatorTreePath();
                        if (prev.get(prev.size() - 1).equals(u)) {
                            //ok, we selected the previous item
                            goBack();
                            return;
                        }
                    }
                    if (cursor != null) {
                        List<URI> current = cursor.value.getProjectNavigatorTreePath();
                        if (current.get(current.size() - 1).equals(u)) {
                            //ok, we selected the current item
                            //ignore
                            return;
                        }
                    }
                    
                    //nothing matches
                    clear();
                    return;
                }
            }
        };
        result.addLookupListener(lookupListener);
    }
    
    private void clear() {
        if (cursor != null) {
            cursor = null;
            fire();
        }
    }

    @Override
    public void add(String displayName, List<URI> entityPath, List<URI> source) {
        if (source != null && !source.isEmpty()) {
            add(null, source);
        }
        add(displayName, entityPath);
    }

    private void add(String displayName, List<URI> entityPath) {
        ItemImpl item = new ItemImpl(displayName, entityPath);

        if (cursor != null) {
            if (item.equals(cursor.value)) {
                //cannot add the same value twice in the history
                return;
            }


            if (cursor.next == null) {
                Link<Item> next = new Link(item);
                next.prev = cursor;
                cursor.next = next;

                cursor = next;
                fire();
            } else {
                Item current = cursor.next.value;

                if (item.equals(current)) {
                    //same jump
                    cursor = cursor.next;
                    fire();
                } else {
                    //break link
                    cursor.next.prev = null;

                    Link<Item> next = new Link(item);
                    next.prev = cursor;
                    cursor.next = next;

                    cursor = next;
                    fire();
                }
            }
        } else {
            cursor = new Link<Item>(item);
            fire();
        }
    }

    @Override
    public Item goBack() {
        if (hasBack()) {
            cursor = cursor.prev;
            fire();

            return cursor.value;
        }

        return null;
    }

    @Override
    public boolean hasBack() {
        return cursor != null && cursor.prev != null;
    }

    @Override
    public boolean hasForward() {
        return cursor != null && cursor.next != null;
    }

    @Override
    public Item goForward() {
        if (hasForward()) {
            cursor = cursor.next;
            fire();

            return cursor.value;
        }
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    private void fire() {
        //values don't mean anything
        pcs.firePropertyChange(PROP_CURSOR, true, false);
    }
}
