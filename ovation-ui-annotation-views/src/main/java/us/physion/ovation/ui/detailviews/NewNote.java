/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import ovation.IAnnotatableEntityBase;
import ovation.IAnnotation;
import ovation.ITimelineElement;
import ovation.Note;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

@ActionID(category = "Tools",
id = "us.physion.ovation.ui.detailviews.NewNote")
@ActionRegistration(displayName = "#CTL_NewNote")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 2350),
    @ActionReference(path = "Shortcuts", name = "M-N")
})
@Messages("CTL_NewNote=New Note")
public final class NewNote implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        //Lookup entities
        Collection<? extends IEntityWrapper> entities = Utilities.actionsGlobalContext().lookupAll(IEntityWrapper.class);
        boolean annotatable = false;
        for (IEntityWrapper ent : entities)
        {
            if (IAnnotatableEntityBase.class.isAssignableFrom(ent.getType()))
            {
                annotatable = true;
            }
        }
        if (entities.isEmpty() || !annotatable)
            return;
        
        //open dialog
        NewNoteDialog d = new NewNoteDialog();
        d.display();
        if (d.isCancelled())
            return;
        
        
        Note note = null;
        for (IEntityWrapper ent : entities)
        {
            if (IAnnotatableEntityBase.class.isAssignableFrom(ent.getType()))
            {
                IAnnotatableEntityBase entity = (IAnnotatableEntityBase)(ent.getEntity());
                {
                    if (note == null)
                    {
                        note = (Note)entity.addNote(d.getNote());
                        note.addProperty("ovation_timestamp",  new Timestamp(new DateTime().getMillis()));
                        note.addProperty("ovation_timezone",  Calendar.getInstance().getTimeZone().getID());
                        NotesTopComponent component = Lookup.getDefault().lookup(NotesTopComponent.class);
                        if (component != null)
                            component.addNote(note);// add the note to the existing table
                    }
                    else{
                        entity.addNote(note);
                    }
                }
            }
        }
    }
}
