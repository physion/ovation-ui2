/*
 * Copyright (C) 2014 Physion LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package us.physion.ovation.ui.browser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.ui.interfaces.IEntityNode;

@ServiceProvider(service = HeavyLoadManager.class)
public final class HeavyLoadManager {

    public HeavyLoadManager() {
        //nothing
    }

    public static HeavyLoadManager getDefault() {
        return Lookup.getDefault().lookup(HeavyLoadManager.class);
    }

    public void register(final ExplorerManager em) {
        em.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                    for (Node n : em.getSelectedNodes()) {
                        //XXX: I could use URITreePathProvider pathProvider = n.getLookup().lookup(URITreePathProvider.class);
                        // to get the actual path!
                        if (n instanceof IEntityNode) {
                            cancel(((IEntityNode) n).getEntityWrapper().getURI());
                        }
                    }
                }
            }

        });
    }

    private void cancel(String selectedURI) {
        //Canceling everything except selectedURI
        synchronized (this) {
            int index = loadingURIs.indexOf(selectedURI);
            for (int i = 0; i < loadingURIs.size(); i++) {
                if (i == index) {
                    continue;
                }
                loadingCanceled.set(i, true);
            }
        }
    }

    List<String> loadingURIs = new ArrayList<>();
    List<Integer> loadingCount = new ArrayList<>();
    List<Boolean> loadingCanceled = new ArrayList<>();

    public void startLoading(EntityWrapper parent) {
        String uri = parent.getURI();
        
        synchronized (this) {
            int index = loadingURIs.indexOf(uri);
            if (index != -1) {
                loadingCount.set(index, loadingCount.get(index) + 1);
                loadingCanceled.set(index, false);
            } else {
                loadingURIs.add(uri);
                loadingCount.add(1);
                loadingCanceled.add(false);
            }
        }
    }

    public void finishedLoading(EntityWrapper parent) {
        String uri = parent.getURI();

        synchronized (this) {
            int index = loadingURIs.indexOf(uri);
            if (index != -1) {
                int count = loadingCount.get(index) - 1;
                if (count == 0) {
                    loadingURIs.remove(index);
                    loadingCount.remove(index);
                    loadingCanceled.remove(index);
                } else {
                    loadingCount.set(index, count);
                }
            }
        }
    }

    public boolean isCancelled(EntityWrapper parent) {
        String uri = parent.getURI();
        synchronized (this) {
            int index = loadingURIs.indexOf(uri);
            if (index != -1) {
                return loadingCanceled.get(index);
            } else {
                //TODO: log? Cannot determine cancel status for uri
                return false;
            }
        }
    }

}
