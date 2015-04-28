package us.physion.ovation.ui.browser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class TreeFilter {

    public static final TreeFilter NO_FILTER = new TreeFilter(NavigatorType.NONE_TYPE) {
        @Override
        public String toString() {
            return "NO_FILTER"; //NOI18N
        }
    };

    public static enum NavigatorType {
        PROJECT,
        SOURCE,
        PROTOCOL,
        NONE_TYPE
    }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private NavigatorType navigatorType;
    private boolean experimentsVisible;
    private boolean epochGroupsVisible;
    private boolean epochsVisible;

    public TreeFilter() {
        this(NavigatorType.PROJECT);
    }

    public TreeFilter(NavigatorType t) {
        navigatorType = t;
        experimentsVisible = true;
        epochGroupsVisible = true;
        epochsVisible = true;
    }

    public NavigatorType getNavigatorType() {
        return navigatorType;
    }

    public void setNavigatorType(NavigatorType navigatorType) {
        this.navigatorType = navigatorType;
    }


    public boolean isExperimentsVisible() {
        return experimentsVisible;
    }

    public void setExperimentsVisible(boolean experimentsVisible) {
        if (this.experimentsVisible == experimentsVisible) {
            return;
        }
        boolean old = this.experimentsVisible;
        this.experimentsVisible = experimentsVisible;
        pcs.firePropertyChange("experimentsVisible", old, experimentsVisible); //NOI18N
    }

    public boolean isEpochGroupsVisible() {
        return epochGroupsVisible;
    }

    public void setEpochGroupsVisible(boolean epochGroupsVisible) {
        if (this.epochGroupsVisible == epochGroupsVisible) {
            return;
        }
        boolean old = this.epochGroupsVisible;
        this.epochGroupsVisible = epochGroupsVisible;
        pcs.firePropertyChange("epochGroupsVisible", old, this.epochGroupsVisible); //NOI18N
    }

    public boolean isEpochsVisible() {
        return epochsVisible;
    }

    public void setEpochsVisible(boolean epochsVisible) {
        if (this.epochsVisible == epochsVisible) {
            return;
        }
        boolean old = this.epochsVisible;
        this.epochsVisible = epochsVisible;
        pcs.firePropertyChange("epochsVisible", old, this.epochsVisible); //NOI18N
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    @Override
    public String toString() {
        return "Info: project " + this.navigatorType + " exp " + this.experimentsVisible + " epochgroups " + this.epochGroupsVisible + " epoch " + this.epochsVisible; //NOI18N
    }
}
