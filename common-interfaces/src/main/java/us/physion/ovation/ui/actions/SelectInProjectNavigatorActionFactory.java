package us.physion.ovation.ui.actions;

import java.net.URI;
import java.util.List;
import javax.swing.Action;
import us.physion.ovation.domain.mixin.Identity;

public interface SelectInProjectNavigatorActionFactory {
    
    Action select(Identity date, String displayName, List<URI> source);
    
    Action selectInTopComponent(String topComponentId, Identity data);
    
}
