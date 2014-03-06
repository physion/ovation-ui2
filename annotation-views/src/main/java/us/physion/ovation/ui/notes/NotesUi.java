package us.physion.ovation.ui.notes;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.AbstractBorder;

public abstract class NotesUi extends JPanel {

    public final static Color OVATION_GREEN = new Color(16, 124, 7);
    protected boolean askBeforeDeleting = false;
    protected boolean showGravatar = false;
    private final Box messages;

    protected abstract void delete(int deleteIndex, Message message);

    protected void refreshMessages() {
        messages.removeAll();
        populateMessages(messages, getListModel());

        messages.revalidate();
        messages.repaint();
    }
    
    protected void refresh() {
        refreshMessages();
    }

    protected void gravatarToggled() {
        refreshMessages();
    }

    /**
     * Remember to call refresh.
     */
    protected abstract void save(String message);

    protected abstract ListModel getListModel();

    protected abstract String getSaveText();

    protected abstract String getUserGreetingText();

    protected abstract String getDeleteText();

    protected abstract String getAskBeforeDeletingText();

    protected abstract Icon getAskBeforeDeletingIcon();

    protected abstract String getRefreshText();

    protected abstract String getRefreshTooltip();

    protected abstract Icon getRefreshIcon();

    protected abstract String getShowGravatarText();

    protected abstract String getShowGravatarTooltip();

    protected abstract Icon getShowGravatarIcon();

    protected abstract String getDeleteConfirmMessageText();

    protected abstract String getDeleteConfirmMessageTitle();

    protected interface Message {

        String getText();

        public String getUsername();

        public String getEmail();
        
        public String getTimestampTooltip();
        
        public String getTimestamp();
    }
    ExecutorService executors = Executors.newSingleThreadExecutor();

    public NotesUi() {
        super(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        toolbar.setBackground(Color.WHITE);

//        toolbar.add(new JLabel(getUserGreetingText()));
//        toolbar.addSeparator();
        toolbar.add(new AbstractAction(getRefreshText(), getRefreshIcon()) {
            {
                putValue(SHORT_DESCRIPTION, getRefreshTooltip());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        toolbar.addSeparator();
        toolbar.add(new JToggleButton(new AbstractAction(getShowGravatarText(), getShowGravatarIcon()) {
            {
                putValue(SHORT_DESCRIPTION, getShowGravatarTooltip());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JToggleButton b = (JToggleButton) e.getSource();
                showGravatar = b.getModel().isSelected();
                gravatarToggled();
            }
        }));
        toolbar.add(new JToggleButton(new AbstractAction(getAskBeforeDeletingText(), getAskBeforeDeletingIcon()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JToggleButton b = (JToggleButton) e.getSource();
                NotesUi.this.askBeforeDeleting = b.getModel().isSelected();
            }
        }));

        this.add(toolbar, BorderLayout.NORTH);

        {
            messages = new ScrollableBox(BoxLayout.Y_AXIS);
            //some border to see the bubbles better
            messages.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
            messages.setBackground(Color.WHITE);

            this.add(new JScrollPane(messages), BorderLayout.CENTER);
        }

        {
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setBackground(Color.WHITE);
            final JTextArea message = new JTextArea();
            message.setRows(3);
            bottom.add(new JScrollPane(message), BorderLayout.CENTER);
            bottom.add(new JButton(new AbstractAction(getSaveText()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String text = message.getText();
                    message.setText(null);
                    save(text);
                }
            }), BorderLayout.EAST);

            this.add(bottom, BorderLayout.SOUTH);
        }
    }

    private void populateMessages(Box messages, ListModel model) {
        if (model == null) {
            return;
        }
        
        Message previousMessage = null;
        Box perUserMessages = null;
        boolean arrowLeft = false;

        for (int i = 0; i < model.getSize(); i++) {
            final Message message = (Message) model.getElementAt(i);

            final JTextArea t = new JTextArea(message.getText());
            t.setEditable(false);
            t.setLineWrap(true);

            final int deleteIndex = i;
            t.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    delete(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    delete(e);
                }

                private void delete(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        JPopupMenu p = new JPopupMenu();
                        p.add(new JMenuItem(new AbstractAction(getDeleteText()) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (askBeforeDeleting) {
                                    int res = JOptionPane.showConfirmDialog(NotesUi.this,
                                            getDeleteConfirmMessageText(), getDeleteConfirmMessageTitle(), JOptionPane.YES_NO_OPTION);
                                    if (JOptionPane.YES_OPTION != res) {
                                        return;
                                    }
                                }
                                
                                NotesUi.this.delete(deleteIndex, message);
                            }
                        }));

                        p.show(t, e.getPoint().x, e.getPoint().y);
                    }
                }
            });
//                {
//                    int width = 200;
//                    t.setSize(new Dimension(width, Integer.MAX_VALUE));
//                    Dimension d = t.getPreferredSize();
//
//                    System.out.println("Text pref " + d);
//
//                    t.setPreferredSize(new Dimension(width, d.height));
//                }

            JPanel b = new JPanel(new BorderLayout());
            b.setBackground(Color.WHITE);
            b.add(t, BorderLayout.CENTER);
            {
                JLabel timestamp = new JLabel(message.getTimestamp());
                timestamp.setForeground(Color.GRAY);
                timestamp.setToolTipText(message.getTimestampTooltip());
                Font font = timestamp.getFont();
                timestamp.setFont(font.deriveFont(Font.PLAIN, (int) (0.8 * font.getSize())));
                timestamp.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
                b.add(timestamp, BorderLayout.SOUTH);
            }


            if (previousMessage == null || !previousMessage.getEmail().equals(message.getEmail())) {
                //toggle next arrow
                arrowLeft = !arrowLeft;

                perUserMessages = new Box(BoxLayout.Y_AXIS);
                //arrow border
                b.setBorder(new BubbleBorder(OVATION_GREEN, arrowLeft));

                JPanel withUser = new JPanel(new BorderLayout());
                withUser.setBackground(Color.WHITE);
                final JLabel user;

                if (showGravatar) {
                    user = new JLabel(message.getUsername());
                    executors.submit(new SwingWorker<ImageIcon, Void>() {
                        @Override
                        protected ImageIcon doInBackground() throws Exception {
                            return new ImageIcon(gravatarURL(message.getEmail()));
                        }

                        @Override
                        protected void done() {
                            if (isCancelled()) {
                                return;
                            }
                            try {
                                ImageIcon icon = get();

                                user.setIcon(icon);
                            } catch (Exception e) {
                            }
                        }
                    });
                } else {
                    user = new JLabel(message.getUsername());
                }
                user.setVerticalAlignment(SwingConstants.TOP);
                withUser.add(user, arrowLeft ? BorderLayout.WEST : BorderLayout.EAST);
                withUser.add(perUserMessages, BorderLayout.CENTER);

                perUserMessages.add(b);

                messages.add(withUser);

            } else {
                //round border
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(0, arrowLeft ? BubbleBorder.DEFAULT_ARROW_SIZE : 0, 0,
                        arrowLeft ? 0 : BubbleBorder.DEFAULT_ARROW_SIZE),
                        new BubbleBorder(OVATION_GREEN, 0, arrowLeft)));
                perUserMessages.add(b);
            }
            perUserMessages.add(Box.createVerticalStrut(5));

            previousMessage = message;
        }
    }

    static class BubbleBorder extends AbstractBorder {

        public final static int DEFAULT_ARROW_SIZE = 7;
        public final static int DEFAULT_RADIUS = 8;
        
        private final RenderingHints antialias = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        private final Color color;
        private final int radius = DEFAULT_RADIUS;
        private final int tickHeight;
        private final Insets insets;
        private final boolean arrowLeft;
        private final BasicStroke stroke = new BasicStroke(1);

        BubbleBorder(Color color, boolean arrowLeft) {
            this(color, DEFAULT_ARROW_SIZE, arrowLeft);
        }

        BubbleBorder(Color color, int tickHeight, boolean arrowLeft) {
            this.tickHeight = tickHeight;
            this.color = color;
            this.arrowLeft = arrowLeft;

            insets = new Insets(radius, radius + tickHeight, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return insets;
        }

        @Override
        public Insets getBorderInsets(Component c, Insets otherInsets) {
            otherInsets.set(insets.top, insets.left, insets.bottom, insets.right);
            return otherInsets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;

            RoundRectangle2D.Double bubble = new RoundRectangle2D.Double(
                    x + (arrowLeft ? tickHeight : 0),
                    y,
                    width - 1 - tickHeight,
                    height - 1,
                    radius,
                    radius);

            Polygon pointer = new Polygon();

            pointer.addPoint(
                    arrowLeft ? x + tickHeight : width - tickHeight - 1,
                    y + radius);
            pointer.addPoint(
                    arrowLeft ? x + 0 : width,
                    y + radius);
            pointer.addPoint(
                    arrowLeft ? x + tickHeight : width - tickHeight - 1,
                    y + radius + tickHeight);

            Area area = new Area(bubble);
            area.add(new Area(pointer));

            g2.setRenderingHints(antialias);

            g2.setColor(color);
            g2.setStroke(stroke);
            g2.draw(area);
        }
    }

    public static URL gravatarURL(String email) {
        try {
            MessageDigest md =
                    MessageDigest.getInstance("MD5"); //NOI18N
            byte[] data = md.digest(email.getBytes("CP1252")); //NOI18N

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < data.length; ++i) {
                sb.append(Integer.toHexString((data[i]
                        & 0xFF) | 0x100).substring(1, 3));
            }
            return new URL("https://secure.gravatar.com/avatar/" + sb.toString() + "?s=40&d=identicon"); //NOI18N
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        } catch (MalformedURLException e) {
        }
        return null;
    }

    static class ScrollableBox extends Box implements Scrollable {

        private ScrollableBox(int axis) {
            super(axis);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 1;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            Container parent = getParent();
            if (parent instanceof JViewport) {
                return ((JViewport) parent).getExtentSize().height;
            } else {
                return 10;
            }
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
