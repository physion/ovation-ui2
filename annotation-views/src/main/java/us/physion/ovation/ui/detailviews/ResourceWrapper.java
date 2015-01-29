/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

public class ResourceWrapper implements IResourceWrapper {

    String entityUri;
    String uri;
    String name;

    public ResourceWrapper(String name, Resource r) {
        uri = r.getDataUrl().toString();
        this.name = name;
        this.entityUri = r.getURI().toString();
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

        return (Resource) c.getObjectWithURI(entityUri);
    }

    @Override
    public String getURI() {
        return uri;
    }
};
