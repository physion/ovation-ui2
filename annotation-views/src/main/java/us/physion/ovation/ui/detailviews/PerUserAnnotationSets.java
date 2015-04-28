/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.mixin.KeywordAnnotatable;
import us.physion.ovation.domain.mixin.Owned;
import us.physion.ovation.domain.mixin.PropertyAnnotatable;
import us.physion.ovation.ui.TableTreeKey;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
public class PerUserAnnotationSets {
    
    static List<TableTreeKey> createPropertySets(Collection<? extends IEntityWrapper> entityWrappers, DataContext c)
    {
        Entities entities = digestEntities(c, entityWrappers);
        List<TableTreeKey> properties = new ArrayList<>();
        
        Set<User> users = new HashSet<>();
        for (IEntityWrapper e : entityWrappers) {
            OvationEntity entity = e.getEntity();
            if (entity instanceof PropertyAnnotatable) {
                users.addAll(((PropertyAnnotatable) entity).getProperties().keySet());
            }
        }
        
        for (User u : users) {
            UserPropertySet propertySet = entities.getUserPropertySet(u);
            if (!propertySet.getProperties().isEmpty())
            {
                properties.add(propertySet);
            }
        }
        
        if (!entities.currentUserHasProperties()) {
            User current = c.getAuthenticatedUser();
            properties.add(entities.getUserPropertySet(current));
        }
        
        Collections.sort(properties);
        return properties;
    }
    static List<TableTreeKey> createTagSets(Collection<? extends IEntityWrapper> entityWrappers, DataContext c)
    {
        Entities entities = digestEntities(c, entityWrappers);
        List<TableTreeKey> tags = new ArrayList<>();
        
        Set<User> users = new HashSet<>();
        for(IEntityWrapper e : entityWrappers) {
            OvationEntity entity = e.getEntity();
            if(entity instanceof KeywordAnnotatable) {
                users.addAll(((KeywordAnnotatable)entity).getTags().keySet());
            }
        }
        
        for (User u : users) { //TODO this is dumb; get the users from entities
            TagsSet tagSet = entities.getUserTagsSet(u);
            if (!tagSet.getTags().isEmpty()) {
                tags.add(tagSet);
            }
        }
        
        if (!entities.currentUserHasTags()) {
            User current = c.getAuthenticatedUser();
            tags.add(entities.getUserTagsSet(current));
        }
        
        Collections.sort(tags);
        return tags;
    }
    
    static Entities digestEntities(DataContext c, Collection<? extends IEntityWrapper> entities)
    {
        return new Entities(c, entities);
    }
    
    static class Entities{
        Set<String> uris;
        Set<OvationEntity> entitybases;
        Set<String> owners;
        User currentUser;
        Entities(DataContext c, Collection<? extends IEntityWrapper> entities)
        {
            entitybases = new HashSet<>();
            owners = new HashSet<>();
            uris = new HashSet<>();
            currentUser = c.getAuthenticatedUser();

            for (IEntityWrapper w : entities) {
                OvationEntity e = w.getEntity();
                entitybases.add(e);
                uris.add(e.getURI().toString());
                if (e instanceof Owned) {
                    owners.add(((Owned) e).getOwner().getURI().toString());
                }
            }
        }
        
        private Map<String, Object> getProperties(User u)
        {
            Map<String, Object> userProps = new HashMap<>();
            for (OvationEntity e : entitybases) {
                if (e instanceof PropertyAnnotatable)
                    userProps.putAll(((PropertyAnnotatable)e).getUserProperties(u));
            }
            return userProps;
        }
        
        
        private List<String> getTags(User u)
        {
            List<String> tags = new LinkedList();
            for (OvationEntity e : entitybases) {
                if (e instanceof KeywordAnnotatable)
                    tags.addAll(Lists.newLinkedList(((KeywordAnnotatable)e).getUserTags(u)));
            }
            Collections.sort(tags);
            return tags;
        }
        
        UserPropertySet getUserPropertySet(User u)
        {
            Map<String, Object> userProps = getProperties(u);
            return new UserPropertySet(u, isOwner(u), isCurrentUser(u), userProps, uris);
        }
        
        TagsSet getUserTagsSet(User u)
        {
            List<String> tags = getTags(u);
            return new TagsSet(u, isOwner(u), isCurrentUser(u), tags, uris);
        }
        private boolean isOwner(User u)
        {
            return owners.contains(u.getURI().toString());
        }
        
        private boolean isCurrentUser(User u)
        {
            return currentUser.equals(u);
        }
        
        public boolean currentUserHasProperties()
        {
            return !getProperties(currentUser).isEmpty();
        }
        
        public boolean currentUserHasTags()
        {
            return !getTags(currentUser).isEmpty();
        }
        
    }
}
