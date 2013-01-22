/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import org.openide.util.Lookup;
import ovation.DataContext;
import ovation.IAuthenticatedDataStoreCoordinator;
import ovation.Resource;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

public class ResourceWrapper implements IResourceWrapper {

    String uri;
    String name;

    public ResourceWrapper(Resource r) {
        uri = r.getURIString();
        name = r.getName();
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
        IAuthenticatedDataStoreCoordinator dsc = Lookup.getDefault().lookup(ConnectionProvider.class).getConnection();
        DataContext c = dsc.getContext();

        return (Resource) c.objectWithURI(uri);
    }

    @Override
    public String getURI() {
        return uri;
    }
};
