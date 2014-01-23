package us.physion.ovation.ui.actions.spi;

import java.net.URI;
import java.util.List;
import org.openide.util.Lookup;
import us.physion.ovation.domain.mixin.DataElement;

public interface DataElementLookupProvider {

    Lookup getLookup(DataElement element);

    Lookup getLookup(DataElement element, List<URI> entityPath);
}
