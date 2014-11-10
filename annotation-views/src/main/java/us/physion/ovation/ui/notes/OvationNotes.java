package us.physion.ovation.ui.notes;

import com.google.common.collect.Multimap;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openide.util.ImageUtilities;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.mixin.NoteAnnotatable;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.values.NoteAnnotation;

class OvationNotes extends NotesUi {

    static abstract class OvationMessage implements NotesUi.Message {

        private final Map.Entry<User, NoteAnnotation> entry;

        OvationMessage(Map.Entry<User, NoteAnnotation> entry) {
            this.entry = entry;
        }

        public Map.Entry<User, NoteAnnotation> getEntry() {
            return entry;
        }
    }
    
    private final ListModel EMPTY_LIST_MODEL = new ListModel() {
        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public Object getElementAt(int index) {
            return null;
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            //ignore
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            //ignore
        }
    };
    
    private NoteAnnotatable noteSource;
    private final DateTimeFormatter dateFormatter = DateTimeFormat.forStyle("MM").withLocale(Locale.getDefault()); //NOI18N
    
    ListModel model = EMPTY_LIST_MODEL;

    @Override
    protected void delete(int deleteIndex, NotesUi.Message message) {
        if (message instanceof OvationNotes.OvationMessage) {
            OvationNotes.OvationMessage m = (OvationNotes.OvationMessage) message;
            noteSource.removeNote(m.getEntry().getValue());
            
            refresh();
        } else {
            //impossible
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    protected void save(String message) {
        if(noteSource == null){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        noteSource.addNote(new DateTime(), message);
        refresh();
    }

    @Override
    protected ListModel getListModel() {
        return model;
    }

    @Override
    protected String getSaveText() {
        return Bundle.CTL_Save();
    }

    @Override
    protected String getUserGreetingText() {
        return Bundle.CTL_UserGreeting();
    }

    @Override
    protected String getDeleteText() {
        return Bundle.CTL_Delete();
    }

    @Override
    protected String getAskBeforeDeletingText() {
        return Bundle.CTL_AskBeforeDeleting();
    }

    @Override
    protected Icon getAskBeforeDeletingIcon() {
        return ImageUtilities.loadImageIcon("us/physion/ovation/ui/notes/api/stimulus.png", true);
    }

    @Override
    protected String getRefreshText() {
        return Bundle.CTL_Refresh();
    }

    @Override
    protected String getRefreshTooltip() {
        return Bundle.HINT_Refresh();
    }

    @Override
    protected Icon getRefreshIcon() {
        return ImageUtilities.loadImageIcon("us/physion/ovation/ui/notes/api/refresh.png", true);
    }

    @Override
    protected String getShowGravatarText() {
        return Bundle.CTRL_ShowGravatar();
    }

    @Override
    protected String getShowGravatarTooltip() {
        return Bundle.HINT_ShowGravatar();
    }

    @Override
    protected Icon getShowGravatarIcon() {
        return ImageUtilities.loadImageIcon("us/physion/ovation/ui/notes/api/user.png", true);
    }

    @Override
    protected String getDeleteConfirmMessageText() {
        return Bundle.CTL_DeleteConfirm();
    }

    @Override
    protected String getDeleteConfirmMessageTitle() {
        return Bundle.CTRL_DeleteConfirmTitle();
    }

    @Override
    protected void refresh() {
        if (noteSource != null) {
            refreshNotes(noteSource);
            super.refresh();
        }
    }

    protected void refreshNotes(Collection entities) {
        if (!entities.isEmpty()) {
            final IEntityWrapper w = (IEntityWrapper) entities.iterator().next();
            if (w.getEntity() instanceof NoteAnnotatable) {
                refreshNotes((NoteAnnotatable) w.getEntity());
            }
        }
    }

    protected void refreshNotes(NoteAnnotatable source) {
        if (source == null) {
            return;
        }
        
        noteSource = source;
        Multimap<User, NoteAnnotation> nodes = noteSource.getNotes();
        final List<Map.Entry<User, NoteAnnotation>> list = new ArrayList<Map.Entry<User, NoteAnnotation>>(nodes.entries());
        Collections.sort(list, new Comparator<Map.Entry<User, NoteAnnotation>>() {
            @Override
            public int compare(Map.Entry<User, NoteAnnotation> a, Map.Entry<User, NoteAnnotation> b) {
                return a.getValue().getTimeStamp().compareTo(b.getValue().getTimeStamp());
            }
        });
        ListModel newModel = new ListModel() {
            @Override
            public int getSize() {
                return list.size();
            }

            @Override
            public Object getElementAt(int index) {
                Map.Entry<User, NoteAnnotation> entry = list.get(index);
                final NoteAnnotation note = entry.getValue();
                final User user = entry.getKey();
                return new OvationNotes.OvationMessage(entry) {
                    @Override
                    public String getText() {
                        return note.getText();
                    }

                    @Override
                    public String getUsername() {
                        return user.getUsername();
                    }

                    @Override
                    public String getEmail() {
                        return user.getEmail();
                    }

                    @Override
                    public String getTimestampTooltip() {
                        return note.getTimeStamp().toString();
                    }

                    @Override
                    public String getTimestamp() {
                        return dateFormatter.print(note.getTimeStamp());
                    }
                };
            }

            @Override
            public void addListDataListener(ListDataListener l) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void removeListDataListener(ListDataListener l) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        model = newModel;
        refreshMessages();
    }
}
