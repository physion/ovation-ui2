/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.values.Resource;

public class ResourceWrapper implements IResourceWrapper {

    String uri;
    String name;

    public ResourceWrapper(String name, Resource r) {
        uri = r.getUti();
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
        DataStoreCoordinator dsc = Lookup.getDefault().lookup(ConnectionProvider.class).getConnection();
        DataContext c = dsc.getContext();

        return (Resource) c.getObjectWithURI(uri);
    }

    @Override
    public String getURI() {
        return uri;
    }
};
