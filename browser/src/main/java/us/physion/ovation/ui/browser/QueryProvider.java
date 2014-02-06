/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jackie
 */
@ServiceProvider(service = QueryProvider.class)
public class QueryProvider {
    QuerySet querySet;
    public QuerySet getQuerySet()
    {
        return querySet;
    }
    
    public void setQuerySet(QuerySet set)
    {
        querySet = set;
    }
}
