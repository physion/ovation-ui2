/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.net.URI;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.mixin.ResourceContainer;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.values.Resource;

public class ResourceWrapper implements IResourceWrapper {

    String entityUri;
    String uri;
    String name;

    public ResourceWrapper(String name, Resource r, URI entityUri) {
        uri = r.getDataUri().toString();
        this.entityUri = entityUri.toString();
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Resource getEntity() {
        DataContext c  = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();

        return ((ResourceContainer) c.getObjectWithURI(entityUri)).getResource(getName());
    }

    @Override
    public String getURI() {
        return uri;
    }
};
