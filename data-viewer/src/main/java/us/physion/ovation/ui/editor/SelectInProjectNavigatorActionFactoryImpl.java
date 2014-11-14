package us.physion.ovation.ui.editor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.mixin.Identity;
import us.physion.ovation.ui.actions.SelectInProjectNavigatorActionFactory;
import us.physion.ovation.ui.browser.BrowserUtilities;

@ServiceProvider(service = SelectInProjectNavigatorActionFactory.class)
public class SelectInProjectNavigatorActionFactoryImpl implements SelectInProjectNavigatorActionFactory {

    private enum PathType {
        ProjectPath, SourcePath
    }
    
    @Override
    public Action selectInTopComponent(String topComponentId, Identity data) {
        List<URI> path = new ArrayList<URI>();
        PathType kind = buildPath(data, path);
        path.add(null);
        Collections.reverse(path);
        return new OpenNodeInBrowserAction(topComponentId, path);
    }

    @Override
    public Action select(Identity data, String displayName, List<URI> source) {
        List<URI> path = new ArrayList<URI>();
        PathType kind = buildPath(data, path);
        path.add(null);
        Collections.reverse(path);
        return new OpenNodeInBrowserAction(path, displayName,
                kind == PathType.ProjectPath, source,
                kind == PathType.ProjectPath ? BrowserUtilities.PROJECT_BROWSER_ID : BrowserUtilities.SOURCE_BROWSER_ID);
    }

    @Override
    public Action select(String explorerTopComponentID, List<URI> path) {
        return new OpenNodeInBrowserAction(path, null, false, null, explorerTopComponentID);
    }

    //XXX: This should be moved in some Utils class
    //XXX: Also see OvationSearchProvider.getURIPath
    private PathType buildPath(Identity data, List<URI> path) {
        Identity id = data;

        while (id != null) {
            path.add(id.getURI());
            if (id instanceof Measurement) {
                Measurement m = (Measurement) id;
                id = m.getEpoch();
            } else if (id instanceof Resource) {
                id = ((Resource) id).getContainingEntity();
            } else if (id instanceof AnalysisRecord) {
                AnalysisRecord record = (AnalysisRecord) id;
                
                //if parent is a Project, see if the record doesn't have an User owner
                User u = record.getOwner();
                if (u != null) {
                    path.add(u.getURI());
                }
                
                id = record.getParent();
                
            } else if (id instanceof Epoch) {
                id = ((Epoch) id).getParent();
            } else if (id instanceof EpochGroup) {
                id = ((EpochGroup) id).getParent();
            } else if (id instanceof Experiment) {
                //XXX: 1:N mapping
                Experiment e = (Experiment) id;
                Iterator<Project> it = e.getProjects().iterator();
                if (it.hasNext()) {
                    id = it.next();
                } else {
                    id = null;
                }
            } else if (id instanceof Project) {
                //reached top level
                return PathType.ProjectPath;
            } else if (id instanceof Source) {
                //XXX: 1:N mapping
                Source s = (Source) id;
                
                Iterator<Source> parents = s.getParentSources().iterator();
                
                if (parents.hasNext()) {
                    id = parents.next();
                } else {
                    return PathType.SourcePath;
                }
            } else {
                throw new IllegalStateException("Cannot build a path for DateElement of class " + data.getClass());
            }
        }
        
        throw new IllegalStateException("No top-level Project or Source found");
    }
}
