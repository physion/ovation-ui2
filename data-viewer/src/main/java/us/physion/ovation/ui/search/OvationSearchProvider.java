package us.physion.ovation.ui.search;

import java.awt.Toolkit;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.browser.BrowserUtilities;
import us.physion.ovation.ui.browser.QueryProvider;
import us.physion.ovation.ui.browser.QuerySet;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.reveal.api.RevealNode;

@Messages({
    "# {0} - entity url",
    "Search_Open_Entity=Select {0}",
    "Search_Run_Query=Run query {0}"
})
public class OvationSearchProvider implements SearchProvider {

    private final static Logger log = LoggerFactory.getLogger(OvationSearchProvider.class);

    public OvationSearchProvider() {
        //nothing
    }

    @Override
    public void evaluate(SearchRequest search, SearchResponse response) {
        final String query = search.getText();
        if (query == null) {
            return;
        }

        final DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();

        if (query.startsWith("ovation")) { // Select entity
            String uriString = null;
            try {
                URI uri = new URI(query);
                uriString = uri.toString();
            } catch (URISyntaxException ex) {
                uriString = null;
            }

            if (uriString != null) {
                final OvationEntity ent = ctx.getObjectWithURI(uriString);

                if (ent != null) {
                    response.addResult(new Runnable() {

                        @Override
                        public void run() {
                            //TODO: Get display name from the OvationEntity ent
                            RevealNode.forEntity(BrowserUtilities.PROJECT_BROWSER_ID, ent);
                        }
                    }, Bundle.Search_Open_Entity(ent.getURI()));
                }
            }
        }

        // Run search query
        response.addResult(new Runnable() {

            @Override
            public void run() {
                QuerySet querySet = new QuerySet();
                Lookup.getDefault().lookup(QueryProvider.class).setQuerySet(querySet);
                try {
                    for (OvationEntity entity : ctx.query(OvationEntity.class, query).get()) {
                        querySet.add(entity);
                    }
                } catch (InterruptedException e1) {
                    //pass
                } catch (ExecutionException e1) {
                    //pass — invalid query
                    Toolkit.getDefaultToolkit().beep();
                } catch (OvationException e1) {
                    //pass — invalid query
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }, Bundle.Search_Run_Query(query));
    }
}
