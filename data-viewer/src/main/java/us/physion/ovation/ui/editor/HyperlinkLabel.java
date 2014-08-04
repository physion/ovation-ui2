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

package us.physion.ovation.ui.editor;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 *
 * @author barry
 */
public class HyperlinkLabel extends JButton {

    public HyperlinkLabel() {
        this("link", "Go to link");
    }

    public HyperlinkLabel(String text, String tooltip) {
        super(makeText(text));
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorderPainted(false);
        setOpaque(false);
        setBackground(Color.WHITE);
        setToolTipText(tooltip);
        Font f = getFont();
        setFont(f.deriveFont(8));
    }

    private static String makeText(String text) {
        return "<html><font color=\"#000099\"><u>" + text + "</u></font></html>";
    }

    public void setText(String text) {
        if (!text.startsWith("<html>")) {
            text = makeText(text);
        }

        super.setText(text);
    }
}
