package us.physion.ovation.ui.actions;

import java.net.URI;
import java.util.List;
import javax.swing.Action;
import us.physion.ovation.domain.mixin.DataElement;

public interface SelectInProjectNavigatorActionFactory {
    
    Action select(DataElement date, List<URI> source);
    
}
