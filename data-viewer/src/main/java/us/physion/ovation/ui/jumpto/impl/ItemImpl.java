package us.physion.ovation.ui.jumpto.impl;

import java.net.URI;
import java.util.List;
import us.physion.ovation.ui.jumpto.api.JumpHistory.Item;

final class ItemImpl extends Item {

    private final String displayName;
    private final List<URI> projectNavigatorTreePath;

    public ItemImpl(String displayName, List<URI> projectNavigatorTreePath) {
        this.displayName = displayName;
        this.projectNavigatorTreePath = projectNavigatorTreePath;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public List<URI> getProjectNavigatorTreePath() {
        return projectNavigatorTreePath;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemImpl)) {
            return super.equals(o);
        }
        ItemImpl other = (ItemImpl) o;
        return ((displayName == null && other.getDisplayName() == null) || (displayName != null && displayName.equals(other.getDisplayName()))) && getProjectNavigatorTreePath().equals(other.getProjectNavigatorTreePath());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
        hash = 47 * hash + (this.projectNavigatorTreePath != null ? this.projectNavigatorTreePath.hashCode() : 0);
        return hash;
    }
}
