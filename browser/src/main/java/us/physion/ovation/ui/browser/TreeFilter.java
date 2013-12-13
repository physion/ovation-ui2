package us.physion.ovation.ui.browser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class TreeFilter {

    public static final TreeFilter NO_FILTER = new TreeFilter(true) {
        @Override
        public String toString() {
            return "NO_FILTER"; //NOI18N
        }
    };
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean projectView;
    private boolean experimentsVisible;
    private boolean epochGroupsVisible;
    private boolean epochsVisible;

    public TreeFilter() {
        this(true);
    }

    private TreeFilter(boolean b) {
        projectView = b;
        experimentsVisible = b;
        epochGroupsVisible = b;
        epochsVisible = b;
    }

    public boolean isProjectView() {
        return projectView;
    }

    public void setProjectView(boolean projectView) {
        if (this.projectView == projectView) {
            return;
        }
        boolean old = this.projectView;
        this.projectView = projectView;
        pcs.firePropertyChange("projectView", old, projectView); //NOI18N
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
        return "Info: project " + this.projectView + " exp " + this.experimentsVisible + " epochgroups " + this.epochGroupsVisible + " epoch " + this.epochsVisible; //NOI18N
    }
}
