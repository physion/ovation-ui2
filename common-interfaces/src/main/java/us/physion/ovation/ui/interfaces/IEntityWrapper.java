package us.physion.ovation.ui.interfaces;

import java.net.URI;
import java.util.List;
import us.physion.ovation.domain.OvationEntity;

/**
 *
 * @author huecotanks
 */
public interface IEntityWrapper {

    String getDisplayName();

    OvationEntity getEntity();
    
    OvationEntity getEntity(boolean includingTrash);
    
    <T extends OvationEntity> T getEntity(Class<T> clazz);

    Class getType();

    String getURI();
    
    List<URI> getFilteredParentURIs();
    
    boolean isLeaf();

    public boolean canRename();

    public void setName(String s);

    public String getName();
}
