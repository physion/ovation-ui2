package us.physion.ovation.ui.interfaces;

import java.net.URI;
import java.util.List;

public interface URINode {

    List<URI> getFilteredParentURIs();
    
    URI getURI();
}
