package us.physion.ovation.ui.jumpto.api;

import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.List;

public abstract class JumpHistory {

    public final static String PROP_CURSOR = "cursor"; //NOI18N

    public static abstract class Item {

        public abstract String getDisplayName();

        public abstract List<URI> getProjectNavigatorTreePath();
    }

    public abstract void add(String displayName, List<URI> entityPath, List<URI> source);

    public abstract Item goBack();

    public abstract boolean hasBack();

    public abstract Item goForward();

    public abstract boolean hasForward();
    
    public abstract void addPropertyChangeListener(PropertyChangeListener pcl);

    public abstract void removePropertyChangeListener(PropertyChangeListener pcl);
}
