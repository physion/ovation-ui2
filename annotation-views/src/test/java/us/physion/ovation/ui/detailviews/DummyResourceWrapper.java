/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Resource;

/**
 *
 * @author jackie
 */
public class DummyResourceWrapper implements IResourceWrapper{

    private DataStoreCoordinator dsc;
    String uri;
    String name;
    public DummyResourceWrapper(DataStoreCoordinator dsc, String name, Resource r)
    {
        this.dsc = dsc;
        this.name = name;
        uri = r.getDataUri().toString();
    }
    @Override
    public Resource getEntity() {
        return (Resource)dsc.getContext().getObjectWithURI(uri);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getURI() {
        return uri;
    }

}
