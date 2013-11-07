package us.physion.ovation.ui.detailviews;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Tools",
id = "us.physion.ovation.ui.detailviews.NewNote")
@ActionRegistration(displayName = "#CTL_NewNote")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1000),
    @ActionReference(path = "Shortcuts", name = "DS-N")
})
@Messages("CTL_NewNote=New Note")
public final class NewNote implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        //Lookup entities
        /*Collection<? extends IEntityWrapper> entities = Utilities.actionsGlobalContext().lookupAll(IEntityWrapper.class);
        NotesTopComponent component = Lookup.getDefault().lookup(NotesTopComponent.class);
        if (entities.isEmpty() && component != null && component.getEntities() != null)
        {
            entities = component.getEntities();
        }
        boolean annotatable = false;
        for (IEntityWrapper ent : entities)
        {
            if (AnnotatableEntity.class.isAssignableFrom(ent.getType()))
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
                        if (component != null)
                            component.addNote(note);// add the note to the existing table
                    }
                    else{
                        entity.addNote(note);
                    }
                }
            }
        }*/
    }
}
