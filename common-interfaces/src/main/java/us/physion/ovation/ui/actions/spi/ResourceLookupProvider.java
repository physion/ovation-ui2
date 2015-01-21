package us.physion.ovation.ui.actions.spi;

import java.net.URI;
import java.util.List;
import org.openide.util.Lookup;
import us.physion.ovation.domain.Resource;

public interface ResourceLookupProvider {

    Lookup getLookup(Resource element);

    Lookup getLookup(Resource element, List<URI> entityPath);
}
