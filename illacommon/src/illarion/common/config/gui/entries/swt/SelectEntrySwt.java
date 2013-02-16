/*
 * This file is part of the Illarion Common Library.
 *
 * Copyright © 2013 - Illarion e.V.
 *
 * The Illarion Common Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion Common Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion Common Library.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.common.config.gui.entries.swt;

import illarion.common.config.entries.ConfigEntry;
import illarion.common.config.entries.SelectEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import javax.annotation.Nonnull;

/**
 * This is a special implementation for the combo box that is initialized with a
 * configuration entry. Its sole purpose is the use along with the configuration
 * system.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class SelectEntrySwt implements SaveableEntrySwt {
    /**
     * The combo box used to display the entry in SWT.
     */
    @Nonnull
    private final Combo combo;

    /**
     * The text entry used to initialize this instance.
     */
    @Nonnull
    private final SelectEntry entry;

    /**
     * Create a instance of this check entry and set the configuration entry
     * that is used to setup this class.
     *
     * @param usedEntry    the entry used to setup this class, the entry needs to
     *                     pass the check with the static method
     * @param parentWidget the widget this widget is added to
     */
    @SuppressWarnings("nls")
    public SelectEntrySwt(final ConfigEntry usedEntry,
                          final Composite parentWidget) {
        if (!isUsableEntry(usedEntry)) {
            throw new IllegalArgumentException("ConfigEntry type illegal.");
        }

        combo = new Combo(parentWidget, SWT.DROP_DOWN | SWT.SIMPLE);
        entry = (SelectEntry) usedEntry;

        combo.setItems(entry.getLabels());
        combo.setText(entry.getLabel());
    }

    /**
     * Text a entry if it is usable with this class or not.
     *
     * @param entry the entry to test
     * @return <code>true</code> in case this entry is usable with this class
     */
    public static boolean isUsableEntry(final ConfigEntry entry) {
        return (entry instanceof SelectEntry);
    }

    /**
     * Save the value in this text entry to the configuration.
     */
    @Override
    public void save() {
        entry.setValue(combo.getSelectionIndex());
    }

    /**
     * Set the layout data of this widget.
     */
    @Override
    public void setLayoutData(final Object data) {
        combo.setLayoutData(data);
    }

}
