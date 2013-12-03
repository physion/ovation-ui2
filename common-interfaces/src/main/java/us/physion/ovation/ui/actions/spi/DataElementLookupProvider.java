package us.physion.ovation.ui.actions.spi;

import org.openide.util.Lookup;
import us.physion.ovation.domain.mixin.DataElement;

public interface DataElementLookupProvider {

    Lookup getLookup(DataElement element);
}
