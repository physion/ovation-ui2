/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import ovation.IAuthenticatedDataStoreCoordinator;
import ovation.Resource;

/**
 *
 * @author jackie
 */
public class TestResourceWrapper implements IResourceWrapper{

    private IAuthenticatedDataStoreCoordinator dsc;
    String uri; 
    String name;
    public TestResourceWrapper(IAuthenticatedDataStoreCoordinator dsc, Resource r)
    {
        this.dsc = dsc;
        name = r.getName();
        uri = r.getURIString();
    }
    @Override
    public Resource getEntity() {
        return (Resource)dsc.getContext().objectWithURI(uri);
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
