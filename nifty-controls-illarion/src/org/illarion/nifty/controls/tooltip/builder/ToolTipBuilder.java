/*
 * This file is part of the Illarion Nifty-GUI Controls.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion Nifty-GUI Controls is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion Nifty-GUI Controls is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion Nifty-GUI Controls.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.illarion.nifty.controls.tooltip.builder;

import de.lessvoid.nifty.builder.ControlBuilder;
import de.lessvoid.nifty.tools.Color;

import javax.annotation.Nonnull;

/**
 * Build the tooltip.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public class ToolTipBuilder extends ControlBuilder {
    public ToolTipBuilder() {
        super("tooltip");
    }

    public ToolTipBuilder(final String id) {
        super(id, "tooltip");
    }

    public void title(final String value) {
        set("title", value);
    }

    public void titleColor(@Nonnull final Color value) {
        titleColor(value.getColorString());
    }

    public void titleColor(final String value) {
        set("titleColor", value);
    }

    public void description(final String value) {
        set("description", value);
    }

    public void producer(final String value) {
        set("producer", value);
    }

    public void weight(final String value) {
        set("weight", value);
    }

    public void worth(final long value) {
        set("worth", Long.toString(value));
    }

    public void quality(final String value) {
        set("quality", value);
    }

    public void durability(final String value) {
        set("durability", value);
    }

    public void diamondLevel(final int value) {
        set("diamondLevel", Integer.toString(value));
    }

    public void emeraldLevel(final int value) {
        set("emeraldLevel", Integer.toString(value));
    }

    public void rubyLevel(final int value) {
        set("rubyLevel", Integer.toString(value));
    }

    public void obsidianLevel(final int value) {
        set("obsidianLevel", Integer.toString(value));
    }

    public void sapphireLevel(final int value) {
        set("sapphireLevel", Integer.toString(value));
    }

    public void amethystLevel(final int value) {
        set("amethystLevel", Integer.toString(value));
    }

    public void topazLevel(final int value) {
        set("topazLevel", Integer.toString(value));
    }

    public void gemBonus(final String value) {
        set("gemBonus", value);
    }
}
